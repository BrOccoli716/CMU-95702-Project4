package com.project.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        // 调用你的 LogsDataServlet 所返回的 JSON
        String logsUrl = "https://reimagined-space-garbanzo-66x5755q6wr35rrg-8080.app.github.dev/api/logs";

        URL url = new URL(logsUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder jsonSB = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) jsonSB.append(line);
        reader.close();
        String raw = jsonSB.toString();

        // Parse JSON
        Gson gson = new Gson();
        String json = gson.fromJson(raw, String.class);
        JsonObject root = gson.fromJson(json, JsonObject.class);

        // 传给 JSP 的对象
        req.setAttribute("apiFrequency", gson.fromJson(root.get("apiFrequency"), Map.class));
        req.setAttribute("avgLatency", gson.fromJson(root.get("avgLatency"), Map.class));
        req.setAttribute("errorRate", gson.fromJson(root.get("errorRate"), Map.class));

        req.setAttribute("topDevices",
                gson.fromJson(root.get("topDevices"), List.class));

        req.setAttribute("topPlayers",
                gson.fromJson(root.get("topPlayers"), List.class));

        req.setAttribute("logs",
                gson.fromJson(root.get("logs"), List.class));

        // forward 给 JSP
        req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
    }
}