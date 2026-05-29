package tienda.api.reportes.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestHeadersInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                String role = attributes.getRequest().getHeader("X-User-Role");
                String userId = attributes.getRequest().getHeader("X-User-Id");
                
                if (role != null) requestTemplate.header("X-User-Role", role);
                if (userId != null) requestTemplate.header("X-User-Id", userId);
            }
        };
    }
}
