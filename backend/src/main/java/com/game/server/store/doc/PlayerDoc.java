package com.game.server.store.doc;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document("players")
public class PlayerDoc {
  @Id public String id;
  @Indexed(unique = true) public String name;
  public long totalPoints;
  public Date createdAt = new Date();
  public Date lastSeen = new Date();
}
