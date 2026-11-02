package br.senac.fullstack.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckServiceTest {

    @Test
    void checkHealthRetornaUp() {
        HealthCheckService s = new HealthCheckService();
        var h = s.checkHealth();
        assertNotNull(h);
        assertEquals("UP", h.status());
    }
}
