package GoLink_Backend.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {
    @Value("${jwt.secret:default-secret-key-that-is-very-long-and-secure-for-golink-application-123456}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 hours
    private long expirationMs;

    public String generateToken(String email) {
        return JWT.create()
                .withSubject(email)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationMs))
                .sign(Algorithm.HMAC256(secret));
    }

    public String getEmailFromToken(String token) {
        DecodedJWT jwt = JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token);
        return jwt.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
