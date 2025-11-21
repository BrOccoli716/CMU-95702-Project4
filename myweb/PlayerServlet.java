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


// Define Team class
class Team {
    int id;
    String conference, division, city;
    String name, full_name, abbreviation;
    String imageUrl;
}

// Define Player Class
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

// Define Meta class for third party API response
class Meta {
    public Integer next_cursor;
    public Integer per_page;
}

// Define PlayerResponse class for third party API response
class PlayerResponse {
    public List<Player> data;
    public Meta meta;
}

// Servlet for Player
public class PlayerServlet extends HttpServlet {
    private final String API_URL = "https://api.balldontlie.io/v1/players?per_page=100";
    private final String API_KEY = "6b6de5ab-779d-4100-bcb8-533fef7ee07e";
    private final PlayerServiceModel model = new PlayerServiceModel();

    @Override
    public void init() throws ServletException {
        super.init();
        model.loadPlayerImages();
    }

    // Send error response to mobile app
    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        JsonObject json = new JsonObject();
        json.addProperty("error", message);
        response.getWriter().write(json.toString());
    }

    // Send success response to mobile app
    private void sendSuccess(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);  // 200
        JsonObject wrapper = new JsonObject();
        wrapper.add("data", new Gson().toJsonTree(data));
        PrintWriter out = response.getWriter();
        out.write(model.repairJson(wrapper.toString()));
        out.flush();
    }

    // Get method for PlayerServlet
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
        // If query is too long (error)
        if ((firstName != null && firstName.length() > 20) || (lastName != null && lastName.length() > 20)) {
            sendError(res, "Query too long!");
            latency = System.currentTimeMillis() - startTime;
            // Write to query logs
            LogHelper.logRequest(req, HttpServletResponse.SC_BAD_REQUEST, latency, 0);
            return;
        }
        Gson gson = new Gson();
        ArrayList<Player> players = new ArrayList<>();
        // Set up API node and URL
        String urlString = model.buildUrl(firstName, lastName, null, API_URL);
        Integer cursor = null;
        int result_size = 0;
        while (true) {
            // Fetch response from third party API
            JsonObject result = model.fetchResponse(urlString, API_KEY);
            // Deal for error status code
            if (result.has("error")) {
                if (result.get("status").getAsInt() != 429) {
                    sendError(res, result.get("error").getAsString());
                    latency = System.currentTimeMillis() - startTime;
                    LogHelper.logRequest(req, HttpServletResponse.SC_BAD_REQUEST, latency, 0);
                    return;
                }
                break;  // for error 429 (this request exceeds limit, but others count)
            }
            // Extract player information from response
            PlayerResponse json = gson.fromJson(result.get("data"), PlayerResponse.class);
            if (json != null) { System.out.println("Fetched " + json.data.size() + " players"); }
            result_size += json.toString().length();
            for (Player p: json.data) { 
                p = model.extractPlayerInformation(p);
                players.add(p); 
                // Write to player logs
                LogHelper.incrementPlayerCount(p.id, p.first_name + " " + p.last_name);
            }
            // If there is more players to fetch
            if (json.meta.next_cursor != null) {
                cursor = json.meta.next_cursor;
                urlString = model.buildUrl(firstName, lastName, cursor, API_URL);
            } else {
                System.out.println("No more pages.");
                break;
            }
        }
        // Write response with returned players
        sendSuccess(res, players);
        latency = System.currentTimeMillis() - startTime;
        // Write to query logs
        LogHelper.logRequest(req, HttpServletResponse.SC_OK, latency, result_size);
    }
}