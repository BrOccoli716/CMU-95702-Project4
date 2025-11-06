package com.project.api;

import java.net.*;
import java.io.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/api/player")
public class PlayerServlet extends HttpServlet {
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

    private static final String API_URL = "https://api.balldontlie.io/v1/players";
    private static final String API_KEY = "6b6de5ab-779d-4100-bcb8-533fef7ee07e";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        // 从请求中读取参数 first_name
        String firstName = req.getParameter("first_name");
        String lastName = req.getParameter("last_name");

        // 拼接完整的 API URL
        String urlString = "https://api.balldontlie.io/v1/players";
        if ((firstName != null) && (lastName != null)) {
            urlString = "https://api.balldontlie.io/v1/players?first_name=%s&last_name=%s".formatted(firstName, lastName);
        } else if ((firstName == null) && (lastName != null)) {
            urlString = "https://api.balldontlie.io/v1/players?last_name=%s".formatted(lastName);
        } else if ((firstName != null) && (lastName == null)) {
            urlString = "https://api.balldontlie.io/v1/players?first_name=%s".formatted(firstName);
        }

        // 发送请求到 balldontlie API
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", API_KEY);

        // Read in response
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder json = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            json.append(line);
        }
        in.close();

        // 返回结果给前端
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().print(json.toString());
    }
}