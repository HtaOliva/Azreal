package br.senac.fullstack.service;

import br.senac.fullstack.dto.PropostaPocRequestDTO;
import br.senac.fullstack.dto.PropostaPocResponseDTO;
import br.senac.fullstack.model.StatusProposta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropostaPocServiceTest {

    private static final String ESCOPO_PADRAO = "Validacao de acuracia do modelo";
    private static final String RESP_STARTUP = "Fornecer endpoint e modelo de predicao";
    private static final String RESP_AMBIENTE = "Disponibilizar dataset anonimizado de teste";
    private static final String METRICA_PADRAO = "Acuracia minima de 85%";

    private PropostaPocService service;

    @BeforeEach
    void setUp() {
        service = new PropostaPocService();
    }

    private PropostaPocRequestDTO criarRequestValido(UUID startupId, UUID desafioId) {
        return new PropostaPocRequestDTO(
                startupId,
                desafioId,
                ESCOPO_PADRAO,
                15,
                RESP_STARTUP,
                RESP_AMBIENTE,
                METRICA_PADRAO,
                true
        );
    }

    @Test
    void criarPropostaComSucesso() {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        PropostaPocRequestDTO request = criarRequestValido(startupId, desafioId);

        PropostaPocResponseDTO response = service.criar(request);

        assertNotNull(response.id());
        assertEquals(startupId, response.startupId());
        assertEquals(desafioId, response.desafioValidacaoId());
        assertEquals(StatusProposta.PENDENTE, response.status());
        assertNotNull(response.prazoRespostaAmbiente());
        assertEquals(1, service.contarPendentesPorStartup(startupId));
    }

    @Test
    void naoDevePermitirMaisDeTresPropostasPendentesSimultaneas() {
        UUID startupId = UUID.randomUUID();

        service.criar(criarRequestValido(startupId, UUID.randomUUID()));
        service.criar(criarRequestValido(startupId, UUID.randomUUID()));
        service.criar(criarRequestValido(startupId, UUID.randomUUID()));

        assertEquals(3, service.contarPendentesPorStartup(startupId));

        PropostaPocRequestDTO quartaProposta = criarRequestValido(startupId, UUID.randomUUID());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.criar(quartaProposta));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("limite maximo de 3 propostas pendentes"));
    }

    @Test
    void naoDevePermitirProporDuasVezesAoMesmoDesafio() {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        service.criar(criarRequestValido(startupId, desafioId));

        PropostaPocRequestDTO duplicada = criarRequestValido(startupId, desafioId);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.criar(duplicada));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("ja possui proposta ativa para este desafio"));
    }

    @Test
    void cancelamentoDePropostaLiberaVagaNoLimite() {
        UUID startupId = UUID.randomUUID();
        UUID desafio1 = UUID.randomUUID();
        UUID desafio2 = UUID.randomUUID();
        UUID desafio3 = UUID.randomUUID();
        UUID desafio4 = UUID.randomUUID();

        PropostaPocResponseDTO p1 = service.criar(criarRequestValido(startupId, desafio1));
        service.criar(criarRequestValido(startupId, desafio2));
        service.criar(criarRequestValido(startupId, desafio3));

        assertEquals(3, service.contarPendentesPorStartup(startupId));

        service.cancelar(p1.id());
        assertEquals(2, service.contarPendentesPorStartup(startupId));

        PropostaPocResponseDTO p4 = service.criar(criarRequestValido(startupId, desafio4));
        assertNotNull(p4.id());
        assertEquals(3, service.contarPendentesPorStartup(startupId));
    }

    @Test
    void permiteNovaPropostaAoMesmoDesafioAposCancelamentoDaAnterior() {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        PropostaPocResponseDTO p1 = service.criar(criarRequestValido(startupId, desafioId));
        service.cancelar(p1.id());

        PropostaPocResponseDTO p2 = service.criar(criarRequestValido(startupId, desafioId));
        assertNotNull(p2.id());
        assertEquals(StatusProposta.PENDENTE, p2.status());
    }

    @Test
    void propostasEmOutrosStatusNaoContamNoLimiteDePendencias() {
        UUID startupId = UUID.randomUUID();

        PropostaPocResponseDTO p1 = service.criar(criarRequestValido(startupId, UUID.randomUUID()));
        PropostaPocResponseDTO p2 = service.criar(criarRequestValido(startupId, UUID.randomUUID()));
        service.criar(criarRequestValido(startupId, UUID.randomUUID()));

        UUID p1Id = p1.id();
        UUID p2Id = p2.id();
        service.obterPorId(p1Id).setStatus(StatusProposta.EM_TESTE);
        service.obterPorId(p2Id).setStatus(StatusProposta.RECUSADA);

        assertEquals(1, service.contarPendentesPorStartup(startupId));

        service.criar(criarRequestValido(startupId, UUID.randomUUID()));
        service.criar(criarRequestValido(startupId, UUID.randomUUID()));

        assertEquals(3, service.contarPendentesPorStartup(startupId));
    }

    @Test
    void deveRejeitarRequisicaoNula() {
        assertThrows(ResponseStatusException.class, () -> service.criar(null));
    }

    @Test
    void deveRejeitarRequisicaoComAceiteFalso() {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        PropostaPocRequestDTO semAceite = new PropostaPocRequestDTO(
                startupId, desafioId, ESCOPO_PADRAO, 15, RESP_STARTUP, RESP_AMBIENTE, METRICA_PADRAO, false
        );
        assertThrows(ResponseStatusException.class, () -> service.criar(semAceite));
    }

    @Test
    void deveRejeitarRequisicaoComAceiteNulo() {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        PropostaPocRequestDTO aceiteNulo = new PropostaPocRequestDTO(
                startupId, desafioId, ESCOPO_PADRAO, 15, RESP_STARTUP, RESP_AMBIENTE, METRICA_PADRAO, null
        );
        assertThrows(ResponseStatusException.class, () -> service.criar(aceiteNulo));
    }

    @Test
    void deveRejeitarEscopoVazio() {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        PropostaPocRequestDTO escopoVazio = new PropostaPocRequestDTO(
                startupId, desafioId, "   ", 15, RESP_STARTUP, RESP_AMBIENTE, METRICA_PADRAO, true
        );
        assertThrows(ResponseStatusException.class, () -> service.criar(escopoVazio));
    }

    @Test
    void deveRejeitarMetricaVazia() {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        PropostaPocRequestDTO metricaVazia = new PropostaPocRequestDTO(
                startupId, desafioId, ESCOPO_PADRAO, 15, RESP_STARTUP, RESP_AMBIENTE, "   ", true
        );
        assertThrows(ResponseStatusException.class, () -> service.criar(metricaVazia));
    }

    @Test
    void deveRejeitarStartupNula() {
        UUID desafioId = UUID.randomUUID();
        PropostaPocRequestDTO startupNula = new PropostaPocRequestDTO(
                null, desafioId, ESCOPO_PADRAO, 15, RESP_STARTUP, RESP_AMBIENTE, METRICA_PADRAO, true
        );
        assertThrows(ResponseStatusException.class, () -> service.criar(startupNula));
    }

    @Test
    void deveRejeitarDesafioNulo() {
        UUID startupId = UUID.randomUUID();
        PropostaPocRequestDTO desafioNulo = new PropostaPocRequestDTO(
                startupId, null, ESCOPO_PADRAO, 15, RESP_STARTUP, RESP_AMBIENTE, METRICA_PADRAO, true
        );
        assertThrows(ResponseStatusException.class, () -> service.criar(desafioNulo));
    }

    @Test
    void deveRejeitarDuracaoInvalida() {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        PropostaPocRequestDTO duracaoInvalida = new PropostaPocRequestDTO(
                startupId, desafioId, ESCOPO_PADRAO, 0, RESP_STARTUP, RESP_AMBIENTE, METRICA_PADRAO, true
        );
        assertThrows(ResponseStatusException.class, () -> service.criar(duracaoInvalida));
    }

    @Test
    void deveRejeitarEscopoNulo() {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        PropostaPocRequestDTO escopoNulo = new PropostaPocRequestDTO(
                startupId, desafioId, null, 15, RESP_STARTUP, RESP_AMBIENTE, METRICA_PADRAO, true
        );
        assertThrows(ResponseStatusException.class, () -> service.criar(escopoNulo));
    }

    @Test
    void deveRejeitarMetricaNula() {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        PropostaPocRequestDTO metricaNula = new PropostaPocRequestDTO(
                startupId, desafioId, ESCOPO_PADRAO, 15, RESP_STARTUP, RESP_AMBIENTE, null, true
        );
        assertThrows(ResponseStatusException.class, () -> service.criar(metricaNula));
    }

    @Test
    void deveRejeitarDuracaoNula() {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        PropostaPocRequestDTO duracaoNula = new PropostaPocRequestDTO(
                startupId, desafioId, ESCOPO_PADRAO, null, RESP_STARTUP, RESP_AMBIENTE, METRICA_PADRAO, true
        );
        assertThrows(ResponseStatusException.class, () -> service.criar(duracaoNula));
    }

    @Test
    void naoDevePermitirPropostaParaDesafioComPropostaEmTeste() {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        PropostaPocResponseDTO propostaInicial = service.criar(criarRequestValido(startupId, desafioId));
        service.obterPorId(propostaInicial.id()).setStatus(StatusProposta.EM_TESTE);

        PropostaPocRequestDTO novaTentativa = criarRequestValido(startupId, desafioId);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.criar(novaTentativa));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void buscarEListarPropostas() {
        UUID startupId1 = UUID.randomUUID();
        UUID startupId2 = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        PropostaPocResponseDTO p1 = service.criar(criarRequestValido(startupId1, desafioId));
        service.criar(criarRequestValido(startupId2, desafioId));

        PropostaPocResponseDTO encontrada = service.buscar(p1.id());
        assertEquals(p1.id(), encontrada.id());

        List<PropostaPocResponseDTO> porStartup = service.listarPorStartup(startupId1);
        assertEquals(1, porStartup.size());
        assertEquals(p1.id(), porStartup.get(0).id());

        List<PropostaPocResponseDTO> porDesafio = service.listarPorDesafio(desafioId);
        assertEquals(2, porDesafio.size());

        List<PropostaPocResponseDTO> todas = service.listarTodas();
        assertEquals(2, todas.size());
    }

    @Test
    void buscarPropostaInexistenteDeveLancarNotFound() {
        UUID inexistente = UUID.randomUUID();
        assertThrows(ResponseStatusException.class, () -> service.buscar(inexistente));
    }

    @Test
    void cancelarPropostaInexistenteDeveLancarNotFound() {
        UUID inexistente = UUID.randomUUID();
        assertThrows(ResponseStatusException.class, () -> service.cancelar(inexistente));
    }

    @Test
    void cancelarPropostaNaoPendenteDeveLancarBadRequest() {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        PropostaPocResponseDTO p = service.criar(criarRequestValido(startupId, desafioId));
        UUID pId = p.id();
        service.obterPorId(pId).setStatus(StatusProposta.EM_TESTE);

        assertThrows(ResponseStatusException.class, () -> service.cancelar(pId));
    }

    @Test
    void contarPendentesComStartupNulaRetornaZero() {
        assertEquals(0, service.contarPendentesPorStartup(null));
    }

    @Test
    void obterPorIdComIdNuloDeveLancarBadRequest() {
        assertThrows(ResponseStatusException.class, () -> service.obterPorId(null));
    }

    @Test
    void listarPorStartupComIdNuloDeveLancarBadRequest() {
        assertThrows(ResponseStatusException.class, () -> service.listarPorStartup(null));
    }

    @Test
    void listarPorDesafioComIdNuloDeveLancarBadRequest() {
        assertThrows(ResponseStatusException.class, () -> service.listarPorDesafio(null));
    }

    @Test
    void listarFilaAmbienteRetornaApenasPropostasPendentesDosDesafiosEspecificados() {
        UUID startup1 = UUID.randomUUID();
        UUID startup2 = UUID.randomUUID();
        UUID desafio1 = UUID.randomUUID();
        UUID desafio2 = UUID.randomUUID();
        UUID outroDesafio = UUID.randomUUID();

        PropostaPocResponseDTO p1 = service.criar(criarRequestValido(startup1, desafio1));
        PropostaPocResponseDTO p2 = service.criar(criarRequestValido(startup2, desafio2));
        service.criar(criarRequestValido(startup1, outroDesafio));

        // Marca p1 em teste - nao deve mais aparecer na fila pendente
        service.obterPorId(p1.id()).setStatus(StatusProposta.EM_TESTE);

        List<PropostaPocResponseDTO> fila = service.listarFilaAmbiente(List.of(desafio1, desafio2));
        assertEquals(1, fila.size());
        assertEquals(p2.id(), fila.get(0).id());
        assertNotNull(fila.get(0).prazoRespostaAmbiente());
    }

    @Test
    void listarFilaAmbienteComListaNulaOuVaziaRetornaVazio() {
        assertTrue(service.listarFilaAmbiente(null).isEmpty());
        assertTrue(service.listarFilaAmbiente(List.of()).isEmpty());
    }

    @Test
    void recusarPropostaPendenteAtualizaStatusELiberaLimiteImediatamente() {
        UUID startupId = UUID.randomUUID();
        UUID d1 = UUID.randomUUID();
        UUID d2 = UUID.randomUUID();
        UUID d3 = UUID.randomUUID();
        UUID d4 = UUID.randomUUID();

        PropostaPocResponseDTO p1 = service.criar(criarRequestValido(startupId, d1));
        service.criar(criarRequestValido(startupId, d2));
        service.criar(criarRequestValido(startupId, d3));

        assertEquals(3, service.contarPendentesPorStartup(startupId));

        // 4a proposta bloqueada por atingir o limite
        PropostaPocRequestDTO p4 = criarRequestValido(startupId, d4);
        assertThrows(ResponseStatusException.class, () -> service.criar(p4));

        // Recusa motivada de p1
        PropostaPocResponseDTO recusada = service.recusar(p1.id(), "Fora do escopo do desafio");
        assertEquals(StatusProposta.RECUSADA, recusada.status());
        assertEquals("Fora do escopo do desafio", recusada.motivoRecusa());

        // Limite imediatamente liberado (2 pendencias)
        assertEquals(2, service.contarPendentesPorStartup(startupId));

        // Agora pode submeter a 4a proposta com sucesso
        PropostaPocResponseDTO criada4 = service.criar(p4);
        assertNotNull(criada4.id());
        assertEquals(3, service.contarPendentesPorStartup(startupId));
    }

    @Test
    void recusarPropostaComIdNuloLancaBadRequest() {
        assertThrows(ResponseStatusException.class, () -> service.recusar(null, "Motivo"));
    }

    @Test
    void recusarPropostaComMotivoNuloOuVazioLancaBadRequest() {
        UUID id = UUID.randomUUID();
        assertThrows(ResponseStatusException.class, () -> service.recusar(id, null));
        assertThrows(ResponseStatusException.class, () -> service.recusar(id, "   "));
    }

    @Test
    void recusarPropostaInexistenteLancaNotFound() {
        UUID inexistente = UUID.randomUUID();
        assertThrows(ResponseStatusException.class, () -> service.recusar(inexistente, "Motivo"));
    }

    @Test
    void recusarPropostaNaoPendenteLancaBadRequest() {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        PropostaPocResponseDTO p = service.criar(criarRequestValido(startupId, desafioId));

        UUID pId = p.id();
        service.obterPorId(pId).setStatus(StatusProposta.EM_TESTE);

        assertThrows(ResponseStatusException.class, () -> service.recusar(pId, "Motivo"));
    }

    @Test
    void listarFilaAmbienteOrdenaPorDataCriacaoAscendente() {
        UUID startup1 = UUID.randomUUID();
        UUID startup2 = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        PropostaPocRequestDTO req1 = criarRequestValido(startup1, desafioId);
        PropostaPocResponseDTO p1 = service.criar(req1);

        PropostaPocRequestDTO req2 = criarRequestValido(startup2, desafioId);
        PropostaPocResponseDTO p2 = service.criar(req2);

        service.obterPorId(p1.id()).setDataCriacao(LocalDateTime.now().minusHours(2));
        service.obterPorId(p2.id()).setDataCriacao(LocalDateTime.now().minusHours(1));

        List<PropostaPocResponseDTO> fila = service.listarFilaAmbiente(List.of(desafioId));
        assertEquals(2, fila.size());
        assertEquals(p1.id(), fila.get(0).id());
        assertEquals(p2.id(), fila.get(1).id());
    }

    @Test
    void listarFilaAmbienteIgnoraPropostasRecusadasCanceladasAceitas() {
        UUID s1 = UUID.randomUUID();
        UUID s2 = UUID.randomUUID();
        UUID s3 = UUID.randomUUID();
        UUID s4 = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        PropostaPocResponseDTO pPendente = service.criar(criarRequestValido(s1, desafioId));
        PropostaPocResponseDTO pRecusada = service.criar(criarRequestValido(s2, desafioId));
        PropostaPocResponseDTO pCancelada = service.criar(criarRequestValido(s3, desafioId));
        PropostaPocResponseDTO pEmTeste = service.criar(criarRequestValido(s4, desafioId));

        service.recusar(pRecusada.id(), "Recusa formal");
        service.cancelar(pCancelada.id());
        service.obterPorId(pEmTeste.id()).setStatus(StatusProposta.EM_TESTE);

        List<PropostaPocResponseDTO> fila = service.listarFilaAmbiente(List.of(desafioId));
        assertEquals(1, fila.size());
        assertEquals(pPendente.id(), fila.get(0).id());
        assertEquals(StatusProposta.PENDENTE, fila.get(0).status());
    }

    @Test
    void recusarPropostaJaRecusadaLancaBadRequest() {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        PropostaPocResponseDTO p = service.criar(criarRequestValido(startupId, desafioId));

        service.recusar(p.id(), "Primeira recusa");
        assertThrows(ResponseStatusException.class, () -> service.recusar(p.id(), "Segunda recusa"));
    }

    @Test
    void recusarPropostaCanceladaLancaBadRequest() {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        PropostaPocResponseDTO p = service.criar(criarRequestValido(startupId, desafioId));

        service.cancelar(p.id());
        assertThrows(ResponseStatusException.class, () -> service.recusar(p.id(), "Tentativa em cancelada"));
    }

    @Test
    void recusarPropostaPreservaDemaisCamposEAtualizaMotivo() {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        PropostaPocResponseDTO criada = service.criar(criarRequestValido(startupId, desafioId));

        PropostaPocResponseDTO recusada = service.recusar(criada.id(), "  Inviabilidade tecnica  ");
        assertEquals(criada.id(), recusada.id());
        assertEquals(startupId, recusada.startupId());
        assertEquals(desafioId, recusada.desafioValidacaoId());
        assertEquals(criada.escopoTeste(), recusada.escopoTeste());
        assertEquals(criada.duracaoSugeridaDias(), recusada.duracaoSugeridaDias());
        assertEquals(criada.responsabilidadesStartup(), recusada.responsabilidadesStartup());
        assertEquals(criada.responsabilidadesAmbiente(), recusada.responsabilidadesAmbiente());
        assertEquals(criada.metricaValidacao(), recusada.metricaValidacao());
        assertEquals(StatusProposta.RECUSADA, recusada.status());
        assertEquals("Inviabilidade tecnica", recusada.motivoRecusa());
    }
}
