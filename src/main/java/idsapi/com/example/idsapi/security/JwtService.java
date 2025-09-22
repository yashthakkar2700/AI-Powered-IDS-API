package idsapi.com.example.idsapi.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    private final String SECRET = "mysecretkeymysecretkeymysecretkey123"; // use env variable in real app
    private final Algorithm algorithm = Algorithm.HMAC256(SECRET);
    private final JWTVerifier verifier = JWT.require(algorithm).build();

    // Generate JWT
    public String generateToken(String username) {
        return JWT.create()
                .withSubject(username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hr
                .sign(algorithm);
    }

    public String validateTokenAndGetUsername(String token) {
        try {
            DecodedJWT decoded = verifier.verify(token);

            System.out.println("JWTService.validateTokenAndGetUsername(): Parsed username: " + decoded.getSubject());
            return decoded.getSubject();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    // Validate
    public boolean validateToken(String token, String username) {
        try {
            DecodedJWT decodedJWT = JWT.require(algorithm).build().verify(token);
            return decodedJWT.getSubject().equals(username) &&
                    decodedJWT.getExpiresAt().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
