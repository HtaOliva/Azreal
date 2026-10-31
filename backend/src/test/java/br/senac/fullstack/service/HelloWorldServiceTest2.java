package br.senac.fullstack.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HelloWorldServiceExtraTest {

    @Test
    void getGreetingRetornaMensagem() {
        HelloWorldService s = new HelloWorldService();
        var r = s.getGreeting("Mundo");
        assertNotNull(r);
        assertTrue(r.message().contains("Mundo"));
    }
}
