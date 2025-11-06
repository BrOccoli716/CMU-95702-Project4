import java.net.*;
import java.io.*;
import com.google.gson.Gson;

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

public class Test {
    public static Player searchPlayer(String firstName, String lastName) {
        try {
            // Set up API node and URL
            String urlString = "https://api.balldontlie.io/v1/players";
            if ((firstName != null) && (lastName != null)) {
                urlString = "https://api.balldontlie.io/v1/players?first_name=%s&last_name=%s".formatted(firstName, lastName);
            } else if ((firstName == null) && (lastName != null)) {
                urlString = "https://api.balldontlie.io/v1/players?last_name=%s".formatted(lastName);
            } else if ((firstName != null) && (lastName == null)) {
                urlString = "https://api.balldontlie.io/v1/players?first_name=%s".formatted(firstName);
            }
            
            URL url = new URI(urlString).toURL();
            // Establish URL connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // Set up request headers
            conn.setRequestProperty("Authorization", "6b6de5ab-779d-4100-bcb8-533fef7ee07e");  // 替换为你的真实 API Key
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
            // Parse JSON
            Gson gson = new Gson();
            System.out.println(json.toString());
            Player response = gson.fromJson(json.toString(), Player.class);
            return response;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
    public static void main(String[] args) {
        System.out.println(searchPlayer("lebron", "james"));
    }
}