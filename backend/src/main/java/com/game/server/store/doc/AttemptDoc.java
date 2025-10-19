package com.game.server.store.doc;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document("attempts")
public class AttemptDoc {
  @Id public String id;
  public String playerId;
  public String sessionId;
  public String questionId;
  public int answerIndex;
  public boolean isCorrect;
  public int deltaPoints;        // e.g., 10 if correct else 0
  public long responseTimeMs;    // optional: send from client or compute
  public Date createdAt = new Date();
}
