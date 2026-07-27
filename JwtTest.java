import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

public class JwtTest {
    public static void main(String[] args) {
        try {
            String secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            Key key = Keys.hmacShaKeyFor(keyBytes);
            System.out.println("Key generated successfully! Bytes length: " + keyBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
