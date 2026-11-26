package br.senac.fullstack.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CorsConfigTest {

    @Test
    void corsConfigAceitaMultiplasOrigensSeparadasPorVirgula() {
        CorsConfig corsConfig = new CorsConfig();
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins",
                "http://localhost:4200, https://azreal.vercel.app");

        WebMvcConfigurer configurer = corsConfig.corsConfigurer();
        assertNotNull(configurer);
        configurer.addCorsMappings(new CorsRegistry());
    }

    @Test
    void securityConfigCorsAceitaOrigensParametrizadas() {
        SecurityConfig securityConfig = new SecurityConfig();
        ReflectionTestUtils.setField(securityConfig, "allowedOrigins",
                "http://localhost:4200, https://azreal.vercel.app");

        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration cors = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/v1/health"));

        assertNotNull(cors);
        assertEquals(List.of("http://localhost:4200", "https://azreal.vercel.app"), cors.getAllowedOrigins());
    }

    @Test
    void ldapSecurityConfigCorsAceitaOrigensParametrizadas() {
        LdapSecurityConfig ldapConfig = new LdapSecurityConfig();
        ReflectionTestUtils.setField(ldapConfig, "allowedOrigins",
                "http://localhost:4200, https://azreal.vercel.app");

        CorsConfigurationSource source = ldapConfig.corsConfigurationSource();
        CorsConfiguration cors = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/v1/health"));

        assertNotNull(cors);
        assertEquals(List.of("http://localhost:4200", "https://azreal.vercel.app"), cors.getAllowedOrigins());
    }
}
