// package com.game.server.net;

// import com.game.server.ai.QuestionFactory;
// import com.game.server.ai.QuestionItem;
// import com.game.server.game.Session;
// import com.game.server.model.Dto;
// import com.game.server.service.PlayerService;
// import com.game.server.store.AttemptRepo;
// import com.game.server.store.QuestionCacheRepo;
// import com.game.server.store.doc.AttemptDoc;
// import com.game.server.store.doc.QuestionCacheDoc;
// import com.google.gson.Gson;
// import com.google.gson.JsonObject;
// import org.springframework.stereotype.Component;
// import org.springframework.web.reactive.socket.WebSocketHandler;
// import org.springframework.web.reactive.socket.WebSocketMessage;
// import org.springframework.web.reactive.socket.WebSocketSession;
// import reactor.core.publisher.Flux;
// import reactor.core.publisher.Mono;
// import reactor.core.publisher.Sinks;

// import java.time.Duration;
// import java.util.Map;
// import java.util.concurrent.ConcurrentHashMap;

// @Component
// public class GameWsHandler implements WebSocketHandler {
//     private static final Gson GSON = new Gson();

//     private final PlayerService players;
//     private final QuestionFactory qFactory;
//     private final QuestionCacheRepo qRepo;
//     private final AttemptRepo attemptRepo;

//     // per-connection state
//     private final Map<String, Session> byConn = new ConcurrentHashMap<>();
//     private final Map<String, QuestionItem> lastQ = new ConcurrentHashMap<>();

//     public GameWsHandler(PlayerService players, QuestionFactory qFactory,
//                          QuestionCacheRepo qRepo, AttemptRepo attemptRepo) {
//         this.players = players; this.qFactory = qFactory; this.qRepo = qRepo; this.attemptRepo = attemptRepo;
//     }

//     @Override
//     public Mono<Void> handle(WebSocketSession ws) {
//         // IMPORTANT: make sink typed to String
//         Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();
//         Flux<WebSocketMessage> outgoing = sink.asFlux().map((String s) -> ws.textMessage(s));

//         // incoming frames -> side-effect send() into sink
//         ws.receive()
//                 .map(WebSocketMessage::getPayloadAsText) // String
//                 .flatMap(text -> handleIncoming(ws, sink, text)) // Mono<Void> per msg
//                 .onErrorResume(e -> sendError(sink, "BAD_FRAME", e.getMessage()))
//                 .subscribe(); // drive the side effects

//         // cleanup when connection closes
//         return ws.send(outgoing)
//                 .doFinally(sig -> {
//                     byConn.remove(ws.getId());
//                     lastQ.remove(ws.getId());
//                     sink.tryEmitComplete();
//                 });
//     }

//     private Mono<Void> handleIncoming(WebSocketSession ws, Sinks.Many<String> sink, String text) {
//         try {
//             JsonObject obj = GSON.fromJson(text, JsonObject.class);
//             String type = obj.get("type").getAsString();
//             JsonObject data = obj.has("data") && obj.get("data").isJsonObject()
//                     ? obj.getAsJsonObject("data") : new JsonObject();

//             return switch (type) {
//                 case "HELLO" -> {
//                     String name = data.get("name").getAsString();
//                     String playerId = players.upsertByName(name);
//                     var s = new Session(playerId, name);
//                     byConn.put(ws.getId(), s);
//                     yield send(sink, "WELCOME", new Dto.Welcome(playerId, s.sessionId));
//                 }
//                 case "START_LEVEL" -> {
//                     var s = require(ws);
//                     s.level = data.get("level").getAsInt();
//                     yield Mono.empty();
//                 }
//                 case "REQUEST_QUESTION" -> {
//                     var s = require(ws);
//                     yield Mono.fromFuture(qFactory.nextLevel1Question("LEVEL1-DSA"))
//                             .flatMap(q -> {
//                                 // persist asked question
//                                 var qc = new QuestionCacheDoc();
//                                 qc.questionId = q.questionId; qc.level = 1; qc.category = "DSA";
//                                 qc.text = q.text; qc.options = q.options; qc.correctIndex = q.correctIndex; qc.source = "gemini";
//                                 qRepo.save(qc);

