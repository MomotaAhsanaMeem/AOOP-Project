package com.game.server.game;
import java.util.UUID;

public class Session {
  public final String sessionId = "s-" + UUID.randomUUID();
  public final String playerId;
  public final String name;
  public int level = 1;
  public int progress = 0;
  public int totalPoints = 0;
  public Session(String playerId, String name) { this.playerId = playerId; this.name = name; }
}
