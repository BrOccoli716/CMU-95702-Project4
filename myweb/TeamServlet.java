// Name: Yiming Fu
// Andrew ID: yimingfu

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

// Servlet for Team
public class TeamServlet extends HttpServlet {
    private final String API_URL = "https://api.balldontlie.io/v1/teams";
    private final String API_KEY = "6b6de5ab-779d-4100-bcb8-533fef7ee07e";
    private long apiLatency;  // Latency for requesting all teams from third party API
    private final TeamServiceModel model = new TeamServiceModel();

    @Override
    public void init() throws ServletException {
        super.init();
        model.buildImageUrlMap();
        apiLatency = model.loadTeams(API_URL, API_KEY);
    }

    // Send error repsonse to mobile app
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

    // Get method for TeamServlet
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        long start = System.currentTimeMillis();
        long latency;  // Define the latency
        // Parse query paramters
        String query = req.getParameter("team");
        // Default: search for all teams
        if (query == null || query.isEmpty()) {
            List<Document> allTeams = model.getAllTeams();
            sendSuccess(resp, allTeams);
            latency = System.currentTimeMillis() - start + apiLatency;
            // Write to query logs
            LogHelper.logRequest(req, HttpServletResponse.SC_OK, latency, allTeams.size());
            for (Document team: allTeams) {
                // Write to team logs
                LogHelper.incrementTeamCount(team.getInteger("id"), team.getString("full_name"));
            }
            return;
        }
        // If query is too long (error)
        if (query != null && query.length() > 30) {
            sendError(resp, "Query too long");
            latency = System.currentTimeMillis() - start + apiLatency;
            // Write to query logs
            LogHelper.logRequest(req, HttpServletResponse.SC_BAD_REQUEST, latency, 0);
            return;
        }

        query = query.toLowerCase();
        List<Document> matches = model.findMatchedTeams(query);
        for (Document team : matches) {
            // Write to team logs
            LogHelper.incrementTeamCount(team.getInteger("id"), team.getString("full_name"));
        }
        // Write response with matching teams
        sendSuccess(resp, matches);
        latency = System.currentTimeMillis() - start + apiLatency;
        // Write to query logs
        LogHelper.logRequest(req, HttpServletResponse.SC_OK, latency, matches.size());
    }
}