package org.cyanidemc.cyanidetokens.mongo;

import lombok.Getter;


import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.cyanidemc.cyanidetokens.CyanideTokens;
import org.cyanidemc.cyanidetokens.events.TokenBalanceUpdateEvent;
import org.cyanidemc.cyanidetokens.util.CyanideUtil;

public class MongoDatabase {


    @Getter private MongoClient mongoClient;
    @Getter private DB database;
    @Getter private DBCollection collection;

    public boolean connect(ConfigurationSection section) {
        try {
            ServerAddress address = new ServerAddress(section.getString("ip"), section.getInt("port"));
            String databaseName = section.getString("database");

            mongoClient = new MongoClient(address);
            database = mongoClient.getDB(databaseName);
            collection = database.getCollection("tokens");

            return true;
        } catch (Exception e) {
            CyanideTokens.getInstance().getLogger().info("Something went wrong while connecting to the MongoDB server.");
            e.printStackTrace();
            return false;
        }
    }

    public DBObject getDocument(String uuid) {
        return collection.findOne(new BasicDBObject("uuid", uuid));
    }

    public void saveTokens(String uuid, int tokens, boolean async) {
        TokenBalanceUpdateEvent event = new TokenBalanceUpdateEvent(async, uuid, tokens);
        Bukkit.getPluginManager().callEvent(event);

        collection.save(new BasicDBObject("uuid", uuid).append("tokens", tokens));
    }

    public void removeTokens(String uuid) {
        DBObject document = getDocument(uuid);

        if (document != null) {
            collection.remove(document);
        }
    }

    public void addTokens(String uuid, int tokens, boolean async) {
        saveTokens(uuid, getTokens(uuid) + Math.max(tokens, 0), async);
    }

    public void takeTokens(String uuid, int tokens, boolean async) {
        saveTokens(uuid, getTokens(uuid) - Math.max(tokens, 0), async);
    }

    public int getTokens(String uuid) {
        DBObject document = getDocument(uuid);

        return document == null ? 0 : (int) document.get("tokens");
    }
}
