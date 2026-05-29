package tienda.api.reportes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ReportesApplication {

    public static org.springframework.context.ApplicationContext context;

	public static void main(String[] args) {
		context = SpringApplication.run(ReportesApplication.class, args);
	}

}
