package br.senac.fullstack.service;

import br.senac.fullstack.dto.AmbienteValidacaoResponseDTO;
import br.senac.fullstack.dto.StartupResponseDTO;
import br.senac.fullstack.dto.TermoPilotoResponseDTO;
import br.senac.fullstack.model.Estagio;
import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.model.StatusProposta;
import br.senac.fullstack.model.TermoPiloto;
import br.senac.fullstack.repository.TermoPilotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TermoPilotoServiceTest {

    private TermoPilotoRepository repository;
    private ObjectProvider<TermoPilotoRepository> repositoryProvider;
    private TermoPilotoService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        repository = mock(TermoPilotoRepository.class);
        repositoryProvider = mock(ObjectProvider.class);
        when(repositoryProvider.getIfAvailable()).thenReturn(repository);
        service = new TermoPilotoService(repositoryProvider);
    }

    @Test
    @DisplayName("Deve salvar termo no repositorio e em memoria")
    void deveSalvarTermo() {
        TermoPiloto termo = new TermoPiloto();
        termo.setPropostaPocId(UUID.randomUUID());
        when(repository.save(any(TermoPiloto.class))).thenReturn(termo);

        TermoPiloto salvo = service.salvar(termo);

        assertNotNull(salvo);
        verify(repository).save(termo);
    }

    @Test
    @DisplayName("Deve salvar em memoria quando repositorio falhar")
    void deveSalvarEmMemoriaQuandoRepositorioFalhar() {
        TermoPiloto termo = new TermoPiloto();
        termo.setPropostaPocId(UUID.randomUUID());
        when(repository.save(any(TermoPiloto.class))).thenThrow(new RuntimeException("DB offline"));

        TermoPiloto salvo = service.salvar(termo);

        assertNotNull(salvo);
        assertEquals(termo.getId(), salvo.getId());
    }

    @Test
    @DisplayName("Deve lancar BAD_REQUEST ao tentar salvar termo nulo")
    void deveLancarExcecaoAoSalvarNulo() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.salvar(null));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    @DisplayName("Deve buscar termo por id atraves do repositorio")
    void deveBuscarPorIdNoRepositorio() {
        UUID id = UUID.randomUUID();
        TermoPiloto termo = new TermoPiloto();
        termo.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(termo));

        TermoPiloto encontrado = service.buscarPorId(id);

        assertNotNull(encontrado);
        assertEquals(id, encontrado.getId());
    }

    @Test
    @DisplayName("Deve buscar por id em memoria quando repositorio lanca excecao")
    void deveBuscarPorIdEmMemoriaQuandoRepoFalha() {
        UUID id = UUID.randomUUID();
        TermoPiloto termo = new TermoPiloto();
        termo.setId(id);
        when(repository.save(any(TermoPiloto.class))).thenThrow(new RuntimeException());
        service.salvar(termo);

        when(repository.findById(id)).thenThrow(new RuntimeException("DB offline"));
        TermoPiloto encontrado = service.buscarPorId(id);

        assertNotNull(encontrado);
        assertEquals(id, encontrado.getId());
    }

    @Test
    @DisplayName("Deve lancar NOT_FOUND quando termo nao existe por id")
    void deveLancarNotFoundQuandoNaoEncontradoPorId() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.buscarPorId(id));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    @DisplayName("Deve lancar BAD_REQUEST quando id buscado for nulo")
    void deveLancarBadRequestQuandoIdNulo() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.buscarPorId(null));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    @DisplayName("Deve buscar termo por proposta id no repositorio")
    void deveBuscarPorPropostaId() {
        UUID propostaId = UUID.randomUUID();
        TermoPiloto termo = new TermoPiloto();
        termo.setPropostaPocId(propostaId);
        when(repository.findByPropostaPocId(propostaId)).thenReturn(Optional.of(termo));

        TermoPiloto encontrado = service.buscarPorPropostaId(propostaId);

        assertNotNull(encontrado);
        assertEquals(propostaId, encontrado.getPropostaPocId());
    }

    @Test
    @DisplayName("Deve buscar por proposta id em memoria quando repositorio lanca excecao")
    void deveBuscarPorPropostaIdEmMemoriaQuandoRepoFalhar() {
        UUID propostaId = UUID.randomUUID();
        TermoPiloto termo = new TermoPiloto();
        termo.setPropostaPocId(propostaId);
        when(repository.save(any())).thenThrow(new RuntimeException());
        service.salvar(termo);

        when(repository.findByPropostaPocId(propostaId)).thenThrow(new RuntimeException());
        TermoPiloto encontrado = service.buscarPorPropostaId(propostaId);

        assertNotNull(encontrado);
        assertEquals(propostaId, encontrado.getPropostaPocId());
    }

    @Test
    @DisplayName("Deve lancar NOT_FOUND quando termo nao existe por proposta id")
    void deveLancarNotFoundQuandoPropostaNaoPossuiTermo() {
        UUID propostaId = UUID.randomUUID();
        when(repository.findByPropostaPocId(propostaId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.buscarPorPropostaId(propostaId));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    @DisplayName("Deve lancar BAD_REQUEST quando proposta id for nulo")
    void deveLancarBadRequestQuandoPropostaIdNulo() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.buscarPorPropostaId(null));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    @DisplayName("Deve listar termos atraves do repositorio")
    void deveListarTermos() {
        TermoPiloto termo = new TermoPiloto();
        when(repository.findAll()).thenReturn(List.of(termo));

        List<TermoPiloto> lista = service.listar();

        assertFalse(lista.isEmpty());
        assertEquals(1, lista.size());
    }

    @Test
    @DisplayName("Deve listar termos da memoria quando repositorio falhar")
    void deveListarTermosMemoriaQuandoRepoFalhar() {
        TermoPiloto termo = new TermoPiloto();
        when(repository.save(any())).thenThrow(new RuntimeException());
        service.salvar(termo);

        when(repository.findAll()).thenThrow(new RuntimeException());
        List<TermoPiloto> lista = service.listar();

        assertFalse(lista.isEmpty());
    }

    @Test
    @DisplayName("Deve construir TermoPilotoResponseDTO com revelacao de dados do ambiente")
    void deveConstruirResponseDTO() {
        TermoPiloto termo = new TermoPiloto();
        termo.setEscopoTeste("Teste IA");
        AmbienteValidacaoResponseDTO ambiente = new AmbienteValidacaoResponseDTO(
                UUID.randomUUID(), "Org S.A.", "Org", "TI", PorteOrganizacao.MEDIO,
                "2611606", "Recife", "contato@org.com", "81988887777", null, true, LocalDateTime.now()
        );

        TermoPilotoResponseDTO dto = service.construirResponseDTO(termo, ambiente);

        assertNotNull(dto);
        assertEquals("Teste IA", dto.escopoTeste());
        assertEquals(StatusProposta.EM_TESTE, dto.statusProposta());
        assertEquals("Org S.A.", dto.razaoSocial());
        assertEquals("contato@org.com", dto.emailContato());
    }

    @Test
    @DisplayName("Deve obter termo com identidade revelada quando usuario for admin ou proprietario")
    @SuppressWarnings("unchecked")
    void deveObterTermoComIdentidadeRevelada() {
        AmbienteValidacaoService ambService = mock(AmbienteValidacaoService.class);
        StartupService startService = mock(StartupService.class);

        ObjectProvider<AmbienteValidacaoService> ambProvider = mock(ObjectProvider.class);
        ObjectProvider<StartupService> startProvider = mock(ObjectProvider.class);
        when(ambProvider.getIfAvailable()).thenReturn(ambService);
        when(startProvider.getIfAvailable()).thenReturn(startService);

        TermoPilotoService serviceCompleto = new TermoPilotoService(repositoryProvider, ambProvider, startProvider);

        UUID propostaId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();

        TermoPiloto termo = new TermoPiloto();
        termo.setPropostaPocId(propostaId);
        termo.setStartupId(startupId);
        termo.setAmbienteValidacaoId(ambienteId);
        when(repository.findByPropostaPocId(propostaId)).thenReturn(Optional.of(termo));

        AmbienteValidacaoResponseDTO ambDTO = new AmbienteValidacaoResponseDTO(
                ambienteId, "Org S.A.", "Org", "TI", PorteOrganizacao.MEDIO,
                "2611606", "Recife", "contato@org.com", "81988887777", null, true, LocalDateTime.now()
        );
        when(ambService.buscar(ambienteId)).thenReturn(ambDTO);
        when(ambService.buscarPorEmail("parceiro@email.com")).thenReturn(ambDTO);

        Authentication authAdmin = new UsernamePasswordAuthenticationToken(
                "admin@email.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        TermoPilotoResponseDTO respAdmin = serviceCompleto.obterTermoComIdentidadeRevelada(propostaId, authAdmin);
        assertNotNull(respAdmin);
        assertEquals("Org S.A.", respAdmin.razaoSocial());

        Authentication authAmbiente = new UsernamePasswordAuthenticationToken(
                "parceiro@email.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_AMBIENTE_VALIDACAO")));

        TermoPilotoResponseDTO respAmbiente = serviceCompleto.obterTermoComIdentidadeRevelada(propostaId, authAmbiente);
        assertNotNull(respAmbiente);
        assertEquals("Org S.A.", respAmbiente.razaoSocial());

        StartupResponseDTO startupDTO = new StartupResponseDTO(
                startupId, "Startup", "startup@email.com", "Desc", "TI",
                LocalDate.now(), Estagio.MVP_FUNCIONAL, List.of(), "Recife", "PE", false, null, LocalDateTime.now()
        );
        when(startService.buscarPorEmail("startup@email.com")).thenReturn(startupDTO);

        Authentication authStartup = new UsernamePasswordAuthenticationToken(
                "startup@email.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_STARTUP")));

        TermoPilotoResponseDTO respStartup = serviceCompleto.obterTermoComIdentidadeRevelada(propostaId, authStartup);
        assertNotNull(respStartup);

        Authentication authOutro = new UsernamePasswordAuthenticationToken(
                "estranho@email.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_STARTUP")));
        when(startService.buscarPorEmail("estranho@email.com")).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> serviceCompleto.obterTermoComIdentidadeRevelada(propostaId, authOutro));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    @DisplayName("buscarAmbienteRevelado deve retornar nulo quando id for nulo ou provider ausente")
    void buscarAmbienteReveladoTrataNulosEFalhas() {
        assertNull(service.buscarAmbienteRevelado(null));
        assertNull(service.buscarAmbienteRevelado(UUID.randomUUID()));
    }
}
