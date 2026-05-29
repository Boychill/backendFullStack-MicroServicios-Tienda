package tienda.api.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions.lb;
import org.springframework.web.servlet.function.RequestPredicates;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouterFunction<ServerResponse> customRoutes() {
        return route("auth-service")
            .route(RequestPredicates.path("/api/auth/**"), http())
            .filter(lb("AUTH"))
            .build()
            .and(route("catalogo-service")
                .route(RequestPredicates.path("/api/productos/**"), http())
                .filter(lb("CATALOGO"))
                .build())
            .and(route("inventario-service")
                .route(RequestPredicates.path("/api/inventario/**"), http())
                .filter(lb("INVENTARIO"))
                .build())
            .and(route("carrito-service")
                .route(RequestPredicates.path("/api/carrito/**"), http())
                .filter(lb("CARRITO"))
                .build())
            .and(route("pagos-service")
                .route(RequestPredicates.path("/api/pagos/**"), http())
                .filter(lb("PAGOS"))
                .build())
            .and(route("pedidos-service")
                .route(RequestPredicates.path("/api/pedidos/**"), http())
                .filter(lb("PEDIDOS"))
                .build())
            .and(route("perfil-service")
                .route(RequestPredicates.path("/api/perfiles/**"), http())
                .filter(lb("AUTH"))
                .build())
            .and(route("logistica-service")
                .route(RequestPredicates.path("/api/logistica/**"), http())
                .filter(lb("LOGISTICA"))
                .build())
            .and(route("notificaciones-service")
                .route(RequestPredicates.path("/api/notificaciones/**"), http())
                .filter(lb("NOTIFICACIONES"))
                .build())
            .and(route("reportes-service")
                .route(RequestPredicates.path("/api/reportes/**"), http())
                .filter(lb("REPORTES"))
                .build());

    }
}
