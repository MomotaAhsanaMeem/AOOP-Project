package com.game.server.ws;

import com.game.server.ai.GeminiClient;
import com.game.server.ai.QuestionFactory;
import com.game.server.ai.QuestionItem;
import com.google.gson.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.*;

@Component
public class GameWsHandler extends TextWebSocketHandler {

  private final QuestionFactory questionFactory;
  private final GeminiClient gemini;

  private final Gson gson = new GsonBuilder().create();

  // Per-connection context
  private final ConcurrentMap<String, PlayerCtx> bySessionId = new ConcurrentHashMap<>();

  public GameWsHandler(QuestionFactory questionFactory, GeminiClient gemini) {
    this.questionFactory = questionFactory;
    this.gemini = gemini;
  }

  // ───────── WebSocket lifecycle ─────────

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    String pid = objectId();
    bySessionId.put(session.getId(), new PlayerCtx(pid));
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    bySessionId.remove(session.getId());
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    String payload = message.getPayload();
    JsonObject root;
    try {
      root = JsonParser.parseString(payload).getAsJsonObject();
    } catch (Exception e) {
      sendError(session, "BAD_FRAME", e.toString());
      return;
    }

    String type = jString(root, "type");
    JsonObject data = jObj(root, "data");
    if (!StringUtils.hasText(type)) {
      sendError(session, "BAD_FRAME", "Missing 'type'");
      return;
    }

