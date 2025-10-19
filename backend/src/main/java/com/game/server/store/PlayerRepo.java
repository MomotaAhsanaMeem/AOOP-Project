package com.game.server.store;
import com.game.server.store.doc.PlayerDoc;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;
public interface PlayerRepo extends MongoRepository<PlayerDoc, String> {
  Optional<PlayerDoc> findByName(String name);
}