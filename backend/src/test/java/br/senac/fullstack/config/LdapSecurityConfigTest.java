package br.senac.fullstack.config;

import org.junit.jupiter.api.Test;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class LdapSecurityConfigTest {

    @Test
    void instanciacaoEComponentesConfig() {
        LdapSecurityConfig config = new LdapSecurityConfig();
        ReflectionTestUtils.setField(config, "ldapUrls", "ldap://localhost:3890");
        ReflectionTestUtils.setField(config, "ldapBase", "dc=example,dc=com");
        ReflectionTestUtils.setField(config, "ldapBindDn", "uid=admin,ou=people,dc=example,dc=com");
        ReflectionTestUtils.setField(config, "ldapBindPassword", "secret");
        ReflectionTestUtils.setField(config, "adminGroup", "azrael_admin");

        CorsConfigurationSource corsSource = config.corsConfigurationSource();
        assertNotNull(corsSource);

        BaseLdapPathContextSource contextSource = config.contextSource();
        assertNotNull(contextSource);

        assertNotNull(config.ldapAuthenticationProvider(contextSource));
    }
}
