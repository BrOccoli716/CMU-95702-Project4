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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class Team {
    int id;
    String conference, division, city;
    String name, full_name, abbreviation;
}

class Player {
    int id;
    String first_name, last_name;
    String position, jersey_number;
    String height, weight;
    String college, country;
    int draft_year, draft_round, draft_number;
    Team team;
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
    private static final String API_URL = "https://api.balldontlie.io/v1/players";
    private static final String API_KEY = "6b6de5ab-779d-4100-bcb8-533fef7ee07e";
    private Map<String, String> imgUrlMap = new HashMap<>();

    private String fetchResponse(String urlString) throws IOException, URISyntaxException {
        URL url = new URI(urlString).toURL();
        System.out.println("Request URL: " + url);
        // Establish URL connection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        // Set up request headers
        conn.setRequestProperty("Authorization", "6b6de5ab-779d-4100-bcb8-533fef7ee07e");
        conn.setRequestProperty("Accept", "application/json");

        // Check status code
        int responseCode = conn.getResponseCode();
        System.out.println("Response Code: " + responseCode);
        // Read in response
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder json = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            json.append(line);
        }
        in.close();
        return json.toString();
    }
    
    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println("Servlet init: Loading image_html.txt...");
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("image_html.txt");
            if (is == null) throw new RuntimeException("image_html.txt Not Found!");
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

    private String buildUrl(String firstName, String lastName, Integer next_cursor) {
        String urlString = "https://api.balldontlie.io/v1/players?per_page=100";
        if (firstName != "" && firstName != null) { urlString += "&first_name=%s".formatted(firstName); }
        if (lastName != "" && lastName != null) { urlString += "&last_name=%s".formatted(lastName); }
        // System.out.println("firstName: " + firstName + "\nlastName: " + lastName);
        if (next_cursor != null) { urlString += "&cursor=%s".formatted(String.valueOf(next_cursor)); }
        return urlString;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        // Read in parameters from request
        String firstName = req.getParameter("first_name");
        String lastName = req.getParameter("last_name");
        Gson gson = new Gson();
        ArrayList<Player> players = new ArrayList<>();

        try {
            // Set up API node and URL
            String urlString = buildUrl(firstName, lastName, null);
            Integer cursor = null;
            while (true) {
                PlayerResponse json = gson.fromJson(fetchResponse(urlString), PlayerResponse.class);
                if (json != null) { System.out.println("Fetched " + json.data.size() + " players"); }
                for (Player p: json.data) { players.add(p); }
                if (json.meta.next_cursor != null) {
                    cursor = json.meta.next_cursor;
                    urlString = buildUrl(firstName, lastName, cursor);
                } else {
                    System.out.println("No more pages.");
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        // response result to front-end
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().print(gson.toJson(players));
    }
}