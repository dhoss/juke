package in.stonecolddev.juke;

import in.stonecolddev.juke.configuration.JukeConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@ConfigurationPropertiesScan
public class JukeApplication {

	public static void main(String[] args) {
		SpringApplication.run(JukeApplication.class, args);
	}

}
