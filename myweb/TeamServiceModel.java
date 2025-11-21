// Name: Yiming Fu
// Andrew ID: yimingfu

package com.project.api;

import java.net.*;
import java.io.*;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.annotation.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.google.gson.JsonArray;

public class TeamServiceModel {
    private Map<String, String> imgUrlMap;
    private List<Document> allTeams;

    public TeamServiceModel() { 
        imgUrlMap = new HashMap<>(); 
        allTeams = new ArrayList<>();
    }

    // Extract team image url for each team
    public void buildImageUrlMap() {
        System.out.println("Servlet init: Loading team_image_html.txt...");
        try {
            // Read in txt file
            InputStream is = getClass().getClassLoader().getResourceAsStream("team_image_html.txt");
            if (is == null) throw new RuntimeException("team_image_html.txt Not Found!");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            List<String> lines = new ArrayList<>();
            String in_line;
            while ((in_line = reader.readLine()) != null) { lines.add(in_line); }
            System.out.println("Loaded txt (line count approx): finished");
            System.out.println("Read in " + lines.size() + " lines");
            for (String line: lines) {
                // Parse HTML scripts
                org.jsoup.nodes.Document doc = Jsoup.parse(line);
                Elements images = doc.select("img");
                Elements names = doc.select("div.title");
                for (int i = 0; i < names.size(); i++) {
                    // Extract team name and image url
                    Element image = images.get(i);
                    Element name = names.get(i);
                    imgUrlMap.put(name.text().toLowerCase().replace(" logo", ""), image.attr("src"));
                }
            }
            System.out.println("Processed " + imgUrlMap.size() + " teams");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Fetch all teams' information from third party API
    // Because third party API doesn't support query team by name, so I fetched all information initially, and respond to mobile by selecting matching teams
    public long loadTeams(String API_URL, String API_KEY) {
        long apiLatency = 0;
        try {
            // Set up connection to third party API
            long start = System.currentTimeMillis();
            URL apiUrl = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", API_KEY);
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            // Read in response
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
                // Only keep the first 30 teams
                if (teamJson.get("id").getAsInt() > 30) { continue; }
                String full_name = teamJson.get("full_name").getAsString();
                // Special cases
                if (full_name.equals("LA Clippers")) { full_name = "Los Angeles Clippers"; }
                String city = teamJson.get("city").getAsString();
                if (city.equals("LA")) { city = "Los Angeles"; }
                // Extract team information
                Document team = new Document("id", teamJson.get("id").getAsInt())
                        .append("full_name", full_name)
                        .append("name", teamJson.get("name").getAsString())
                        .append("city", city)
                        .append("division", teamJson.get("division").getAsString())
                        .append("conference", teamJson.get("conference").getAsString())
                        .append("abbreviation", teamJson.get("abbreviation").getAsString())
                        .append("imageUrl", imgUrlMap.get(full_name.toLowerCase()));
                allTeams.add(team);
            }
            // Calculate latency
            apiLatency = System.currentTimeMillis() - start;
            System.out.println("Loaded " + allTeams.size() + " teams!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apiLatency;
    }

    // Special case
    public String repairJson(String json) {
        json = json.replace("full_name", "fullName");
        json = json.replace("LA Clippers", "Los Angeles Clippers");
        return json;
    }

    // Get all teams
    public List<Document> getAllTeams() {
        return allTeams;
    }

    // Find all matched teams with given query
    public List<Document> findMatchedTeams(String query) {
        List<Document> matches = new ArrayList<>();
        for (Document team : allTeams) {
            // Fetch matching teams
            if (team.getString("full_name").toLowerCase().contains(query) ||
                team.getString("name").toLowerCase().contains(query) ||
                team.getString("city").toLowerCase().contains(query)) {
                matches.add(team);
            }
        }
        return matches;
    }
}