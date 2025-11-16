import java.net.*;
import java.io.*;
import com.google.gson.Gson;
import java.util.List;
import java.util.ArrayList;

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
    public String toString() {
        return "ID: " + id + " Name: " + first_name + " " + "last_name";
    }
}

class Meta {
    public Integer next_cursor;
    public Integer per_page;
}

class PlayerResponse {
    public List<Player> data;
    public Meta meta;
}

public class APITest {
    private static String fetchResponse(String urlString) throws IOException, URISyntaxException {
        URL url = new URI(urlString).toURL();
        System.out.println("Request URL: " + url);
        // Establish URL connection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        // Set up request headers
        conn.setRequestProperty("Authorization", "6b6de5ab-779d-4100-bcb8-533fef7ee07e");
        conn.setRequestProperty("Accept", "application/json");

        // Check status code
        int responseCode = conn.getResponseCode();
        System.out.println("Response Code: " + responseCode);
        // Read in response
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder json = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            json.append(line);
        }
        in.close();
        return json.toString();
    }
    private static String buildUrl(String firstName, String lastName, Integer next_cursor) {
        String urlString = "https://api.balldontlie.io/v1/players?per_page=100";
        if (firstName != "") { urlString += "&first_name=%s".formatted(firstName); }
        if (lastName != "") { urlString += "&last_name=%s".formatted(lastName); }
        if (next_cursor != null) { urlString += "&cursor=%s".formatted(String.valueOf(next_cursor)); }
        return urlString;
    }
    public static ArrayList<Player> searchPlayer(String firstName, String lastName) {
        try {
            // Set up API node and URL
            String urlString = buildUrl(firstName, lastName, null);
            Gson gson = new Gson();
            Integer cursor = null;
            ArrayList<Player> players = new ArrayList<>();
            while (true) {
                PlayerResponse json = gson.fromJson(fetchResponse(urlString), PlayerResponse.class);
                if (json != null) { System.out.println("Fetched " + json.data.size() + " players"); }
                for (Player p: json.data) { players.add(p); }
                if (json.meta.next_cursor != null) {
                    cursor = json.meta.next_cursor;
                    urlString = buildUrl(firstName, lastName, cursor);
                } else {
                    System.out.println("No more pages.");
                    break;
                }
            }
            return players;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
    public static void main(String[] args) {
        Gson gson = new Gson();
        System.out.println(gson.toJson(searchPlayer("lebron", "")));
    }
}