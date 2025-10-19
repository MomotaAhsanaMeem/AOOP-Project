package com.game.server.ai;
import java.util.List;
import java.util.UUID;
public class QuestionItem {
  public final String questionId = "q-" + UUID.randomUUID();
  public final String text; public final List<String> options; public final int correctIndex;
  public QuestionItem(String text, List<String> options, int correctIndex) { this.text=text; this.options=options; this.correctIndex=correctIndex;}
}
