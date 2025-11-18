<%@ page import="org.bson.Document" %>
<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    Document result = (Document) request.getAttribute("result");
    if (result == null) {
        out.println("<h2>No analytics data available.</h2>");
        return;
    }

    Map<String, Integer> apiFreq = (Map<String, Integer>) result.get("apiFrequency");
    Map<String, Double> avgLatency = (Map<String, Double>) result.get("avgLatency");
    Document errorRate = result.get("errorRate", Document.class);
    List<Document> topDevices = (List<Document>) result.get("topDevices");
    List<Document> topPlayers = (List<Document>) result.get("topPlayers");
    List<Document> logs = (List<Document>) result.get("logs");
%>

<html>
<head>
    <title>Dashboard Analytics</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 30px;
        }
        h2 {
            margin-top: 40px;
        }
        table {
            width: 70%;
            border-collapse: collapse;
            margin-bottom: 30px;
        }
        table, th, td {
            border: 1px solid #888;
        }
        th, td {
            padding: 8px 12px;
            text-align: left;
        }
        th {
            background-color: #EEE;
        }
    </style>
</head>
<body>

<h1>Server Dashboard</h1>

<!-- API Frequency -->
<h2>API Request Frequency</h2>
<table>
    <tr><th>API</th><th>Count</th></tr>
    <%
        if (apiFreq != null) {
            for (String key : apiFreq.keySet()) {
                <tr>
                    <td><%= api %></td>
                    <td><%= apiFreq.get(api) %></td>
                </tr>
            }
        }
    %>
</table>

<!-- Average Latency -->
<h2>Average Latency (ms)</h2>
<table>
    <tr><th>API</th><th>Average Latency</th></tr>
    <%
        if (avgLatency != null) {
            for (String key : avgLatency.keySet()) {
                <tr>
                    <td><%= api %></td>
                    <td><%= avgLatency.get(api) %></td>
                </tr>
            }
        }
    %>
</table>

<!-- Error Rate -->
<h2>Error Rate</h2>
<p>Success: <%= errorRate.getInteger("success") %></p>
<p>Bad Request: <%= errorRate.getInteger("badRequest") %></p>

<!-- Top Devices -->
<h2>Top Devices</h2>
<table>
    <tr><th>Device</th><th>Count</th></tr>
    <%
        if (topDevices != null) {
            for (Document d : topDevices) {
                out.println("<tr><td>" + d.getString("device") + "</td><td>" + d.getInteger("count") + "</td></tr>");
            }
        }
    %>
</table>

<!-- Top Players -->
<h2>Top Searched Players</h2>
<table>
    <tr><th>Player Name</th><th>Search Count</th></tr>
    <%
        if (topPlayers != null) {
            for (Document p : topPlayers) {
                out.println("<tr><td>" + p.getString("first_name") + " " + p.getString("last_name") + "</td><td>" + p.getInteger("count") + "</td></tr>");
            }
        }
    %>
</table>

<!-- Logs -->
<h2>All Logs</h2>
<table style="width:90%;">
    <tr>
        <th>Time</th>
        <th>API</th>
        <th>Device</th>
        <th>Latency (ms)</th>
        <th>Status</th>
    </tr>
    <%
        if (logs != null) {
            for (Document log : logs) {
                out.println("<tr>");
                out.println("<td>" + log.getDate("timestamp") + "</td>");
                out.println("<td>" + log.getDate("clientIP") + "</td>");
                out.println("<td>" + log.getString("deviceModel") + "</td>");
                out.println("<td>" + log.getString("requestPath") + "</td>");
                out.println("<td>" + log.getLong("requestParams") + "</td>");
                out.println("<td>" + log.getInteger("thirdPartyLatency") + "</td>");
                out.println("<td>" + log.getDate("statusCode") + "</td>");
                out.println("<td>" + log.getDate("responseSize") + "</td>");
                out.println("</tr>");
            }
        }
    %>
</table>

</body>
</html>