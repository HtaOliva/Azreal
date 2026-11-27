package br.senac.fullstack.service;

import br.senac.fullstack.config.SecurityConfig;
import br.senac.fullstack.controller.AmbienteValidacaoController;
import br.senac.fullstack.controller.StartupController;
import br.senac.fullstack.dto.AmbienteValidacaoRequestDTO;
import br.senac.fullstack.dto.AmbienteValidacaoResponseDTO;
import br.senac.fullstack.dto.StartupRequestDTO;
import br.senac.fullstack.dto.StartupResponseDTO;
import br.senac.fullstack.dto.TermoPilotoResponseDTO;
import br.senac.fullstack.model.DesafioValidacao;
import br.senac.fullstack.model.Estagio;
import br.senac.fullstack.model.ModalidadeTeste;
import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.model.StatusProposta;
import br.senac.fullstack.model.TermoPiloto;
import br.senac.fullstack.repository.DesafioValidacaoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Sprint4BackendCoverageTest {

    @Test
    void ambienteValidacaoControllerMeuAmbienteERegistroCredenciaisLocais() {
        AmbienteValidacaoService service = mock(AmbienteValidacaoService.class);
        UserDetailsManager manager = new InMemoryUserDetailsManager();
        PasswordEncoder encoder = new BCryptPasswordEncoder();

        AmbienteValidacaoController controller = new AmbienteValidacaoController(
                service,
                new FixedObjectProvider<>(manager),
                new FixedObjectProvider<>(encoder));

        AmbienteValidacaoResponseDTO dto = new AmbienteValidacaoResponseDTO(
                UUID.randomUUID(), "Empresa Ltda", "Empresa", "Varejo",
                PorteOrganizacao.MEDIO, "PE", "Recife", "amb@empresa.com",
                "(81) 99999-0000", "https://linkedin.com/company/emp", true, LocalDateTime.now());
        when(service.criar(any())).thenReturn(dto);
        when(service.buscarPorEmail("amb@empresa.com")).thenReturn(dto);

        AmbienteValidacaoRequestDTO req = new AmbienteValidacaoRequestDTO(
                "Empresa Ltda", "Empresa", "Varejo", PorteOrganizacao.MEDIO,
                "PE", "Recife", "amb@empresa.com", "(81) 99999-0000",
                "https://linkedin.com/company/emp", "segredo123", UUID.randomUUID());

        ResponseEntity<AmbienteValidacaoResponseDTO> created = controller.criar(req);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        assertTrue(manager.userExists("amb@empresa.com"));

        // Second call when user already exists
        controller.criar(req);

        // Null authentication returns 401
        assertEquals(HttpStatus.UNAUTHORIZED, controller.meuAmbiente(null).getStatusCode());

        // Valid authentication returns 200
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "amb@empresa.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_AMBIENTE_VALIDACAO")));
        ResponseEntity<AmbienteValidacaoResponseDTO> me = controller.meuAmbiente(auth);
        assertEquals(HttpStatus.OK, me.getStatusCode());
        assertEquals("amb@empresa.com", me.getBody().emailContato());
    }

    @Test
    void startupControllerRegistroCredenciaisLocais() {
        StartupService service = mock(StartupService.class);
        UserDetailsManager manager = new InMemoryUserDetailsManager();
        PasswordEncoder encoder = new BCryptPasswordEncoder();

        StartupController controller = new StartupController(
                service,
                null,
                new FixedObjectProvider<>(manager),
                new FixedObjectProvider<>(encoder));

        StartupResponseDTO dto = new StartupResponseDTO(
                UUID.randomUUID(), "Startup X", "startup@x.com", "Descricao",
                "Saude", LocalDate.of(2024, 1, 1), Estagio.MVP_FUNCIONAL,
                List.of("Fundador"), "Recife", "Brasil", true, "Porto Digital",
                LocalDateTime.now());
        when(service.criar(any())).thenReturn(dto);

        StartupRequestDTO req = new StartupRequestDTO(
                "Startup X", "startup@x.com", "segredo123", "Descricao",
                "Saude", LocalDate.of(2024, 1, 1), Estagio.MVP_FUNCIONAL,
                List.of("Fundador"), "Recife", "Brasil", true, "Porto Digital",
                UUID.randomUUID(), "PE", "Recife", "Objetivo", List.of(),
                List.of(ModalidadeTeste.REMOTO));

        ResponseEntity<StartupResponseDTO> created = controller.criar(req);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        assertTrue(manager.userExists("startup@x.com"));

        // Second call when user already exists
        controller.criar(req);
    }

    @Test
    void termoPilotoResponseDtoConstrutor27ArgsEFromEntityComNomeStartup() {
        UUID id = UUID.randomUUID();
        LocalDateTime agora = LocalDateTime.now();

        TermoPilotoResponseDTO dto = new TermoPilotoResponseDTO(
                id, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "Escopo", 30, "Resp S", "Resp A", "Metrica",
                agora, StatusProposta.EM_TESTE, "Razao", "Fantasia", "e@e.com", "999",
                "Varejo", PorteOrganizacao.MEDIO, null, null, null, null, null, null, null, false, null);

        assertEquals("TP-" + id.toString().substring(0, 8).toUpperCase(), dto.codigoTermo());
        assertEquals(agora, dto.dataInicio());
        assertEquals(agora.plusDays(30), dto.dataFim());
        assertNull(dto.nomeStartup());

        TermoPilotoResponseDTO dtoNulls = new TermoPilotoResponseDTO(
                null, null, null, null, null, "Escopo", null, null, null, null,
                null, StatusProposta.EM_TESTE, null, null, null, null, null, null,
                null, null, null, null, null, null, null, false, null);
        assertNull(dtoNulls.codigoTermo());
        assertNull(dtoNulls.dataFim());

        TermoPiloto termo = new TermoPiloto();
        termo.setId(id);
        termo.setDataCriacao(agora);
        termo.setDuracaoDias(15);
        termo.setStatus(StatusProposta.EM_TESTE);

        TermoPilotoResponseDTO fromEntity = TermoPilotoResponseDTO.fromEntity(
                termo, StatusProposta.EM_TESTE, null, "TechPulse");
        assertEquals("TechPulse", fromEntity.nomeStartup());
        assertEquals(agora.plusDays(15), fromEntity.dataFim());
        assertNotNull(TermoPilotoResponseDTO.fromEntity(termo, StatusProposta.EM_TESTE, null));
    }

    @Test
    void desafioValidacaoServiceListarEntidadesAtivasEMatchmakingFallback() {
        DesafioValidacaoRepository repo = mock(DesafioValidacaoRepository.class);
        DesafioValidacao d = new DesafioValidacao();
        d.setId(UUID.randomUUID());
        d.setAtivo(true);
        when(repo.findByAtivoTrue()).thenReturn(List.of(d));

        DesafioValidacaoService serviceComRepo = new DesafioValidacaoService(
                new FixedObjectProvider<>(repo), new NullObjectProvider<>(), new NullObjectProvider<>());
        assertEquals(1, serviceComRepo.listarEntidadesAtivas().size());

        DesafioValidacaoService serviceSemRepo = new DesafioValidacaoService(
                new NullObjectProvider<>(), new NullObjectProvider<>(), new NullObjectProvider<>());
        assertTrue(serviceSemRepo.listarEntidadesAtivas().isEmpty());

        MatchmakingService matchmaking = new MatchmakingService(
                new NullObjectProvider<>(),
                new NullObjectProvider<>(),
                new NullObjectProvider<>(),
                new FixedObjectProvider<>(serviceComRepo),
                new NullObjectProvider<>());
        assertEquals(1, matchmaking.obterDesafiosAtivos().size());

        MatchmakingService matchmakingVazio = new MatchmakingService(
                new NullObjectProvider<>(),
                new NullObjectProvider<>(),
                new NullObjectProvider<>(),
                new NullObjectProvider<>(),
                new NullObjectProvider<>());
        assertTrue(matchmakingVazio.obterDesafiosAtivos().isEmpty());
    }

    @Test
    void termoPilotoServiceResolverNomeStartupESecurityConfigUserDetailsManager() {
        StartupService startupService = mock(StartupService.class);
        UUID startupId = UUID.randomUUID();
        StartupResponseDTO startupDto = new StartupResponseDTO(
                startupId, "TechPulse", "contato@techpulse.com.br", "Descricao",
                "Varejo", LocalDate.of(2024, 1, 1), Estagio.MVP_FUNCIONAL,
                List.of("Ana"), "Recife", "Brasil", true, "Porto Digital",
                LocalDateTime.now());
        when(startupService.buscar(startupId)).thenReturn(startupDto);

        TermoPilotoService termoService = new TermoPilotoService(
                new NullObjectProvider<>(),
                new NullObjectProvider<>(),
                new FixedObjectProvider<>(startupService));

        assertNull(termoService.resolverNomeStartup(null));
        assertEquals("TechPulse", termoService.resolverNomeStartup(startupId));

        UUID idErro = UUID.randomUUID();
        when(startupService.buscar(idErro)).thenThrow(new RuntimeException("not found"));
        assertNull(termoService.resolverNomeStartup(idErro));

        TermoPilotoService termoSemStartup = new TermoPilotoService(new NullObjectProvider<>());
        assertNull(termoSemStartup.resolverNomeStartup(startupId));

        TermoPiloto termo = new TermoPiloto();
        termo.setId(UUID.randomUUID());
        termo.setStartupId(startupId);
        termo.setStatus(StatusProposta.EM_TESTE);
        assertEquals("TechPulse", termoService.construirResponseDTO(termo, null).nomeStartup());

        SecurityConfig securityConfig = new SecurityConfig();
        assertNotNull(securityConfig.userDetailsManager());
    }
}
