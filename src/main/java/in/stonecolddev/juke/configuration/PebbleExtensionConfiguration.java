package in.stonecolddev.juke.configuration;

import io.pebbletemplates.pebble.extension.Extension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PebbleExtensionConfiguration {
    @Bean
    public Extension dateNowExtension() {
        return new DateNowExtension();
    }
}