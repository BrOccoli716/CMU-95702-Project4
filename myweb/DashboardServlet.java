package com.project.api;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        out.println("<html><head><title>Operations Dashboard</title>");
        out.println("<style>");
        out.println("table, th, td { border:1px solid black; border-collapse:collapse; padding:6px; }");
        out.println("</style></head><body>");

        out.println("<h1>Operations Dashboard</h1>");

        out.println("<h2>API Analytics</h2>");
        out.println("<div id='apiFrequency'></div>");
        out.println("<div id='avgLatency'></div>");
        out.println("<div id='errorRate'></div>");
        out.println("<div id='deviceModels'></div>");
        out.println("<div id='topPlayers'></div>");

        out.println("<h2>Full Logs</h2>");
        out.println("<div id='logsTable'></div>");

        out.println("<script>");
        out.println("" +
                "fetch('/myweb/api/logs')\n" +
                "  .then(res => res.json())\n" +
                "  .then(data => buildTables(data));\n" +
                "\n" +
                "function buildTables(data) {\n" +

                "  // API Frequency\n" +
                "  let apiHTML = '<h3>API Request Frequency</h3><table><tr><th>API</th><th>Count</th></tr>';\n" +
                "  for (let api in data.apiFrequency) apiHTML += `<tr><td>${api}</td><td>${data.apiFrequency[api]}</td></tr>`;\n" +
                "  apiHTML += '</table>'; document.getElementById('apiFrequency').innerHTML = apiHTML;\n" +

                "  // Avg Latency\n" +
                "  let latHTML = '<h3>Average Latency (ms)</h3><table><tr><th>API</th><th>Latency</th></tr>';\n" +
                "  for (let api in data.avgLatency) latHTML += `<tr><td>${api}</td><td>${data.avgLatency[api].toFixed(2)}</td></tr>`;\n" +
                "  latHTML += '</table>'; document.getElementById('avgLatency').innerHTML = latHTML;\n" +

                "  // Error Rate\n" +
                "  let e = data.errorRate;\n" +
                "  let errHTML = '<h3>Error Rate</h3><table><tr><th>Status</th><th>Count</th></tr>' +\n" +
                "                `<tr><td>Success (200)</td><td>${e.success}</td></tr>` +\n" +
                "                `<tr><td>Bad Request (400)</td><td>${e.badRequest}</td></tr>` +\n" +
                "                '</table>';\n" +
                "  document.getElementById('errorRate').innerHTML = errHTML;\n" +

                "  // Top Devices\n" +
                "  let devHTML = '<h3>Top 5 Device Models</h3><table><tr><th>Device</th><th>Count</th></tr>';\n" +
                "  for (let d of data.topDevices) devHTML += `<tr><td>${d.device}</td><td>${d.count}</td></tr>`;\n" +
                "  devHTML += '</table>'; document.getElementById('deviceModels').innerHTML = devHTML;\n" +

                "  // Top Players\n" +
                "  let pHTML = '<h3>Top 5 Players</h3><table><tr><th>Player</th><th>Count</th></tr>';\n" +
                "  for (let p of data.topPlayers) pHTML += `<tr><td>${p.playerName}</td><td>${p.count}</td></tr>`;\n" +
                "  pHTML += '</table>'; document.getElementById('topPlayers').innerHTML = pHTML;\n" +

                "  // Full Logs\n" +
                "  let logsHTML = '<table><tr><th>Time</th><th>Path</th><th>Method</th><th>Device</th><th>Status</th><th>Latency</th></tr>';\n" +
                "  for (let l of data.logs) logsHTML += `<tr><td>${l.timestamp}</td><td>${l.path}</td><td>${l.method}</td><td>${l.deviceModel || ''}</td><td>${l.status}</td><td>${l.latencyMs}</td></tr>`;\n" +
                "  logsHTML += '</table>'; document.getElementById('logsTable').innerHTML = logsHTML;\n" +
                "}\n");

        out.println("</script></body></html>");
    }
}