package com.game.server.service;

import com.game.server.store.PlayerRepo;
import com.game.server.store.doc.PlayerDoc;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class PlayerService {
  private final PlayerRepo repo;
  public PlayerService(PlayerRepo repo) { this.repo = repo; }

  public String upsertByName(String name) {
    var ex = repo.findByName(name).orElse(null);
    if (ex != null) { ex.lastSeen = new Date(); repo.save(ex); return ex.id; }
    var p = new PlayerDoc(); p.name = name; p.totalPoints = 0;
    repo.save(p); return p.id;
  }

  public void addPoints(String playerId, int delta) {
    repo.findById(playerId).ifPresent(p -> { p.totalPoints += delta; p.lastSeen = new Date(); repo.save(p);});
  }
}
