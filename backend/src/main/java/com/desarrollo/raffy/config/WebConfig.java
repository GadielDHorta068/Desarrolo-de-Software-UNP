package com.desarrollo.raffy.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    // Esta configuración asegura que Spring MVC maneje correctamente los endpoints REST
    // y no los trate como recursos estáticos
}