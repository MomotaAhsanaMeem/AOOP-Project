package com.game.server.store;
import com.game.server.store.doc.AttemptDoc;
import org.springframework.data.mongodb.repository.MongoRepository;
public interface AttemptRepo extends MongoRepository<AttemptDoc, String> {}
