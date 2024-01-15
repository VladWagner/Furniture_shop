package gp.wagner.backend;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.net.InetAddress;

@SpringBootApplication
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
