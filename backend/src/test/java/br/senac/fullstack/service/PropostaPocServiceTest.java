package br.senac.fullstack.service;

import br.senac.fullstack.dto.PropostaPocRequestDTO;
import br.senac.fullstack.dto.PropostaPocResponseDTO;
import br.senac.fullstack.model.StatusProposta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropostaPocServiceTest {

    private PropostaPocService service;

    @BeforeEach
    void setUp() {
        service = new PropostaPocService();
    }

    private PropostaPocRequestDTO criarRequestValido(UUID startupId, UUID desafioId) {
        return new PropostaPocRequestDTO(
                startupId,
                desafioId,
                "Validacao de acuracia do modelo",
                15,
                "Fornecer endpoint e modelo de predicao",
                "Disponibilizar dataset anonimizado de teste",
                "Acuracia minima de 85%",
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

        service.obterPorId(p1.id()).setStatus(StatusProposta.EM_TESTE);
        service.obterPorId(p2.id()).setStatus(StatusProposta.RECUSADA);

        assertEquals(1, service.contarPendentesPorStartup(startupId));

        service.criar(criarRequestValido(startupId, UUID.randomUUID()));
        service.criar(criarRequestValido(startupId, UUID.randomUUID()));

        assertEquals(3, service.contarPendentesPorStartup(startupId));
    }

    @Test
    void deveRejeitarRequisicaoSemAceitePrevioOuInvalida() {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        assertThrows(ResponseStatusException.class, () -> service.criar(null));

        PropostaPocRequestDTO semAceite = new PropostaPocRequestDTO(
                startupId, desafioId, "Escopo", 15, "Resp", "Resp", "Metrica", false
        );
        assertThrows(ResponseStatusException.class, () -> service.criar(semAceite));

        PropostaPocRequestDTO aceiteNulo = new PropostaPocRequestDTO(
                startupId, desafioId, "Escopo", 15, "Resp", "Resp", "Metrica", null
        );
        assertThrows(ResponseStatusException.class, () -> service.criar(aceiteNulo));

        PropostaPocRequestDTO escopoVazio = new PropostaPocRequestDTO(
                startupId, desafioId, "   ", 15, "Resp", "Resp", "Metrica", true
        );
        assertThrows(ResponseStatusException.class, () -> service.criar(escopoVazio));

        PropostaPocRequestDTO metricaVazia = new PropostaPocRequestDTO(
                startupId, desafioId, "Escopo", 15, "Resp", "Resp", "   ", true
        );
        assertThrows(ResponseStatusException.class, () -> service.criar(metricaVazia));

        PropostaPocRequestDTO startupNula = new PropostaPocRequestDTO(
                null, desafioId, "Escopo", 15, "Resp", "Resp", "Metrica", true
        );
        assertThrows(ResponseStatusException.class, () -> service.criar(startupNula));

        PropostaPocRequestDTO desafioNulo = new PropostaPocRequestDTO(
                startupId, null, "Escopo", 15, "Resp", "Resp", "Metrica", true
        );
        assertThrows(ResponseStatusException.class, () -> service.criar(desafioNulo));

        PropostaPocRequestDTO duracaoInvalida = new PropostaPocRequestDTO(
                startupId, desafioId, "Escopo", 0, "Resp", "Resp", "Metrica", true
        );
        assertThrows(ResponseStatusException.class, () -> service.criar(duracaoInvalida));
    }

    @Test
    void buscarEListarPropostas() {
        UUID startupId1 = UUID.randomUUID();
        UUID startupId2 = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        PropostaPocResponseDTO p1 = service.criar(criarRequestValido(startupId1, desafioId));
        PropostaPocResponseDTO p2 = service.criar(criarRequestValido(startupId2, desafioId));

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
    void buscarOuCancelarPropostaInexistenteDeveLancarNotFound() {
        UUID inexistente = UUID.randomUUID();
        assertThrows(ResponseStatusException.class, () -> service.buscar(inexistente));
        assertThrows(ResponseStatusException.class, () -> service.cancelar(inexistente));
    }

    @Test
    void cancelarPropostaNaoPendenteDeveLancarBadRequest() {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        PropostaPocResponseDTO p = service.criar(criarRequestValido(startupId, desafioId));
        service.obterPorId(p.id()).setStatus(StatusProposta.EM_TESTE);

        assertThrows(ResponseStatusException.class, () -> service.cancelar(p.id()));
    }
}
