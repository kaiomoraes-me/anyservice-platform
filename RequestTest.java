import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RequestTest {
    public static void main(String[] args) throws Exception {
        String json = "{\"name\":\"Joao Silva\",\"email\":\"joao@teste.com\",\"password\":\"123\",\"role\":\"CLIENT\"}";
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(new URI("http://localhost:8080/api/auth/register"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
            
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: " + response.body());
    }
}
