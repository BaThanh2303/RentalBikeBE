package com.example.rentalebike.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")         // <-- Accept tất cả method: GET, POST, PUT, DELETE,...
                .allowedHeaders("*")         // <-- Accept tất cả headers
                .allowCredentials(true);
    }

}
