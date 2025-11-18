package com.project.api;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

public class LogsDataServlet extends HttpServlet {
    private static final int playerLimit = 5;
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        // Fetch logs from MongoDB
        List<Document> logs = LogHelper.getAllLogs();
        // 1. API REQUEST FREQUENCY
        Map<String, Integer> apiFrequency = new HashMap<>();
        for (Document log : logs) {
            String path = log.getString("requestPath");
            apiFrequency.put(path, apiFrequency.getOrDefault(path, 0) + 1);
        }
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
        // 3. ERROR RATE
        int successCount = 0;
        int badReqCount = 0;

        for (Document log : logs) {
            int status = log.getInteger("statusCode", 200);
            if (status == HttpServletResponse.SC_OK) successCount++;
            else if (status == HttpServletResponse.SC_BAD_REQUEST) badReqCount++;
        }

        // ========== 4. TOP DEVICE MODELS ==========
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

        // ========== 5. TOP PLAYERS ==========
        List<Document> topPlayers = LogHelper.getTopPlayers(playerLimit); // already sorted & limit


        // ========== BUILD JSON ==========
        Document result = new Document();

        result.append("apiFrequency", apiFrequency);
        result.append("avgLatency", avgLatency);

        Document err = new Document();
        err.append("success", successCount);
        err.append("badRequest", badReqCount);
        result.append("errorRate", err);

        // devices
        List<Document> deviceArr = new ArrayList<>();
        for (var e : topDevices) {
            deviceArr.add(new Document("device", e.getKey())
                    .append("count", e.getValue()));
        }
        result.append("topDevices", deviceArr);

        result.append("topPlayers", topPlayers);
        result.append("logs", logs);

        resp.getWriter().write(result.toJson());
    }
}