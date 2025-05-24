package in.stonecolddev.juke.configuration;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class Layout {
  @Bean
  public LayoutDialect layoutDialect() {
    return new LayoutDialect();
  }
}