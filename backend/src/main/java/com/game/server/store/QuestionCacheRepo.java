// src/main/java/com/game/server/store/QuestionCacheRepo.java
package com.game.server.store;

import com.game.server.store.doc.QuestionCacheDoc;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface QuestionCacheRepo extends MongoRepository<QuestionCacheDoc, String> {
  Optional<QuestionCacheDoc> findTopByTextHashOrderByCreatedAtDesc(String textHash);
}
