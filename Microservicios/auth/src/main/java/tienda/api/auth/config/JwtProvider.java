package tienda.api.auth.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import tienda.api.auth.model.Usuario;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    public String createToken(Usuario usuario, java.util.List<String> roles) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder()
                .setSubject(usuario.getEmail())
                .claim("userId", usuario.getId())
                .claim("role", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
