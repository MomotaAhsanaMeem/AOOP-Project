package com.algoarena.client.net;

import java.util.List;

public interface MessageListener {
    default void onOpen(String playerId, String sessionId) {}
    default void onQuestion(String questionId, String text, List<String> options, int timeLimitMs) {}
    default void onAnswerEval(String questionId, boolean isCorrect, int correctIndex, int deltaPoints, int totalPoints) {}
    default void onProgress(int moveBy, int progress) {}
    default void onScore(int totalPoints) {}
    default void onChatStart(String id) {}
    default void onChatDelta(String id, String chunk) {}
    default void onChatEnd(String id) {}
    default void onError(String code, String message) {}
    default void onClosed() {}
}
