package br.senac.fullstack.service;

import br.senac.fullstack.dto.AceiteTermoResponseDTO;
import br.senac.fullstack.dto.AmbienteValidacaoResponseDTO;
import br.senac.fullstack.model.PorteOrganizacao;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DastDemoSeedRunnerTest {

    @Test
    void runPopulaDadosDemoERegistraUsuariosQuandoNaoExistem() {
        AceiteTermoService aceiteService = mock(AceiteTermoService.class);
        StartupService startupService = mock(StartupService.class);
        AmbienteValidacaoService ambienteService = mock(AmbienteValidacaoService.class);
        DesafioValidacaoService desafioService = mock(DesafioValidacaoService.class);
        UserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();
        PasswordEncoder encoder = new BCryptPasswordEncoder();

        UUID aceiteId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();

        when(aceiteService.registrar(any(), any(), any())).thenReturn(
                new AceiteTermoResponseDTO(aceiteId, "LGPD-2026.1", LocalDateTime.now(), true));
        when(ambienteService.criar(any())).thenReturn(
                new AmbienteValidacaoResponseDTO(
                        ambienteId, "Supermercados Nova Era Ltda", "Supermercados Nova Era",
                        "Varejo", PorteOrganizacao.MEDIO, "PE", "Recife",
                        "inovacao@novaera.com.br", "(81) 99999-0000",
                        "https://www.linkedin.com/company/novaera-varejo", true, LocalDateTime.now()));

        DastDemoSeedRunner runner = new DastDemoSeedRunner(
                aceiteService,
                startupService,
                ambienteService,
                desafioService,
                new FixedObjectProvider<>(userDetailsManager),
                new FixedObjectProvider<>(encoder),
                "demo-pass");

        runner.run(new DefaultApplicationArguments());

        verify(aceiteService).registrar(any(), any(), any());
        verify(startupService).criar(any());
        verify(ambienteService).criar(any());
        verify(desafioService, times(2)).criar(any());
        assertTrue(userDetailsManager.userExists("contato@techpulse.com.br"));
        assertTrue(userDetailsManager.userExists("inovacao@novaera.com.br"));

        // Re-running when users already exist should not fail
        runner.run(new DefaultApplicationArguments());
        assertTrue(userDetailsManager.userExists("contato@techpulse.com.br"));
    }

    @Test
    void runToleraAusenciaDeUserDetailsManagerOuPasswordEncoder() {
        AceiteTermoService aceiteService = mock(AceiteTermoService.class);
        StartupService startupService = mock(StartupService.class);
        AmbienteValidacaoService ambienteService = mock(AmbienteValidacaoService.class);
        DesafioValidacaoService desafioService = mock(DesafioValidacaoService.class);

        UUID aceiteId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();

        when(aceiteService.registrar(any(), any(), any())).thenReturn(
                new AceiteTermoResponseDTO(aceiteId, "LGPD-2026.1", LocalDateTime.now(), true));
        when(ambienteService.criar(any())).thenReturn(
                new AmbienteValidacaoResponseDTO(
                        ambienteId, "Razao", "Fantasia", "Varejo", PorteOrganizacao.MEDIO,
                        "PE", "Recife", "inovacao@novaera.com.br", "(81) 99999-0000",
                        "https://linkedin.com", true, LocalDateTime.now()));

        DastDemoSeedRunner runnerSemManager = new DastDemoSeedRunner(
                aceiteService, startupService, ambienteService, desafioService,
                new NullObjectProvider<>(), new NullObjectProvider<>(), "demo-pass");
        runnerSemManager.run(new DefaultApplicationArguments());

        DastDemoSeedRunner runnerSemEncoder = new DastDemoSeedRunner(
                aceiteService, startupService, ambienteService, desafioService,
                new FixedObjectProvider<>(new InMemoryUserDetailsManager()), new NullObjectProvider<>(), "demo-pass");
        runnerSemEncoder.run(new DefaultApplicationArguments());

        verify(desafioService, times(4)).criar(any());
    }
}
