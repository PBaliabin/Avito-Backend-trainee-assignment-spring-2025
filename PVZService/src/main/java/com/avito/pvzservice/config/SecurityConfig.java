package com.avito.pvzservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/**")
                    .permitAll() // Разрешаем доступ к /api/auth без аутентификации
                    .anyRequest()
                    .authenticated() // Все остальные запросы требуют аутентификации
            )
        .formLogin(AbstractHttpConfigurer::disable); // Отключаем форму логина по умолчанию

    return http.build();
  }
}
