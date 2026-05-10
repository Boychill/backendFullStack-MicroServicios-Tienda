package tienda.api.apigateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;
import java.util.List;

@Component
public class AuthFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secret;

    private final List<String> openEndpoints = List.of(
            "/api/auth/login",
            "/api/auth/register"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();

        boolean isSecured = openEndpoints.stream().noneMatch(path::contains);

        if (isSecured) {
            if(request.getMethod().equals("GET") && path.contains("/api/productos")) {
                 filterChain.doFilter(request, response);
                 return;
            }

            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }

            authHeader = authHeader.substring(7);

            try {
                Key key = Keys.hmacShaKeyFor(secret.getBytes());
                Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authHeader).getBody();
                List<String> roles = claims.get("role", List.class);
                String roleStr = String.join(",", roles);
                
                HeaderMapRequestWrapper wrapper = new HeaderMapRequestWrapper(request);
                wrapper.addHeader("X-User-Role", roleStr);
                String email = claims.getSubject();
                if(email != null) {
                    wrapper.addHeader("X-User-Email", email);
                }

                filterChain.doFilter(wrapper, response);
                return;
            } catch (Exception e) {
                if(request.getMethod().equals("GET") && path.contains("/api/productos")) {
                     filterChain.doFilter(request, response);
                     return;
                }
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
