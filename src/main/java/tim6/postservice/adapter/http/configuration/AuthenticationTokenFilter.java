package tim6.postservice.adapter.http.configuration;

import java.io.IOException;
import java.util.Arrays;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

public class AuthenticationTokenFilter extends UsernamePasswordAuthenticationFilter {

    private final UserDetailsService userDetailsService;

    private final TokenUtilities tokenUtilities;

    @Autowired
    public AuthenticationTokenFilter(
            final UserDetailsService userDetailsService, final TokenUtilities tokenUtilities) {
        this.userDetailsService = userDetailsService;
        this.tokenUtilities = tokenUtilities;
    }

    @Override
    public void doFilter(
            final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        final String token;
        if (httpServletRequest.getCookies() != null) {
            token = AuthenticationTokenFilter.getTokenFromCookie(httpServletRequest);
        } else {
            token = AuthenticationTokenFilter.getTokenFromHeader(httpServletRequest);
        }

        if (token == null) {
            chain.doFilter(request, response);
            return;
        }

        final String userIdFromToken = this.tokenUtilities.getUserIdFromToken(token);
        if (userIdFromToken != null
                || SecurityContextHolder.getContext().getAuthentication() == null) {
            final UserDetails userDetails =
                    this.userDetailsService.loadUserByUsername(userIdFromToken);
            if (this.tokenUtilities.validateToken(token)) {
                final UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, userIdFromToken, userDetails.getAuthorities());
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        chain.doFilter(request, response);
    }

    private static String getTokenFromHeader(final HttpServletRequest httpServletRequest) {
        String token;
        token = httpServletRequest.getHeader("Authorization");
        if (token != null) {
            token = token.substring("Bearer ".length());
        }
        return token;
    }

    private static String getTokenFromCookie(final HttpServletRequest httpServletRequest) {
        final String token;
        token =
                Arrays.stream(httpServletRequest.getCookies())
                        .filter(c -> c.getName().equals("token"))
                        .findFirst()
                        .map(Cookie::getValue)
                        .orElse(null);
        return token;
    }
}
