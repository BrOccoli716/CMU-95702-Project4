import com.mongodb.client.*;
import org.bson.Document;
import java.util.Scanner;

public class MongoTest {
    public static void main(String[] args) {
        String uri = "mongodb+srv://yimingfu:mJt6njGpLAniRJa7@cluster0.b5tx8sw.mongodb.net/?appName=Cluster0";

        // Establish client connection
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase db = mongoClient.getDatabase("testdb");
            MongoCollection<Document> collection = db.getCollection("test");

            // Read in user input
            Scanner sc = new Scanner(System.in);
            System.out.print("Enter a string to store in MongoDB: ");
            String input = sc.nextLine();

            // Write to MongoDB
            Document doc = new Document("text", input);
            collection.insertOne(doc);
            System.out.println("Inserted: " + input);

            // Read all documents
            System.out.println("\nCurrent documents in collection:");
            for (Document d : collection.find()) {
                System.out.println("- " + d.getString("text"));
            }

            // Close connection
            mongoClient.close();
        }
        System.out.println("MongoDB disconnected suceessfully.");
    }
}