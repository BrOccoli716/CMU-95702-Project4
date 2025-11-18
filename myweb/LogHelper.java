package com.project.api;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import jakarta.servlet.http.*;
import java.util.List;
import java.util.ArrayList;

public class LogHelper {
    private static MongoClient client;
    private static MongoCollection<Document> logs;
    private static MongoCollection<Document> playerStats;

    static {
        String uri = "mongodb+srv://yimingfu:mJt6njGpLAniRJa7@cluster0.b5tx8sw.mongodb.net/?appName=Cluster0";
        client = MongoClients.create(uri);
        MongoDatabase db = client.getDatabase("testdb");
        logs = db.getCollection("logs");
        playerStats = db.getCollection("playerStats");
    }

    public static void logRequest(HttpServletRequest req, 
                           int statusCode, 
                           long thirdPartyLatency,
                           int responseSize) {
        Document logDoc = new Document()
                .append("timestamp", System.currentTimeMillis())
                .append("clientIP", req.getRemoteAddr())
                .append("deviceModel", req.getHeader("User-Agent"))
                .append("requestPath", req.getRequestURI())
                .append("requestParams", req.getQueryString())
                .append("thirdPartyLatency", thirdPartyLatency)
                .append("statusCode", statusCode)
                .append("responseSize", responseSize);
        logs.insertOne(logDoc);
    }

    public static List<Document> getAllLogs() {
        List<Document> list = new ArrayList<>();
        logs.find().into(list);
        return list;
    }

    public static void incrementPlayerCount(int playerId, String playerName) {
        Document filter = new Document("playerId", playerId);
        Document update = new Document("$inc", new Document("count", 1))
                .append("$setOnInsert",
                        new Document("playerName", playerName));
        playerStats.updateOne(filter, update, new UpdateOptions().upsert(true));
    }

    public static List<Document> getTopPlayers(int limit) {
        List<Document> result = new ArrayList<>();
        playerStats.find()
                .sort(new Document("count", -1))
                .limit(limit)
                .into(result);
        return result;
    }
}