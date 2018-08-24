package io.annot8.components.mongo.resources;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public interface MongoConnection {

  MongoDatabase getDatabase();

  default <T> MongoCollection<T> getCollection(Class<T> clazz) {
    return getCollection().withDocumentClass(clazz);
  }

  MongoCollection getCollection();

  void disconnect();
}
