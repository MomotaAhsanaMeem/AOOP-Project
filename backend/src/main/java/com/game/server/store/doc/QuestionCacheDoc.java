// src/main/java/com/game/server/store/doc/QuestionCacheDoc.java
package com.game.server.store.doc;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.List;

@Document("question_cache")
public class QuestionCacheDoc {
  @Id public String id;
  public String text;
  public List<String> options;
  public int correctIndex;
  public String textHash;           // for fast de-dupe
  public Instant createdAt = Instant.now();
}
