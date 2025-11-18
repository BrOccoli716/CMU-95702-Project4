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
        Document result = LogHelper.buildAnalyticsJson(playerLimit);
        resp.getWriter().write(result.toJson());
    }
}