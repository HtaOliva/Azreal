package br.senac.fullstack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity

public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Geralmente desativado em APIs REST stateless (JWT)
                .authorizeHttpRequests(auth -> auth
                        // Libera as rotas de cadastro e login para todo mundo
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        // Qualquer outra rota exige que o usuário esteja autenticado
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
