package gp.wagner.backend;

import gp.wagner.backend.configurations.HibernateSearchConfiguration;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.services.Indexer;
import jakarta.servlet.MultipartConfigElement;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;

@SpringBootApplication
//@ImportResource("WEB-INF/applicationContext.xml")
public class BackEndGpApplication {

	public static void main(String[] args) {

		SpringApplication.run(BackEndGpApplication.class, args);

		/*try {
			Services.indexService.initIndexing();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}*/

	}



}
