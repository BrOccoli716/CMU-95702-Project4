// Name: Yiming Fu
// Andrew ID: yimingfu

package com.project.api;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

// This servlet write out analytics result in JSON style, which is used for TESTING
// For dashboard analytics result, refer to DashboardServlet
public class LogsDataServlet extends HttpServlet {
    private static final int playerLimit = 5;  // Limit for top players
    private static final int teamLimit = 3;  // Limit for top teams

    // Get method for LogsDataServlet
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        // Fetch logs from MongoDB
        Document result = LogHelper.buildAnalyticsJson(playerLimit, teamLimit);
        // Write out JSON analytics result
        resp.getWriter().write(result.toJson());
    }
}