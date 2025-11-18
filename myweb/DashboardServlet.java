package com.project.api;

import java.io.IOException;
import org.bson.Document;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DashboardServlet extends HttpServlet {
    private static final int playerLimit = 5;
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {
        Document result = LogHelper.buildAnalyticsJson(playerLimit);
        // Send to JSP
        req.setAttribute("result", result);
        req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
    }
}