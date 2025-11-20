// Name: Yiming Fu
// Andrew ID: yimingfu

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
import java.util.Map;
import java.util.HashMap;

// This class is defined to write logs to Mongo DB
public class LogHelper {
    private static MongoClient client;
    private static MongoCollection<Document> logs;
    private static MongoCollection<Document> playerStats;
    private static MongoCollection<Document> teamStats;

    // Execute only once, which will setup all connecting objects
    static {
        String uri = "mongodb+srv://yimingfu:mJt6njGpLAniRJa7@cluster0.b5tx8sw.mongodb.net/?appName=Cluster0";
        client = MongoClients.create(uri);
        MongoDatabase db = client.getDatabase("testdb");
        logs = db.getCollection("logs");
        playerStats = db.getCollection("playerStats");
        teamStats = db.getCollection("teamStats");
    }

    // Write request logs
    public static void logRequest(HttpServletRequest req, 
                           int statusCode, 
                           long thirdPartyLatency,
                           int responseSize) {
        // Establish query document
        Document logDoc = new Document()
                .append("timestamp", System.currentTimeMillis())
                .append("clientIP", req.getRemoteAddr())
                .append("deviceModel", req.getHeader("User-Agent"))
                .append("requestPath", req.getRequestURI())
                .append("requestParams", req.getQueryString())
                .append("thirdPartyLatency", thirdPartyLatency)
                .append("statusCode", statusCode)
                .append("responseSize", responseSize);
        // Write to Mongo DB
        logs.insertOne(logDoc);
    }

    // Return all stored logs within Mongo DB
    public static List<Document> getAllLogs() {
        List<Document> list = new ArrayList<>();
        logs.find().into(list);
        return list;
    }

    // Increment player count by one
    public static void incrementPlayerCount(int playerId, String playerName) {
        Document filter = new Document("playerId", playerId);
        Document update = new Document("$inc", new Document("count", 1))
                .append("$setOnInsert",
                        new Document("playerName", playerName));
        playerStats.updateOne(filter, update, new UpdateOptions().upsert(true));
    }

    // Increment team count by one
    public static void incrementTeamCount(int teamId, String teamName) {
        Document filter = new Document("teamId", teamId);
        Document update = new Document("$inc", new Document("count", 1))
                .append("$setOnInsert",
                        new Document("teamName", teamName));
        teamStats.updateOne(filter, update, new UpdateOptions().upsert(true));
    }

    // Fetch top players list
    public static List<Document> getTopPlayers(int limit) {
        List<Document> result = new ArrayList<>();
        playerStats.find()
                .sort(new Document("count", -1))
                .limit(limit)
                .into(result);
        return result;
    }

    // Fetch top teams list
    public static List<Document> getTopTeams(int limit) {
        List<Document> result = new ArrayList<>();
        teamStats.find()
                .sort(new Document("count", -1))
                .limit(limit)
                .into(result);
        return result;
    }

    // Generate analytics result
    public static Document buildAnalyticsJson(int playerLimit, int teamLimit) {
        Document result = new Document();
        List<Document> logs = getAllLogs();
        // 1. API REQUEST FREQUENCY
        Map<String, Integer> apiFrequency = new HashMap<>();
        for (Document log : logs) {
            String path = log.getString("requestPath");
            apiFrequency.put(path, apiFrequency.getOrDefault(path, 0) + 1);
        }
        result.append("apiFrequency", apiFrequency);
        // 2. AVERAGE LATENCY
        Map<String, List<Long>> latencyByAPI = new HashMap<>();
        for (Document log : logs) {
            String path = log.getString("requestPath");
            Long latency = log.getLong("thirdPartyLatency");
            latencyByAPI.putIfAbsent(path, new ArrayList<>());
            latencyByAPI.get(path).add(latency);
        }
        Map<String, Double> avgLatency = new HashMap<>();
        for (String api : latencyByAPI.keySet()) {
            List<Long> latList = latencyByAPI.get(api);
            double avg = latList.stream().mapToLong(x -> x).average().orElse(0);
            avgLatency.put(api, avg);
        }
        result.append("avgLatency", avgLatency);
        // 3. ERROR RATE
        int successCount = 0;
        int badReqCount = 0;
        for (Document log : logs) {
            int status = log.getInteger("statusCode", 200);
            if (status == HttpServletResponse.SC_OK) successCount++;
            else if (status == HttpServletResponse.SC_BAD_REQUEST) badReqCount++;
        }
        Document err = new Document();
        err.append("success", successCount);
        err.append("badRequest", badReqCount);
        result.append("errorRate", err);
        // 4. TOP DEVICE MODELS
        Map<String, Integer> deviceCounts = new HashMap<>();
        for (Document log : logs) {
            String device = log.getString("deviceModel");
            if (device != null) {
                deviceCounts.put(device, deviceCounts.getOrDefault(device, 0) + 1);
            }
        }
        List<Map.Entry<String, Integer>> sortedDevices =
                new ArrayList<>(deviceCounts.entrySet());
        sortedDevices.sort((a, b) -> b.getValue() - a.getValue());
        List<Map.Entry<String, Integer>> topDevices =
                sortedDevices.subList(0, Math.min(5, sortedDevices.size()));
        List<Document> deviceArr = new ArrayList<>();
        for (var e : topDevices) {
            deviceArr.add(new Document("device", e.getKey())
                    .append("count", e.getValue()));
        }
        result.append("topDevices", deviceArr);
        // 5. TOP PLAYERS
        List<Document> topPlayers = getTopPlayers(playerLimit); // already sorted & limit
        result.append("topPlayers", topPlayers);
        // 6. TOP TEAMS
        List<Document> topTeams = getTopTeams(teamLimit); // already sorted & limit
        result.append("topTeams", topTeams);
        // For logs
        result.append("logs", logs);
        // Return all fields
        return result;
    }
}