package gp.wagner.backend;


import gp.wagner.backend.controllers.CategoriesController;
import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;

@SpringBootApplication

// JpaAuditing нужен для отслеживания времени изменения и создания некоторых сущностей
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
public class BackEndGpApplication {

	public static void main(String[] args) {

		SpringApplication.run(BackEndGpApplication.class, args);


		try {
			//String ip = InetAddress.getLocalHost().getHostAddress();
			String ip = InetAddress.getLoopbackAddress().getHostAddress();

			System.out.printf("\nCurrent Ip-address: %s", ip);

		} catch (Exception e) {

			System.out.println("Получить ip сервера не удалось");
		}


	}



}
