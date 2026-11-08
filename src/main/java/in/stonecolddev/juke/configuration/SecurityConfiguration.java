package in.stonecolddev.juke.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable) // TODO: enable csrf
        // TODO: why are these headers disabled?
        .headers(
            headers ->
                headers.frameOptions(
                    HeadersConfigurer.FrameOptionsConfig::disable))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.GET, "/img/*").permitAll()
            .requestMatchers(HttpMethod.GET, "/css/*").permitAll()
            .requestMatchers(HttpMethod.GET, "/").permitAll()
            .requestMatchers(HttpMethod.GET, "/pages").permitAll()
            .requestMatchers(HttpMethod.GET, "/pages/*.html").permitAll()
            .requestMatchers(HttpMethod.GET, "/users/new").permitAll()
            .requestMatchers(HttpMethod.POST, "/users/create").permitAll()
            // TODO: update this to whatever default user role
            .requestMatchers(HttpMethod.GET, "/users/welcome").permitAll()
            // TODO: make this suck less
            .requestMatchers(HttpMethod.GET, "/users/home").hasAnyAuthority("admin", "regular_user")
            .requestMatchers(HttpMethod.GET, "/admin").hasAuthority("admin")
            .requestMatchers(HttpMethod.GET, "/admin/**").hasAuthority("admin")
            .anyRequest()
            .authenticated())
        .formLogin(
            form ->
                form.loginPage("/login")
                    .permitAll()
                    .defaultSuccessUrl("/users/home", true));

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

}