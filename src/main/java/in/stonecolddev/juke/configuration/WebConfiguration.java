package in.stonecolddev.juke.configuration;

import in.stonecolddev.juke.ui.page.CommonPagePropertiesInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

  private final CommonPagePropertiesInterceptor commonPagePropertiesInterceptor;

  public WebConfiguration(CommonPagePropertiesInterceptor commonPagePropertiesInterceptor) {
    this.commonPagePropertiesInterceptor = commonPagePropertiesInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(commonPagePropertiesInterceptor)
        .addPathPatterns("/")
        .addPathPatterns("/pages")
        .addPathPatterns("/pages/*.html")
        .addPathPatterns("/users")
        .addPathPatterns("/users/*")
        .excludePathPatterns("/admin/*");
  }
}