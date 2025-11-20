// Name: Yiming Fu
// Andrew ID: yimingfu

package com.project.api;

import java.io.IOException;
import org.bson.Document;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// This servlet displays analytics dashboard
public class DashboardServlet extends HttpServlet {
    private static final int playerLimit = 5;  // Limit for top players
    private static final int teamLimit = 3;  // Limit for top teams

    // Get method for DashboardServlet
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {
        // Fetch analytics result
        Document result = LogHelper.buildAnalyticsJson(playerLimit, teamLimit);
        // Send to JSP
        req.setAttribute("result", result);
        req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
    }
}