package tienda.api.inventario.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, RoleFilter roleFilter) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .addFilterBefore(roleFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/inventario/descuento").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN", "ROLE_BODEGUERO")
                .anyRequest().hasAnyAuthority("ROLE_BODEGUERO", "ROLE_ADMIN")
            );
        return http.build();
    }
}
