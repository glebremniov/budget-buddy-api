package com.budget.buddy.budget_buddy_api.security.cors;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

  @Bean
  CorsConfigurationSource corsConfigurationSource(CorsProperties props) {
    var config = new CorsConfiguration();
    config.setAllowedOriginPatterns(props.allowedOriginPatterns());
    config.setAllowedMethods(props.allowedMethods());
    config.setAllowedHeaders(props.allowedHeaders());
    config.setAllowCredentials(props.allowCredentials());
    config.setMaxAge(props.maxAge());

    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
