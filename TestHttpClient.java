import org.springframework.web.client.RestClient;
import java.util.Map;

public class TestHttpClient {
    public static void main(String[] args) {
        RestClient restClient = RestClient.builder().baseUrl("http://localhost:8000").build();
        try {
            Map response = restClient.get().uri("/api/v1/collections/eric-evans").retrieve().body(Map.class);
            System.out.println("GET SUCCESS: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