    switch (type) {
      case "HELLO" -> onHello(session, data);
      case "START_LEVEL" -> onStartLevel(session, data);
      case "REQUEST_QUESTION" -> onRequestQuestion(session);
      case "SUBMIT_ANSWER" -> onSubmitAnswer(session, data);
      case "CHAT_USER" -> onChatUser(session, data);
      default -> sendError(session, "UNKNOWN_TYPE", "Unsupported type: " + type);
    }
  }

  // ───────── Frame handlers ─────────

  private void onHello(WebSocketSession ws, JsonObject data) throws IOException {
    PlayerCtx ctx = bySessionId.get(ws.getId());
    if (ctx == null) { sendError(ws, "NO_CTX", "No context"); return; }

    String name = data != null ? jString(data, "name") : null;
    if (!StringUtils.hasText(name)) name = "Player";
    ctx.name = name;

    JsonObject resp = new JsonObject();
    resp.addProperty("type", "WELCOME");
    JsonObject d = new JsonObject();
    d.addProperty("playerId", ctx.playerId);
    d.addProperty("sessionId", "s-" + UUID.randomUUID());
    resp.add("data", d);
    ws.sendMessage(new TextMessage(gson.toJson(resp)));
  }

  private void onStartLevel(WebSocketSession ws, JsonObject data) throws IOException {
    PlayerCtx ctx = bySessionId.get(ws.getId());
    if (ctx == null) { sendError(ws, "NO_CTX", "No context"); return; }
    int level = (data != null && data.has("level")) ? data.get("level").getAsInt() : 1;
    ctx.level = level;

    JsonObject resp = new JsonObject();
    resp.addProperty("type", "LEVEL_READY");
    JsonObject d = new JsonObject();
    d.addProperty("level", level);
    d.addProperty("at", Instant.now().toString());
    resp.add("data", d);
    ws.sendMessage(new TextMessage(gson.toJson(resp)));
  }

  private void onRequestQuestion(WebSocketSession ws) throws IOException {
  PlayerCtx ctx = bySessionId.get(ws.getId());
  if (ctx == null) { sendError(ws, "NO_CTX", "No context"); return; }

  String flavor = "Player " + (ctx.name == null ? "Hero" : ctx.name);

  questionFactory.nextLevel1Question(flavor).thenAccept(q -> {
    try {
      ctx.lastQuestion = q;

      JsonObject resp = new JsonObject();
      resp.addProperty("type", "QUESTION");

      JsonObject d = new JsonObject();
      d.addProperty("id", "q-" + UUID.randomUUID());
      d.addProperty("text", q.text);

      // options array (new format)
      JsonArray opts = new JsonArray();
      for (String o : q.options) opts.add(new JsonPrimitive(o));
      d.add("options", opts);

      // legacy fields (old clients sometimes read these)
      String a = q.options.size() > 0 ? q.options.get(0) : "";
      String b = q.options.size() > 1 ? q.options.get(1) : "";
      String c = q.options.size() > 2 ? q.options.get(2) : "";
      String e = q.options.size() > 3 ? q.options.get(3) : "";

      d.addProperty("a", a);
      d.addProperty("b", b);
      d.addProperty("c", c);
      d.addProperty("d", e);

      // optional time limit so your listener signature still matches
      d.addProperty("timeLimitMs", 0);

      resp.add("data", d);
      ws.sendMessage(new TextMessage(gson.toJson(resp)));
    } catch (IOException e) {
      safeError(ws, "SEND_FAIL", e.toString());
    }
  });
}

  private void onSubmitAnswer(WebSocketSession ws, JsonObject data) throws IOException {
    PlayerCtx ctx = bySessionId.get(ws.getId());
    if (ctx == null) { sendError(ws, "NO_CTX", "No context"); return; }
    if (ctx.lastQuestion == null) { sendError(ws, "NO_QUESTION", "Ask for a question first"); return; }

    int idx = (data != null && data.has("index")) ? data.get("index").getAsInt() : -1;
    boolean correct = (idx == ctx.lastQuestion.correctIndex);

    JsonObject resp = new JsonObject();
    resp.addProperty("type", "ANSWER_EVAL");
    JsonObject d = new JsonObject();
    d.addProperty("correct", correct);
    d.addProperty("hint", correct
        ? "Nice! Keep going."
        : "Think about the core property the problem is testing (e.g., stability, LIFO/FIFO, reliance on sorted order).");
    resp.add("data", d);
    ws.sendMessage(new TextMessage(gson.toJson(resp)));
  }

  private void onChatUser(WebSocketSession ws, JsonObject data) throws IOException {
    PlayerCtx ctx = bySessionId.get(ws.getId());
    if (ctx == null) { sendError(ws, "NO_CTX", "No context"); return; }

    String text = data != null ? jString(data, "text") : "";
    if (text == null) text = "";

    boolean looksGreeting = text.matches("(?i).*(^|\\b)(hi|hello|hey|hola|yo|assalam|salam|how are you)\\b.*");
    final String mode = (looksGreeting && ctx.lastQuestion == null) ? "chitchat" : "hint";

    send(ws, "CHAT_AI_START", json(j -> j.addProperty("id", "c-" + UUID.randomUUID())));

    CompletableFuture<String> ai = gemini.generateGuideReply(
        mode,
        text,
        ctx.lastQuestion != null ? ctx.lastQuestion.text : null,
        ctx.lastQuestion != null ? ctx.lastQuestion.options : null
    );

    withTimeout(ai, 900, TimeUnit.MILLISECONDS)
        .exceptionally(ex -> null)
        .thenAccept(reply -> {
          String out = (reply == null || reply.isBlank())
              ? (mode.equals("chitchat")
                  ? "Hey! I’m your Guide—how can I help?"
                  : "Let’s reason about the approach, not the final option.")
              : reply;

          try {
            send(ws, "CHAT_AI_DELTA", json(j -> {
              j.addProperty("id", "c-1");
              j.addProperty("chunk", out);
            }));
            send(ws, "CHAT_AI_END", json(j -> j.addProperty("id", "c-1")));
          } catch (IOException e) {
            safeError(ws, "SEND_FAIL", e.toString());
          }
        });
  }

  // ───────── Utilities ─────────

  private static String objectId() { return UUID.randomUUID().toString().replace("-", ""); }

  private static CompletableFuture<String> withTimeout(CompletableFuture<String> src, long t, TimeUnit u) {
    final CompletableFuture<String> tm = new CompletableFuture<>();
    ScheduledExecutorService sch = Executors.newSingleThreadScheduledExecutor();
    sch.schedule(() -> tm.complete(null), t, u);
    return src.applyToEither(tm, v -> v).whenComplete((v, ex) -> sch.shutdown());
  }

  private void send(WebSocketSession ws, String type, JsonObject data) throws IOException {
    JsonObject root = new JsonObject();
    root.addProperty("type", type);
    if (data != null) root.add("data", data);
    ws.sendMessage(new TextMessage(gson.toJson(root)));
  }

  private void sendError(WebSocketSession ws, String code, String message) throws IOException {
    send(ws, "ERROR", json(j -> {
      j.addProperty("code", code);
      j.addProperty("message", message);
    }));
  }

  private void safeError(WebSocketSession ws, String code, String message) {
    try { sendError(ws, code, message); } catch (Exception ignored) {}
  }

  private JsonObject json(java.util.function.Consumer<JsonObject> f) {
    JsonObject o = new JsonObject();
    f.accept(o);
    return o;
  }

  private static String jString(JsonObject obj, String key) {
    if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return null;
    return obj.get(key).getAsString();
  }

  private static JsonObject jObj(JsonObject obj, String key) {
    if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return null;
    return obj.get(key).getAsJsonObject();
  }

  // Per-connection state
  private static final class PlayerCtx {
    final String playerId;
    String name;
    int level = 1;
    QuestionItem lastQuestion;
    PlayerCtx(String playerId) { this.playerId = playerId; }
  }
}
