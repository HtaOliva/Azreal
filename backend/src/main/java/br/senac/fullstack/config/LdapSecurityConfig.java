package br.senac.fullstack.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
@Profile("ldap")
public class LdapSecurityConfig {

    @Value("${cors.allowed-origins:${CORS_ALLOWED_ORIGINS:http://localhost:4200}}")
    private String allowedOrigins = "http://localhost:4200";

    @Value("${spring.ldap.urls:ldap://lldap.fchicout.dev:3890}")
    private String ldapUrls;

    @Value("${spring.ldap.base:dc=fchicout,dc=dev}")
    private String ldapBase;

    @Value("${spring.ldap.username:uid=azrael_app,ou=people,dc=fchicout,dc=dev}")
    private String ldapBindDn;

    @Value("${spring.ldap.password:}")
    private String ldapBindPassword;

    @Value("${app.ldap.admin-group:azrael_admin}")
    private String adminGroup;

    @Bean
    public BaseLdapPathContextSource contextSource() {
        DefaultSpringSecurityContextSource source = new DefaultSpringSecurityContextSource(ldapUrls + "/" + ldapBase);
        if (ldapBindDn != null && !ldapBindDn.isBlank()) {
            source.setUserDn(ldapBindDn);
            source.setPassword(ldapBindPassword);
        }
        return source;
    }

    @Bean
    public LdapAuthenticationProvider ldapAuthenticationProvider(BaseLdapPathContextSource contextSource) {
        BindAuthenticator authenticator = new BindAuthenticator(contextSource);
        authenticator.setUserSearch(new FilterBasedLdapUserSearch("", "(uid={0})", contextSource));

        DefaultLdapAuthoritiesPopulator authoritiesPopulator =
                new DefaultLdapAuthoritiesPopulator(contextSource, "");
        authoritiesPopulator.setGroupSearchFilter("(member={0})");
        authoritiesPopulator.setSearchSubtree(true);

        return new LdapAuthenticationProvider(authenticator, authoritiesPopulator);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/health",
                                "/api/v1/health",
                                "/api/v1/hello",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/usuarios/**",
                                "/auth/login/**",
                                "/startups/**",
                                "/ambientes-validacao/**",
                                "/termos/**",
                                "/temas/**",
                                "/desafios/**",
                                "/matchmaking/**",
                                "/propostas-poc/**",
                                "/pocs/**",
                                "/registros-poc/**",
                                "/regras/**").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
