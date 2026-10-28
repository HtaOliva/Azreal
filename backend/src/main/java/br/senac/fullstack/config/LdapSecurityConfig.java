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

import java.util.List;

@Configuration
@EnableMethodSecurity
@Profile("ldap")
public class LdapSecurityConfig {

    @Value("${spring.ldap.urls:ldap://localhost:389}")
    private String ldapUrls;

    @Value("${spring.ldap.base:dc=example,dc=com}")
    private String ldapBase;

    @Value("${spring.ldap.username:}")
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

        LdapAuthenticationProvider provider = new LdapAuthenticationProvider(authenticator, authoritiesPopulator);
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(request -> request.getRequestURI().startsWith("/usuarios")).permitAll()
                        .requestMatchers(request -> request.getRequestURI().startsWith("/auth/login")
                                || request.getRequestURI().startsWith("/startups")
                                || request.getRequestURI().startsWith("/mentors")
                                || request.getRequestURI().startsWith("/termos")
                                || request.getRequestURI().startsWith("/cursos")
                                || request.getRequestURI().startsWith("/incubadoras")).permitAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
