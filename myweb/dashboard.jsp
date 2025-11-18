<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<head>
    <title>Operations Dashboard</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; }
        h1 { font-size: 36px; }
        h2 { margin-top: 40px; }
        table { border-collapse: collapse; width: 100%; margin-top: 10px; }
        th, td { border: 1px solid #ccc; padding: 8px; }
        th { background-color: #eee; }
        .section { margin-bottom: 80px; }
    </style>
</head>

<body>

<h1>Operations Dashboard</h1>

<div class="section">
    <h2>API Request Frequency</h2>
    <table>
        <tr><th>API Path</th><th>Count</th></tr>
        <c:forEach var="entry" items="${apiFrequency}">
            <tr><td>${entry.key}</td><td>${entry.value}</td></tr>
        </c:forEach>
    </table>
</div>

<div class="section">
    <h2>Average API Latency</h2>
    <table>
        <tr><th>API Path</th><th>Latency (ms)</th></tr>
        <c:forEach var="entry" items="${avgLatency}">
            <tr><td>${entry.key}</td><td>${entry.value}</td></tr>
        </c:forEach>
    </table>
</div>

<div class="section">
    <h2>Error Rate</h2>
    <table>
        <tr><th>Success</th><th>Bad Request</th></tr>
        <tr>
            <td>${errorRate.success}</td>
            <td>${errorRate.badRequest}</td>
        </tr>
    </table>
</div>

<div class="section">
    <h2>Top Device Models</h2>
    <table>
        <tr><th>Device</th><th>Count</th></tr>
        <c:forEach var="dev" items="${topDevices}">
            <tr>
                <td>${dev.device}</td>
                <td>${dev.count}</td>
            </tr>
        </c:forEach>
    </table>
</div>

<div class="section">
    <h2>Top Searched Players</h2>
    <ul>
        <c:forEach var="p" items="${topPlayers}">
            <li>${p}</li>
        </c:forEach>
    </ul>
</div>

<div class="section">
    <h2>Full Logs</h2>
    <table>
        <tr><th>Time</th><th>Query</th><th>Path</th><th>Latency</th></tr>
        <c:forEach var="log" items="${logs}">
            <tr>
                <td>${log.timestamp}</td>
                <td>${log.query}</td>
                <td>${log.path}</td>
                <td>${log.latencyMs}</td>
            </tr>
        </c:forEach>
    </table>
</div>

</body>
</html>