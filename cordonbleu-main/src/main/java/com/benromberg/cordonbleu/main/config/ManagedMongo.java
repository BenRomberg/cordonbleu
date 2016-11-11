package com.benromberg.cordonbleu.main.config;

import io.dropwizard.lifecycle.Managed;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

@Singleton
public class ManagedMongo implements Managed {
    private MongoClient mongoClient;

    @Inject
    public ManagedMongo(CordonBleuConfiguration configuration) {
        mongoClient = new MongoClient(new MongoClientURI(configuration.getMongoUrl()));
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
        mongoClient.close();
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

}
