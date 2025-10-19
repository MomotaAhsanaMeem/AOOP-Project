package com.game.server.model;
import java.util.List;

public class Dto {
  // client -> server
  public record Hello(String name) {}
  public record StartLevel(int level) {}
  public record RequestQuestion() {}
  public record AnswerSubmit(String questionId, int answerIndex, Long clientT0) {}
  public record ChatUser(String text) {}

  // server -> client
  public record Welcome(String playerId, String sessionId) {}
  public record Question(String questionId, String text, List<String> options, int timeLimitMs) {}
  public record AnswerEval(String questionId, boolean isCorrect, int correctIndex, int deltaPoints, int totalPoints) {}
  public record ProgressUpdate(int moveBy, int progress) {}
  public record ScoreUpdate(int totalPoints) {}
  public record ChatStart(String id) {}
  public record ChatDelta(String id, String chunk) {}
  public record ChatEnd(String id) {}
  public record Error(String code, String message) {}
}
