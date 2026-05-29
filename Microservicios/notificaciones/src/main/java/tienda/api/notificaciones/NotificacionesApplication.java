package tienda.api.notificaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@EnableFeignClients
public class NotificacionesApplication {

    public static ApplicationContext context;

	public static void main(String[] args) {
		context = SpringApplication.run(NotificacionesApplication.class, args);
	}

}
