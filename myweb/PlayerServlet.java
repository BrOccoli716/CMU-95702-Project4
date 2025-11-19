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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class Team {
    int id;
    String conference, division, city;
    String name, full_name, abbreviation;
    String imageUrl;
}

class Player {
    int id;
    String first_name, last_name;
    String position, jersey_number;
    String height, weight;
    String college, country;
    int draft_year, draft_round, draft_number;
    Team team;
    String imageUrl;
}

class Meta {
    public Integer next_cursor;
    public Integer per_page;
}

class PlayerResponse {
    public List<Player> data;
    public Meta meta;
}

public class PlayerServlet extends HttpServlet {
    private final String API_URL = "https://api.balldontlie.io/v1/players?per_page=100";
    private final String API_KEY = "6b6de5ab-779d-4100-bcb8-533fef7ee07e";
    private Map<String, String> imgUrlMap = new HashMap<>();

    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println("Servlet init: Loading player_image_html.txt...");
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("player_image_html.txt");
            if (is == null) throw new RuntimeException("player_image_html.txt Not Found!");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            List<String> lines = new ArrayList<>();
            String in_line;
            while ((in_line = reader.readLine()) != null) { lines.add(in_line); }
            System.out.println("Loaded txt (line count approx): finished");
            System.out.println("Read in " + lines.size() + " lines");
            for (String line: lines) {
                Document doc = Jsoup.parse(line);
                Elements images = doc.select("img");
                Elements texts = doc.select("tbody").select("tr");
                for (int i = 0; i < texts.size(); i++) {
                    Element image = images.get(i);
                    Element text = texts.get(i);
                    Elements values = text.select("td");
                    String name = values.get(0).text();
                    String team = values.get(1).text();
                    if (team.isEmpty()) {
                        team = "NAN";
                    }
                    String key = name.toLowerCase().replace(" ", "_") + "_" + team.toLowerCase();
                    imgUrlMap.put(key, image.attr("src"));
                }
            }
            System.out.println("Processed " + imgUrlMap.size() + " players");
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }

    private JsonObject fetchResponse(String urlString) {
        JsonObject result = new JsonObject();
        Gson gson = new Gson();
        try {
            URL url = new URI(urlString).toURL();
            System.out.println("Request URL: " + url);
            // Establish URL connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // Set up request headers
            conn.setRequestProperty("Authorization", API_KEY);
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            // Check status code
            int responseCode = conn.getResponseCode();
            result.addProperty("status", responseCode);
            System.out.println("Response Code: " + responseCode);
            if (responseCode == 401) {
                result.addProperty("error", "Unauthorized query!");
                return result;
            } else if (responseCode == 400) {
                result.addProperty("error", "Bad request! Check again!");
                return result;
            } else if (responseCode == 404) {
                result.addProperty("error", "Specified resources not found!");
                return result;
            } else if (responseCode == 406) {
                result.addProperty("error", "Resource not acceptable (not JSON)!");
                return result;
            } else if (responseCode == 429) {
                result.addProperty("error", "Too many request! API rate limit!");
                return result;
            } else if (responseCode == 500) {
                result.addProperty("error", "Internal server error for API!");
                return result;
            } else if (responseCode == 503) {
                result.addProperty("error", "API currently Unavailable!");
                return result;
            }
            // Read in response
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                json.append(line);
            }
            in.close();
            JsonElement body = JsonParser.parseString(json.toString());
            result.add("data", body);
            return result;
        } catch (URISyntaxException e) {
            result = new JsonObject();
            result.addProperty("status", 400);
            result.addProperty("error", "URI Syntax Exception: " + e.getMessage());
            return result;
        } catch (MalformedURLException e) {
            result = new JsonObject();
            result.addProperty("status", 400);
            result.addProperty("error", "Malformed URL Exception: " + e.getMessage());
            return result;
        } catch (IOException e) {
            result = new JsonObject();
            result.addProperty("status", 400);
            result.addProperty("error", "IO Exception: " + e.getMessage());
            return result;
        }
    }

    private String buildUrl(String firstName, String lastName, Integer next_cursor) {
        String urlString = API_URL;
        if (firstName != "" && firstName != null) { urlString += "&first_name=%s".formatted(firstName); }
        if (lastName != "" && lastName != null) { urlString += "&last_name=%s".formatted(lastName); }
        // System.out.println("firstName: " + firstName + "\nlastName: " + lastName);
        if (next_cursor != null) { urlString += "&cursor=%s".formatted(String.valueOf(next_cursor)); }
        return urlString;
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
        json = json.replace("first_name", "firstName");
        json = json.replace("last_name", "lastName");
        json = json.replace("jersey_number", "jerseyNumber");
        json = json.replace("draft_year", "draftYear");
        json = json.replace("draft_round", "draftRound");
        json = json.replace("draft_number", "draftNumber");
        json = json.replace("full_name", "fullName");
        json = json.replace("LA Clippers", "Los Angeles Clippers");
        return json;
    }

    private String convertHeight(String height) {
        if (height == null) { return "Unknown"; }
        String[] parts = height.split("-");
        if (parts.length != 2) return "Unknown";
        int feet = Integer.parseInt(parts[0]);
        int inches = Integer.parseInt(parts[1]);
        double cm = feet * 30.48 + inches * 2.54;
        String height_cm = String.format("(%.1f cm)", cm);
        return height.replace("-", "'") + "\" " + height_cm;
    }

    private String convertWeight(String weight) {
        if (weight == null) { return "Unknown"; }
        double lb = Double.parseDouble(weight);
        double kg = lb * 0.453592;
        String weight_kg = String.format("(%.1f kg)", kg);
        return weight + " lb " + weight_kg;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        // Start timer
        long startTime = System.currentTimeMillis();
        // Define latency
        long latency;
        // Read in parameters from request
        String firstName = req.getParameter("first_name");
        if (firstName != null) { firstName = firstName.replace(" ", "_").toLowerCase(); }
        String lastName = req.getParameter("last_name");
        if (lastName != null) { lastName = lastName.replace(" ", "_").toLowerCase(); }
        if ((firstName != null && firstName.length() > 20) || (lastName != null && lastName.length() > 20)) {
            sendError(res, "Query too long!");
            latency = System.currentTimeMillis() - startTime;
            LogHelper.logRequest(req, HttpServletResponse.SC_BAD_REQUEST, latency, 0);
            return;
        }
        Gson gson = new Gson();
        ArrayList<Player> players = new ArrayList<>();
        // Set up API node and URL
        String urlString = buildUrl(firstName, lastName, null);
        Integer cursor = null;
        int result_size = 0;
        while (true) {
            JsonObject result = fetchResponse(urlString);
            if (result.has("error")) {
                if (result.get("status").getAsInt() != 429) {
                    sendError(res, result.get("error").getAsString());
                    latency = System.currentTimeMillis() - startTime;
                    LogHelper.logRequest(req, HttpServletResponse.SC_BAD_REQUEST, latency, 0);
                    return;
                }
                break;  // for error 429 (this request exceeds limit, but others count)
            }
            PlayerResponse json = gson.fromJson(result.get("data"), PlayerResponse.class);
            if (json != null) { System.out.println("Fetched " + json.data.size() + " players"); }
            result_size += json.toString().length();
            for (Player p: json.data) { 
                String name = p.first_name.toLowerCase() + "_" + p.last_name.toLowerCase();
                // String team = p.teamName.toLowerCase();  // Used for TEST
                String team = p.team.abbreviation.toLowerCase();
                if (team.isEmpty()) {
                    team = "NAN";
                }
                String key = name.replace(" ", "_") + "_" + team;
                p.imageUrl = imgUrlMap.get(key);
                if (p.imageUrl == null) {
                    name = p.last_name.toLowerCase() + "_" + p.first_name.toLowerCase();
                    key = name.replace(" ", "_") + "_" + team;
                    p.imageUrl = imgUrlMap.get(key);
                }
                System.out.println(key + " " + p.imageUrl);
                p.height = convertHeight(p.height);
                p.weight = convertWeight(p.weight);
                players.add(p); 
                LogHelper.incrementPlayerCount(p.id, p.first_name + " " + p.last_name);
            }
            if (json.meta.next_cursor != null) {
                cursor = json.meta.next_cursor;
                urlString = buildUrl(firstName, lastName, cursor);
            } else {
                System.out.println("No more pages.");
                break;
            }
        }
        // response result to front-end
        sendSuccess(res, players);
        latency = System.currentTimeMillis() - startTime;
        LogHelper.logRequest(req, HttpServletResponse.SC_OK, latency, result_size);
    }
}