package br.senac.fullstack.service;

import br.senac.fullstack.dto.AmbienteValidacaoResponseDTO;
import br.senac.fullstack.dto.ConfirmacaoExecucaoRequestDTO;
import br.senac.fullstack.dto.EntregaPilotoRequestDTO;
import br.senac.fullstack.dto.StartupResponseDTO;
import br.senac.fullstack.dto.TermoPilotoResponseDTO;
import br.senac.fullstack.model.AvaliacaoMetrica;
import br.senac.fullstack.model.Estagio;
import br.senac.fullstack.model.ModoPrivacidadePoc;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Test
    @DisplayName("buscarPorIdOuPropostaId deve buscar por id ou por propostaId com sucesso")
    void buscarPorIdOuPropostaIdCenarios() {
        UUID termoId = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();
        TermoPiloto termo = new TermoPiloto();
        termo.setId(termoId);
        termo.setPropostaPocId(propostaId);

        when(repository.findById(termoId)).thenReturn(Optional.of(termo));
        when(repository.findByPropostaPocId(propostaId)).thenReturn(Optional.of(termo));

        assertEquals(termoId, service.buscarPorIdOuPropostaId(termoId).getId());
        assertEquals(propostaId, service.buscarPorIdOuPropostaId(propostaId).getPropostaPocId());

        assertThrows(ResponseStatusException.class, () -> service.buscarPorIdOuPropostaId(null));
        assertThrows(ResponseStatusException.class, () -> service.buscarPorIdOuPropostaId(UUID.randomUUID()));
    }

    @Test
    @DisplayName("RF18 - registrarEntrega com sucesso quando startup for dona do termo")
    @SuppressWarnings("unchecked")
    void deveRegistrarEntregaComSucesso() {
        StartupService startService = mock(StartupService.class);
        ObjectProvider<StartupService> startProvider = mock(ObjectProvider.class);
        when(startProvider.getIfAvailable()).thenReturn(startService);

        TermoPilotoService serv = new TermoPilotoService(repositoryProvider, null, startProvider, null);

        UUID termoId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        TermoPiloto termo = new TermoPiloto();
        termo.setId(termoId);
        termo.setStartupId(startupId);
        termo.setStatus(StatusProposta.EM_TESTE);

        when(repository.findById(termoId)).thenReturn(Optional.of(termo));
        when(repository.save(any(TermoPiloto.class))).thenAnswer(i -> i.getArgument(0));

        StartupResponseDTO startupDTO = new StartupResponseDTO(
                startupId, "Startup Tech", "owner@startup.com", "Desc", "TI",
                LocalDate.now(), Estagio.EM_TESTE, List.of(), "Recife", "PE", false, null, LocalDateTime.now()
        );
        when(startService.buscarPorEmail("owner@startup.com")).thenReturn(startupDTO);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "owner@startup.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_STARTUP")));

        EntregaPilotoRequestDTO req = new EntregaPilotoRequestDTO(
                "Algoritmo testado em 500 pacientes com sucesso",
                "https://drive.google.com/evidencias",
                "Nenhum incidente reportado"
        );

        TermoPiloto atualizado = serv.registrarEntrega(termoId, req, auth);

        assertNotNull(atualizado);
        assertEquals("Algoritmo testado em 500 pacientes com sucesso", atualizado.getResumoEntrega());
        assertEquals("https://drive.google.com/evidencias", atualizado.getLinksEvidencias());
        assertEquals("Nenhum incidente reportado", atualizado.getObservacoesEntrega());
        assertNotNull(atualizado.getDataEntrega());
    }

    @Test
    @DisplayName("RF18 - registrarEntrega deve lancar FORBIDDEN quando startup nao for dona do termo")
    @SuppressWarnings("unchecked")
    void deveLancarForbiddenAoRegistrarEntregaStartupDiferente() {
        StartupService startService = mock(StartupService.class);
        ObjectProvider<StartupService> startProvider = mock(ObjectProvider.class);
        when(startProvider.getIfAvailable()).thenReturn(startService);

        TermoPilotoService serv = new TermoPilotoService(repositoryProvider, null, startProvider, null);

        UUID termoId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        TermoPiloto termo = new TermoPiloto();
        termo.setId(termoId);
        termo.setStartupId(startupId);
        termo.setStatus(StatusProposta.EM_TESTE);

        when(repository.findById(termoId)).thenReturn(Optional.of(termo));
        when(startService.buscarPorEmail("outro@startup.com")).thenReturn(null);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "outro@startup.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_STARTUP")));

        EntregaPilotoRequestDTO req = new EntregaPilotoRequestDTO("Resumo", null, null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> serv.registrarEntrega(termoId, req, auth));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    @DisplayName("RF18 - registrarEntrega deve lancar BAD_REQUEST quando status nao for EM_TESTE ou resumo nulo")
    void deveLancarBadRequestRegistrarEntregaStatusInvalido() {
        UUID termoId = UUID.randomUUID();
        TermoPiloto termo = new TermoPiloto();
        termo.setId(termoId);
        termo.setStatus(StatusProposta.POC_CONCLUIDA);
        when(repository.findById(termoId)).thenReturn(Optional.of(termo));

        Authentication adminAuth = new UsernamePasswordAuthenticationToken(
                "admin@email.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        EntregaPilotoRequestDTO reqValido = new EntregaPilotoRequestDTO("Resumo", null, null);
        assertThrows(ResponseStatusException.class, () -> service.registrarEntrega(termoId, reqValido, adminAuth));

        assertThrows(ResponseStatusException.class, () -> service.registrarEntrega(termoId, null, adminAuth));
        EntregaPilotoRequestDTO reqVazio = new EntregaPilotoRequestDTO("  ", null, null);
        assertThrows(ResponseStatusException.class, () -> service.registrarEntrega(termoId, reqVazio, adminAuth));
    }

    @Test
    @DisplayName("RF19 - confirmarExecucao com sucesso quando pilotoExecutado=true")
    @SuppressWarnings("unchecked")
    void deveConfirmarExecucaoComSucessoExecutado() {
        AmbienteValidacaoService ambService = mock(AmbienteValidacaoService.class);
        PropostaPocService propService = mock(PropostaPocService.class);

        ObjectProvider<AmbienteValidacaoService> ambProvider = mock(ObjectProvider.class);
        ObjectProvider<PropostaPocService> propProvider = mock(ObjectProvider.class);
        when(ambProvider.getIfAvailable()).thenReturn(ambService);
        when(propProvider.getIfAvailable()).thenReturn(propService);

        TermoPilotoService serv = new TermoPilotoService(repositoryProvider, ambProvider, null, propProvider);

        UUID termoId = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();

        TermoPiloto termo = new TermoPiloto();
        termo.setId(termoId);
        termo.setPropostaPocId(propostaId);
        termo.setAmbienteValidacaoId(ambienteId);
        termo.setStatus(StatusProposta.EM_TESTE);

        when(repository.findById(termoId)).thenReturn(Optional.of(termo));
        when(repository.save(any(TermoPiloto.class))).thenAnswer(i -> i.getArgument(0));

        AmbienteValidacaoResponseDTO ambDTO = new AmbienteValidacaoResponseDTO(
                ambienteId, "Clinica Sul S.A.", "Clinica Sul", "Saude", PorteOrganizacao.MEDIO,
                "2611606", "Recife", "diretoria@clinicasul.com", "81999991111", null, true, LocalDateTime.now()
        );
        when(ambService.buscarPorEmail("diretoria@clinicasul.com")).thenReturn(ambDTO);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "diretoria@clinicasul.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_AMBIENTE_VALIDACAO")));

        ConfirmacaoExecucaoRequestDTO req = new ConfirmacaoExecucaoRequestDTO(
                true,
                AvaliacaoMetrica.ATENDEU_TOTALMENTE,
                "Atingiu o tempo de resposta medio abaixo de 200ms",
                ModoPrivacidadePoc.EXIBICAO_NOMINAL,
                "Interesse em contratacao imediata",
                null
        );

        TermoPiloto confirmado = serv.confirmarExecucao(termoId, req, auth);

        assertNotNull(confirmado);
        assertEquals(StatusProposta.POC_CONCLUIDA, confirmado.getStatus());
        assertTrue(confirmado.getPilotoExecutado());
        assertEquals(AvaliacaoMetrica.ATENDEU_TOTALMENTE, confirmado.getAvaliacaoMetrica());
        assertEquals("Atingiu o tempo de resposta medio abaixo de 200ms", confirmado.getParecer());
        assertEquals(ModoPrivacidadePoc.EXIBICAO_NOMINAL, confirmado.getModoPrivacidade());
        assertFalse(confirmado.getCertificadoEmitido());
        verify(propService).atualizarStatus(propostaId, StatusProposta.POC_CONCLUIDA);
    }

    @Test
    @DisplayName("RF19/RF20 - confirmarExecucao emite RegistroOficialPoc automaticamente quando provider disponivel")
    @SuppressWarnings("unchecked")
    void deveEmitirRegistroOficialAutomaticamenteAoConfirmarExecucao() {
        AmbienteValidacaoService ambService = mock(AmbienteValidacaoService.class);
        PropostaPocService propService = mock(PropostaPocService.class);
        RegistroOficialPocService regService = mock(RegistroOficialPocService.class);

        ObjectProvider<AmbienteValidacaoService> ambProvider = mock(ObjectProvider.class);
        ObjectProvider<PropostaPocService> propProvider = mock(ObjectProvider.class);
        ObjectProvider<RegistroOficialPocService> regProvider = mock(ObjectProvider.class);

        when(ambProvider.getIfAvailable()).thenReturn(ambService);
        when(propProvider.getIfAvailable()).thenReturn(propService);
        when(regProvider.getIfAvailable()).thenReturn(regService);

        TermoPilotoService serv = new TermoPilotoService(repositoryProvider, ambProvider, null, propProvider, regProvider);

        UUID termoId = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();

        TermoPiloto termo = new TermoPiloto();
        termo.setId(termoId);
        termo.setPropostaPocId(propostaId);
        termo.setAmbienteValidacaoId(ambienteId);
        termo.setStatus(StatusProposta.EM_TESTE);

        when(repository.findById(termoId)).thenReturn(Optional.of(termo));
        when(repository.save(any(TermoPiloto.class))).thenAnswer(i -> i.getArgument(0));

        AmbienteValidacaoResponseDTO ambDTO = new AmbienteValidacaoResponseDTO(
                ambienteId, "Clinica Sul S.A.", "Clinica Sul", "Saude", PorteOrganizacao.MEDIO,
                "2611606", "Recife", "diretoria@clinicasul.com", "81999991111", null, true, LocalDateTime.now()
        );
        when(ambService.buscarPorEmail("diretoria@clinicasul.com")).thenReturn(ambDTO);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "diretoria@clinicasul.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_AMBIENTE_VALIDACAO")));

        ConfirmacaoExecucaoRequestDTO req = new ConfirmacaoExecucaoRequestDTO(
                true,
                AvaliacaoMetrica.ATENDEU_TOTALMENTE,
                "Atingiu o tempo de resposta medio abaixo de 200ms",
                ModoPrivacidadePoc.EXIBICAO_NOMINAL,
                "Interesse em contratacao imediata",
                null
        );

        TermoPiloto confirmado = serv.confirmarExecucao(termoId, req, auth);

        assertNotNull(confirmado);
        assertEquals(StatusProposta.POC_CONCLUIDA, confirmado.getStatus());
        assertTrue(confirmado.getCertificadoEmitido());
        verify(regService).emitir(any(TermoPiloto.class));
    }

    @Test
    @DisplayName("RF19 - confirmarExecucao quando teste nao aconteceu (pilotoExecutado=false) -> POC_NAO_CONFIRMADA")
    @SuppressWarnings("unchecked")
    void deveConfirmarExecucaoQuandoPilotoNaoAconteceu() {
        PropostaPocService propService = mock(PropostaPocService.class);
        ObjectProvider<PropostaPocService> propProvider = mock(ObjectProvider.class);
        when(propProvider.getIfAvailable()).thenReturn(propService);

        TermoPilotoService serv = new TermoPilotoService(repositoryProvider, null, null, propProvider);

        UUID termoId = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();

        TermoPiloto termo = new TermoPiloto();
        termo.setId(termoId);
        termo.setPropostaPocId(propostaId);
        termo.setAmbienteValidacaoId(ambienteId);
        termo.setStatus(StatusProposta.EM_TESTE);

        when(repository.findById(termoId)).thenReturn(Optional.of(termo));
        when(repository.save(any(TermoPiloto.class))).thenAnswer(i -> i.getArgument(0));

        Authentication adminAuth = new UsernamePasswordAuthenticationToken(
                "admin@email.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        ConfirmacaoExecucaoRequestDTO req = new ConfirmacaoExecucaoRequestDTO(
                false,
                null,
                null,
                null,
                null,
                "Piloto nao iniciado por incompatibilidade operacional"
        );

        TermoPiloto confirmado = serv.confirmarExecucao(termoId, req, adminAuth);

        assertNotNull(confirmado);
        assertEquals(StatusProposta.POC_NAO_CONFIRMADA, confirmado.getStatus());
        assertFalse(confirmado.getPilotoExecutado());
        assertFalse(confirmado.getCertificadoEmitido());
        assertEquals("Piloto nao iniciado por incompatibilidade operacional",
                confirmado.getJustificativaNaoExecucao());
        verify(propService).atualizarStatus(propostaId, StatusProposta.POC_NAO_CONFIRMADA);
    }

    @Test
    @DisplayName("RF19 - confirmarExecucao validacoes de campos e permissao")
    @SuppressWarnings("unchecked")
    void deveValidarCamposEPermissaoConfirmacao() {
        AmbienteValidacaoService ambService = mock(AmbienteValidacaoService.class);
        ObjectProvider<AmbienteValidacaoService> ambProvider = mock(ObjectProvider.class);
        when(ambProvider.getIfAvailable()).thenReturn(ambService);

        TermoPilotoService serv = new TermoPilotoService(repositoryProvider, ambProvider, null, null);

        UUID termoId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();

        TermoPiloto termo = new TermoPiloto();
        termo.setId(termoId);
        termo.setAmbienteValidacaoId(ambienteId);
        termo.setStatus(StatusProposta.EM_TESTE);
        when(repository.findById(termoId)).thenReturn(Optional.of(termo));

        Authentication authEstranho = new UsernamePasswordAuthenticationToken(
                "outro@parceiro.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_AMBIENTE_VALIDACAO")));
        when(ambService.buscarPorEmail("outro@parceiro.com")).thenReturn(null);

        ConfirmacaoExecucaoRequestDTO reqValido = new ConfirmacaoExecucaoRequestDTO(
                true, AvaliacaoMetrica.ATENDEU_PARCIALMENTE, "Parecer", ModoPrivacidadePoc.ANONIMO_QUALIFICADO, null, null
        );

        ResponseStatusException exForbidden = assertThrows(ResponseStatusException.class,
                () -> serv.confirmarExecucao(termoId, reqValido, authEstranho));
        assertEquals(HttpStatus.FORBIDDEN, exForbidden.getStatusCode());

        Authentication adminAuth = new UsernamePasswordAuthenticationToken(
                "admin@email.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        // pilotoExecutado nulo
        assertThrows(ResponseStatusException.class, () -> serv.confirmarExecucao(termoId, null, adminAuth));
        ConfirmacaoExecucaoRequestDTO reqSemExecutado = new ConfirmacaoExecucaoRequestDTO(null, null, null, null, null, null);
        assertThrows(ResponseStatusException.class, () -> serv.confirmarExecucao(termoId, reqSemExecutado, adminAuth));

        // pilotoExecutado=true sem metrica
        ConfirmacaoExecucaoRequestDTO reqSemMetrica = new ConfirmacaoExecucaoRequestDTO(
                true, null, "Parecer", ModoPrivacidadePoc.EXIBICAO_NOMINAL, null, null);
        assertThrows(ResponseStatusException.class, () -> serv.confirmarExecucao(termoId, reqSemMetrica, adminAuth));

        // pilotoExecutado=true sem parecer
        ConfirmacaoExecucaoRequestDTO reqSemParecer = new ConfirmacaoExecucaoRequestDTO(
                true, AvaliacaoMetrica.ATENDEU_TOTALMENTE, "   ", ModoPrivacidadePoc.EXIBICAO_NOMINAL, null, null);
        assertThrows(ResponseStatusException.class, () -> serv.confirmarExecucao(termoId, reqSemParecer, adminAuth));

        // pilotoExecutado=true sem modo privacidade
        ConfirmacaoExecucaoRequestDTO reqSemPrivacidade = new ConfirmacaoExecucaoRequestDTO(
                true, AvaliacaoMetrica.ATENDEU_TOTALMENTE, "Parecer", null, null, null);
        assertThrows(ResponseStatusException.class, () -> serv.confirmarExecucao(termoId, reqSemPrivacidade, adminAuth));

        // status invalido
        termo.setStatus(StatusProposta.POC_CONCLUIDA);
        assertThrows(ResponseStatusException.class, () -> serv.confirmarExecucao(termoId, reqValido, adminAuth));
    }
}
