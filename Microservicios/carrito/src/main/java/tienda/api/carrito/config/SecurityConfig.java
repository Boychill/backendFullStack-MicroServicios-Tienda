package tienda.api.carrito.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                .requestMatchers("/error", "/swagger-ui/**", "/v3/api-docs/**", "/api/*/v3/api-docs/**", "/swagger-ui.html", "/webjars/**").permitAll()
                .anyRequest().hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
            );
        return http.build();
    }
}
