package adventure.go.goadventure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class GoadventureApplication{

	private static final Logger log = LoggerFactory.getLogger(GoadventureApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(GoadventureApplication.class, args);

		log.info("Aplikacja włączona");
	}

}
