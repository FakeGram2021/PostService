package tim6.postservice.adapter.http.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TokenUtilities {

    private final String secret;

    @Autowired
    public TokenUtilities(@Value("${jwt.secret}") final String secret) {
        this.secret = secret;
    }

    public String getUserIdFromToken(final String token) {
        try {
            final Claims claims = this.getClaimsFromToken(token);
            return claims.getSubject();
        } catch (final Exception ex) {
            return null;
        }
    }

    public Date getExpirationDateFromToken(final String token) {
        try {
            final Claims claims = this.getClaimsFromToken(token);
            return claims.getExpiration();
        } catch (final Exception ex) {
            return null;
        }
    }

    public boolean validateToken(final String token) {
        final String userId = this.getUserIdFromToken(token);
        return userId != null && !this.isTokenExpired(token);
    }

    private Claims getClaimsFromToken(final String token) {
        final JwtParser parser = Jwts.parser().setSigningKey(this.secret);
        return parser.parseClaimsJws(token).getBody();
    }

    private boolean isTokenExpired(final String token) {
        final Date expirationDate = this.getExpirationDateFromToken(token);
        return expirationDate != null && expirationDate.before(new Date());
    }
}
