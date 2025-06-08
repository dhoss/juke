package in.stonecolddev.juke.configuration;

import in.stonecolddev.juke.ui.CommonPagePropertiesInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final CommonPagePropertiesInterceptor commonPagePropertiesInterceptor;

  public WebConfig(CommonPagePropertiesInterceptor commonPagePropertiesInterceptor) {
    this.commonPagePropertiesInterceptor = commonPagePropertiesInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(commonPagePropertiesInterceptor)
        .addPathPatterns("/")
        .addPathPatterns("/*.html")
        .excludePathPatterns("/admin/*");
  }
}