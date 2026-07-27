package com.anyservice.backend;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import java.security.Key;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JwtServiceTest {
    @Test
    public void testKeyGeneration() {
        String secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        Key key = Keys.hmacShaKeyFor(keyBytes);
        assertNotNull(key);
    }
}
