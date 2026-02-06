package VitAI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;


@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
public class VitAlApplication {

	public static void main(String[] args) {
		SpringApplication.run(VitAlApplication.class, args);
	}

}
