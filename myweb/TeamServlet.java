package com.project.api;

import java.net.*;
import java.io.*;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.annotation.*;

import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;


public class TeamServlet extends HttpServlet {
    private final String API_URL = "https://api.balldontlie.io/v1/teams";
    private final String API_KEY = "6b6de5ab-779d-4100-bcb8-533fef7ee07e";
    private long apiLatency;
    private Map<String, String> imageUrlMap = new HashMap<>();
    private List<Document> allTeams = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        super.init();
        buildImageUrlMap();
        loadTeams();
    }

    private void buildImageUrlMap() {
        System.out.println("Servlet init: Loading team_image_html.txt...");
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("team_image_html.txt");
            if (is == null) throw new RuntimeException("team_image_html.txt Not Found!");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            List<String> lines = new ArrayList<>();
            String in_line;
            while ((in_line = reader.readLine()) != null) { lines.add(in_line); }
            System.out.println("Loaded txt (line count approx): finished");
            System.out.println("Read in " + lines.size() + " lines");
            for (String line: lines) {
                org.jsoup.nodes.Document doc = Jsoup.parse(line);
                Elements images = doc.select("img");
                Elements names = doc.select("div.title");
                for (int i = 0; i < names.size(); i++) {
                    Element image = images.get(i);
                    Element name = names.get(i);
                    imageUrlMap.put(name.text().toLowerCase().replace(" logo", ""), image.attr("src"));
                }
            }
            System.out.println("Processed " + imageUrlMap.size() + " teams");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTeams() {
        try {
            long start = System.currentTimeMillis();
            URL apiUrl = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", API_KEY);
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder json = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) json.append(line);
            in.close();

            // Parse JSON
            Gson gson = new Gson();
            JsonObject root = gson.fromJson(json.toString(), JsonObject.class);
            JsonArray data = root.getAsJsonArray("data");
            for (JsonElement e : data) {
                JsonObject teamJson = e.getAsJsonObject();
                if (teamJson.get("id").getAsInt() > 30) { continue; }
                String full_name = teamJson.get("full_name").getAsString();
                if (full_name.equals("LA Clippers")) { full_name = "Los Angeles Clippers"; }
                String city = teamJson.get("city").getAsString();
                if (city.equals("LA")) { city = "Los Angeles"; }
                Document team = new Document("id", teamJson.get("id").getAsInt())
                        .append("full_name", full_name)
                        .append("name", teamJson.get("name").getAsString())
                        .append("city", city)
                        .append("division", teamJson.get("division").getAsString())
                        .append("conference", teamJson.get("conference").getAsString())
                        .append("abbreviation", teamJson.get("abbreviation").getAsString())
                        .append("imageUrl", imageUrlMap.get(full_name.toLowerCase()));
                allTeams.add(team);
            }
            apiLatency = System.currentTimeMillis() - start;
            System.out.println("Loaded " + allTeams.size() + " teams!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        JsonObject json = new JsonObject();
        json.addProperty("error", message);
        response.getWriter().write(json.toString());
    }

    private void sendSuccess(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);  // 200
        JsonObject wrapper = new JsonObject();
        wrapper.add("data", new Gson().toJsonTree(data));
        PrintWriter out = response.getWriter();
        out.write(repairJson(wrapper.toString()));
        out.flush();
    }

    private String repairJson(String json) {
        json = json.replace("full_name", "fullName");
        json = json.replace("LA Clippers", "Los Angeles Clippers");
        return json;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        long start = System.currentTimeMillis();
        long latency;
        String query = req.getParameter("team");
        if (query == null || query.isEmpty()) {
            sendSuccess(resp, allTeams);
            latency = System.currentTimeMillis() - start + apiLatency;
            LogHelper.logRequest(req, HttpServletResponse.SC_OK, latency, allTeams.size());
            return;
        }
        if (query != null && query.length() > 30) {
            sendError(resp, "Query too long");
            latency = System.currentTimeMillis() - start + apiLatency;
            LogHelper.logRequest(req, HttpServletResponse.SC_BAD_REQUEST, latency, 0);
            return;
        }
    
        query = query.toLowerCase();
        List<Document> matches = new ArrayList<>();
        for (Document team : allTeams) {
            if (team.getString("full_name").toLowerCase().contains(query) ||
                team.getString("name").toLowerCase().contains(query) ||
                team.getString("city").toLowerCase().contains(query)) {
                matches.add(team);
            }
        }
        sendSuccess(resp, matches);
        latency = System.currentTimeMillis() - start + apiLatency;
        LogHelper.logRequest(req, HttpServletResponse.SC_OK, latency, matches.size());
    }
}