package tim6.postservice.helpers;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AuthHelper {

    public static String createAuthToken(final String userId, final String jwtSecret) {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userId);
        claims.put("created", new Date(System.currentTimeMillis()));
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + 3600 * 1000))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }
}