//                                 lastQ.put(ws.getId(), q);
//                                 return send(sink, "QUESTION", new Dto.Question(q.questionId, q.text, q.options, 15000));
//                             });
//                 }
//                 case "ANSWER_SUBMIT" -> {
//                     var s = require(ws);
//                     var q = lastQ.get(ws.getId());
//                     if (q == null) {
//                         yield sendError(sink, "NO_QUESTION", "No question pending.");
//                     }
//                     int ans = data.get("answerIndex").getAsInt();
//                     boolean ok = (ans == q.correctIndex);
//                     int delta = ok ? 10 : 0;
//                     if (ok) { s.totalPoints += delta; s.progress += 10; players.addPoints(s.playerId, delta); }

//                     // persist attempt
//                     var a = new AttemptDoc();
//                     a.playerId = s.playerId; a.sessionId = s.sessionId; a.questionId = q.questionId;
//                     a.answerIndex = ans; a.isCorrect = ok; a.deltaPoints = delta;
//                     a.responseTimeMs = data.has("clientT0") ? (System.currentTimeMillis() - data.get("clientT0").getAsLong()) : 0;
//                     attemptRepo.save(a);

//                     lastQ.remove(ws.getId());

//                     yield send(sink, "ANSWER_EVAL", new Dto.AnswerEval(q.questionId, ok, q.correctIndex, delta, s.totalPoints))
//                             .then(send(sink, "PROGRESS_UPDATE", new Dto.ProgressUpdate(ok ? 10 : 0, s.progress)))
//                             .then(send(sink, "SCORE_UPDATE", new Dto.ScoreUpdate(s.totalPoints)));
//                 }
//                 case "CHAT_USER" -> {
//                     String txt = data.get("text").getAsString();
//                     String id = java.util.UUID.randomUUID().toString();
//                     yield send(sink, "CHAT_AI_START", new Dto.ChatStart(id))
//                             .thenMany(Flux.fromIterable(chunk("Hint: Think about DSA properties. You said: " + txt, 42))
//                                     .delayElements(Duration.ofMillis(40))
//                                     .flatMap(ch -> send(sink, "CHAT_AI_DELTA", new Dto.ChatDelta(id, ch))))
//                             .then(send(sink, "CHAT_AI_END", new Dto.ChatEnd(id)));
//                 }
//                 default -> sendError(sink, "UNKNOWN_TYPE", "Unsupported type: " + type);
//             };

//         } catch (Exception e) {
//             return sendError(sink, "BAD_FRAME", e.getMessage());
//         }
//     }

//     private Session require(WebSocketSession ws) {
//         var s = byConn.get(ws.getId());
//         if (s == null) throw new IllegalStateException("HELLO required first");
//         return s;
//     }

//     // ---- emit helpers (typed to String) ----
//     private Mono<Void> send(Sinks.Many<String> sink, String type, Object data) {
//         var obj = new JsonObject();
//         obj.addProperty("type", type);
//         obj.add("data", GSON.toJsonTree(data));
//         sink.tryEmitNext(obj.toString());
//         return Mono.empty();
//     }

//     private Mono<Void> sendError(Sinks.Many<String> sink, String code, String message) {
//         var d = new JsonObject();
//         d.addProperty("code", code);
//         d.addProperty("message", message);
//         var obj = new JsonObject();
//         obj.addProperty("type", "ERROR");
//         obj.add("data", d);
//         sink.tryEmitNext(obj.toString());
//         return Mono.empty();
//     }

//     private java.util.List<String> chunk(String s, int size) {
//         var out = new java.util.ArrayList<String>();
//         for (int i = 0; i < s.length(); i += size) out.add(s.substring(i, Math.min(s.length(), i + size)));
//         return out;
//     }
// }
