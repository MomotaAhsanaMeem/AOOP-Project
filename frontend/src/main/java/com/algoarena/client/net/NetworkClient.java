package com.algoarena.client.net;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class NetworkClient implements WebSocket.Listener {
    private static final Gson GSON = new Gson();

    private final HttpClient http = HttpClient.newHttpClient();
    private WebSocket ws;
    private volatile boolean open = false;

    private MessageListener listener;

    /** Allow controllers to register a single listener for socket events */
    public void setListener(MessageListener l) { this.listener = l; }

    /** Connect (idempotent). Sends HELLO + START_LEVEL. */
    public CompletableFuture<Void> connect(String url, String name, int startLevel) {
        if (open && ws != null) {
            // still send START_LEVEL in case user restarted level
            JsonObject sl = new JsonObject();
            sl.addProperty("level", startLevel);
            sendFrame("START_LEVEL", sl);
            return CompletableFuture.completedFuture(null);
        }
        return http.newWebSocketBuilder()
            .buildAsync(URI.create(url), this)
            .thenAccept(sock -> {
                this.ws = sock;

                JsonObject hello = new JsonObject();
                hello.addProperty("name", name);
                sendFrame("HELLO", hello);

                JsonObject sl = new JsonObject();
                sl.addProperty("level", startLevel);
                sendFrame("START_LEVEL", sl);
            });
    }

    public void requestQuestion() {
        sendFrame("REQUEST_QUESTION", new JsonObject());
    }

    /** Backend expects SUBMIT_ANSWER with { index } (no questionId required). */
    public void submitAnswer(String questionId, int answerIndex) {
        JsonObject d = new JsonObject();
        d.addProperty("index", answerIndex);
        d.addProperty("clientT0", System.currentTimeMillis());
        sendFrame("SUBMIT_ANSWER", d);
    }

    public void sendChat(String text) {
        JsonObject d = new JsonObject();
        d.addProperty("text", text);
        sendFrame("CHAT_USER", d);
    }

    public void close() {
        if (ws != null) ws.sendClose(WebSocket.NORMAL_CLOSURE, "bye");
    }

    // ---- WebSocket.Listener ----
    @Override
    public void onOpen(WebSocket webSocket) {
        open = true;
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        try {
            JsonObject obj = GSON.fromJson(data.toString(), JsonObject.class);
            String type = obj.has("type") && !obj.get("type").isJsonNull() ? obj.get("type").getAsString() : "";
            JsonObject d = obj.has("data") && obj.get("data").isJsonObject()
                    ? obj.getAsJsonObject("data") : new JsonObject();

            switch (type) {
                case "WELCOME" -> {
                    String playerId = d.has("playerId") && !d.get("playerId").isJsonNull() ? d.get("playerId").getAsString() : "";
                    String sessionId = d.has("sessionId") && !d.get("sessionId").isJsonNull() ? d.get("sessionId").getAsString() : "";
                    if (listener != null) listener.onOpen(playerId, sessionId);
                }

                case "QUESTION" -> {
                    // Parse both new (options[]) and legacy (a/b/c/d) formats
                    String qid  = d.has("id")   && !d.get("id").isJsonNull()   ? d.get("id").getAsString()   : null;
                    String text = d.has("text") && !d.get("text").isJsonNull() ? d.get("text").getAsString() : "Question";

                    List<String> options = new ArrayList<>();

                    if (d.has("options") && d.get("options").isJsonArray()) {
                        JsonArray arr = d.getAsJsonArray("options");
                        for (JsonElement e : arr) {
                            if (e != null && !e.isJsonNull()) options.add(e.getAsString());
                        }
                    } else {
                        if (d.has("a") && !d.get("a").isJsonNull()) options.add(d.get("a").getAsString());
                        if (d.has("b") && !d.get("b").isJsonNull()) options.add(d.get("b").getAsString());
                        if (d.has("c") && !d.get("c").isJsonNull()) options.add(d.get("c").getAsString());
                        if (d.has("d") && !d.get("d").isJsonNull()) options.add(d.get("d").getAsString());
                    }

                    int timeLimitMs = d.has("timeLimitMs") && !d.get("timeLimitMs").isJsonNull()
                            ? d.get("timeLimitMs").getAsInt() : 0;

                    if (listener != null) {
                        listener.onQuestion(qid, text, options, timeLimitMs);
                    }
                }

                case "ANSWER_EVAL" -> {
                    // New backend sends: correct (boolean), hint (string)
                    boolean correct = d.has("correct") && !d.get("correct").isJsonNull() && d.get("correct").getAsBoolean();

                    // Legacy fields support
                    boolean legacyCorrect = d.has("isCorrect") && !d.get("isCorrect").isJsonNull() && d.get("isCorrect").getAsBoolean();

                    boolean isCorrect = correct || legacyCorrect;

                    int correctIndex = d.has("correctIndex") && !d.get("correctIndex").isJsonNull() ? d.get("correctIndex").getAsInt() : -1;
                    int deltaPoints  = d.has("deltaPoints")  && !d.get("deltaPoints").isJsonNull()  ? d.get("deltaPoints").getAsInt()  : 0;
                    int totalPoints  = d.has("totalPoints")  && !d.get("totalPoints").isJsonNull()  ? d.get("totalPoints").getAsInt()  : 0;

                    // Provide a fake qid if missing; your UI doesn’t use it anyway
                    String qid = d.has("questionId") && !d.get("questionId").isJsonNull() ? d.get("questionId").getAsString() : "";

                    if (listener != null) listener.onAnswerEval(qid, isCorrect, correctIndex, deltaPoints, totalPoints);
                }

                case "PROGRESS_UPDATE" -> {
                    if (listener != null) {
                        int moveBy = d.has("moveBy") && !d.get("moveBy").isJsonNull() ? d.get("moveBy").getAsInt() : 0;
                        int progress = d.has("progress") && !d.get("progress").isJsonNull() ? d.get("progress").getAsInt() : 0;
                        listener.onProgress(moveBy, progress);
                    }
                }

                case "SCORE_UPDATE" -> {
                    if (listener != null) {
                        int total = d.has("totalPoints") && !d.get("totalPoints").isJsonNull() ? d.get("totalPoints").getAsInt() : 0;
                        listener.onScore(total);
                    }
                }

                case "CHAT_AI_START" -> {
                    if (listener != null) listener.onChatStart(d.has("id") && !d.get("id").isJsonNull() ? d.get("id").getAsString() : "");
                }

                case "CHAT_AI_DELTA" -> {
                    if (listener != null) {
                        String id = d.has("id") && !d.get("id").isJsonNull() ? d.get("id").getAsString() : "";
                        String chunk = d.has("chunk") && !d.get("chunk").isJsonNull() ? d.get("chunk").getAsString() : "";
                        listener.onChatDelta(id, chunk);
                    }
                }

                case "CHAT_AI_END" -> {
                    if (listener != null) listener.onChatEnd(d.has("id") && !d.get("id").isJsonNull() ? d.get("id").getAsString() : "");
                }

                case "ERROR" -> {
                    String code = d.has("code") && !d.get("code").isJsonNull() ? d.get("code").getAsString() : "ERROR";
                    String msg  = d.has("message") && !d.get("message").isJsonNull() ? d.get("message").getAsString() : "";
                    if (listener != null) listener.onError(code, msg);
                }
            }
        } catch (Exception ex) {
            if (listener != null) listener.onError("CLIENT_PARSE", ex.getMessage());
        }
        webSocket.request(1);
        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        if (listener != null) listener.onError("SOCKET", error.toString());
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        open = false;
        if (listener != null) listener.onClosed();
        return CompletableFuture.completedFuture(null);
    }

    private void sendFrame(String type, JsonObject data) {
        if (ws == null) return;
        JsonObject obj = new JsonObject();
        obj.addProperty("type", type);
        if (data == null) data = new JsonObject();
        obj.add("data", data);
        ws.sendText(obj.toString(), true);
    }
}
