package gp.wagner.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;

@SpringBootApplication

// JpaAuditing нужен для отслеживания времени изменения и создания некоторых сущностей
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
@ConditionalOnProperty(name = "scheduler.enabled", matchIfMissing = true)
public class BackEndGpApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(BackEndGpApplication.class);
	}

	public static void main(String[] args) {

		SpringApplication.run(BackEndGpApplication.class, args);

		try {
			String ip = InetAddress.getLoopbackAddress().getHostAddress();

			System.out.printf("\nCurrent Ip-address: %s", ip);
		} catch (Exception e) {

			System.out.println("Получить ip сервера не удалось");
		}

	}

}
