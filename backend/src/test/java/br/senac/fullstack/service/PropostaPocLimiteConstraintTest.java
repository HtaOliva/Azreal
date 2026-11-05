package br.senac.fullstack.service;

import br.senac.fullstack.dto.PropostaPocRequestDTO;
import br.senac.fullstack.dto.PropostaPocResponseDTO;
import br.senac.fullstack.model.StatusProposta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropostaPocLimiteConstraintTest {

    private PropostaPocService service;

    @BeforeEach
    void setUp() {
        service = new PropostaPocService();
    }

    private PropostaPocRequestDTO criarDTO(UUID startupId, UUID desafioId) {
        return new PropostaPocRequestDTO(
                startupId,
                desafioId,
                "Validar solucao de automacao no ambiente parceiro",
                15,
                "Fornecer software e suporte a operacao",
                "Disponibilizar estacao de trabalho e base de dados",
                "Tempo medio de resposta < 500ms em 95% das requisicoes",
                true
        );
    }

    @Test
    @DisplayName("Startup pode enviar ate exatamente 3 propostas pendentes simultaneas para desafios distintos")
    void devePermitirAteTresPropostasPendentesParaDesafiosDistintos() {
        UUID startupId = UUID.randomUUID();

        PropostaPocResponseDTO p1 = service.criar(criarDTO(startupId, UUID.randomUUID()));
        PropostaPocResponseDTO p2 = service.criar(criarDTO(startupId, UUID.randomUUID()));
        PropostaPocResponseDTO p3 = service.criar(criarDTO(startupId, UUID.randomUUID()));

        assertNotNull(p1.id());
        assertNotNull(p2.id());
        assertNotNull(p3.id());
        assertEquals(3, service.contarPendentesPorStartup(startupId));
    }

    @Test
    @DisplayName("Quarta proposta pendente para mesma startup deve ser rejeitada com Bad Request (RF13)")
    void deveRejeitarQuartaPropostaPendenteParaMesmaStartup() {
        UUID startupId = UUID.randomUUID();

        service.criar(criarDTO(startupId, UUID.randomUUID()));
        service.criar(criarDTO(startupId, UUID.randomUUID()));
        service.criar(criarDTO(startupId, UUID.randomUUID()));

        PropostaPocRequestDTO quartaProposta = criarDTO(startupId, UUID.randomUUID());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.criar(quartaProposta));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("limite maximo de 3 propostas pendentes simultaneas"));
        assertEquals(3, service.contarPendentesPorStartup(startupId));
    }

    @Test
    @DisplayName("Limite de 3 propostas pendentes e independente por startup")
    void limiteDeveSerIndependenteEntreStartupsDiferentes() {
        UUID startup1 = UUID.randomUUID();
        UUID startup2 = UUID.randomUUID();

        service.criar(criarDTO(startup1, UUID.randomUUID()));
        service.criar(criarDTO(startup1, UUID.randomUUID()));
        service.criar(criarDTO(startup1, UUID.randomUUID()));

        assertEquals(3, service.contarPendentesPorStartup(startup1));
        assertEquals(0, service.contarPendentesPorStartup(startup2));

        PropostaPocResponseDTO pStartup2 = service.criar(criarDTO(startup2, UUID.randomUUID()));
        assertNotNull(pStartup2.id());
        assertEquals(1, service.contarPendentesPorStartup(startup2));
    }

    @Test
    @DisplayName("Nao permite que uma startup proponha duas vezes para o mesmo desafio enquanto pendente")
    void devePrevenirPropostaDuplicadaParaOMesmoDesafio() {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        service.criar(criarDTO(startupId, desafioId));

        PropostaPocRequestDTO duplicada = new PropostaPocRequestDTO(
                startupId,
                desafioId,
                "Outro escopo para o mesmo desafio",
                30,
                "Outra responsabilidade",
                "Outra responsabilidade",
                "Outra metrica",
                true
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.criar(duplicada));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("ja possui proposta ativa para este desafio"));
        assertEquals(1, service.contarPendentesPorStartup(startupId));
    }

    @Test
    @DisplayName("Startups diferentes podem propor para o mesmo desafio sem conflito de duplicidade")
    void startupsDiferentesPodemProporParaOMesmoDesafio() {
        UUID startup1 = UUID.randomUUID();
        UUID startup2 = UUID.randomUUID();
        UUID desafioCompartilhado = UUID.randomUUID();

        PropostaPocResponseDTO p1 = service.criar(criarDTO(startup1, desafioCompartilhado));
        PropostaPocResponseDTO p2 = service.criar(criarDTO(startup2, desafioCompartilhado));

        assertNotNull(p1.id());
        assertNotNull(p2.id());
        assertEquals(1, service.contarPendentesPorStartup(startup1));
        assertEquals(1, service.contarPendentesPorStartup(startup2));
        assertEquals(2, service.listarPorDesafio(desafioCompartilhado).size());
    }

    @Test
    @DisplayName("Cancelamento de proposta pendente recupera imediatamente o slot no limite de 3 propostas")
    void cancelamentoDeveRecuperarSlotNoLimiteImediatamente() {
        UUID startupId = UUID.randomUUID();
        UUID desafio1 = UUID.randomUUID();
        UUID desafio2 = UUID.randomUUID();
        UUID desafio3 = UUID.randomUUID();
        UUID desafioNovo = UUID.randomUUID();

        PropostaPocResponseDTO p1 = service.criar(criarDTO(startupId, desafio1));
        service.criar(criarDTO(startupId, desafio2));
        service.criar(criarDTO(startupId, desafio3));

        assertEquals(3, service.contarPendentesPorStartup(startupId));

        // Rejeita a 4ª proposta antes de cancelar
        assertThrows(ResponseStatusException.class, () -> service.criar(criarDTO(startupId, desafioNovo)));

        // Cancela a primeira proposta
        PropostaPocResponseDTO cancelada = service.cancelar(p1.id());
        assertEquals(StatusProposta.CANCELADA, cancelada.status());
        assertEquals(2, service.contarPendentesPorStartup(startupId));

        // Slot recuperado: agora a nova proposta é aceita com sucesso
        PropostaPocResponseDTO aceita = service.criar(criarDTO(startupId, desafioNovo));
        assertNotNull(aceita.id());
        assertEquals(StatusProposta.PENDENTE, aceita.status());
        assertEquals(3, service.contarPendentesPorStartup(startupId));
    }

    @Test
    @DisplayName("Apos cancelamento, startup pode propor novamente para o mesmo desafio")
    void aposCancelamentoPermiteProporNovamenteParaOMesmoDesafio() {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        PropostaPocResponseDTO original = service.criar(criarDTO(startupId, desafioId));
        assertEquals(StatusProposta.PENDENTE, original.status());

        service.cancelar(original.id());
        assertEquals(0, service.contarPendentesPorStartup(startupId));

        PropostaPocResponseDTO nova = service.criar(criarDTO(startupId, desafioId));
        assertNotNull(nova.id());
        assertEquals(StatusProposta.PENDENTE, nova.status());
        assertEquals(1, service.contarPendentesPorStartup(startupId));
    }

    @Test
    @DisplayName("Propostas que mudam para EM_TESTE ou RECUSADA tambem recuperam o slot de pendencias")
    void mudancaDeStatusParaEmTesteOuRecusadaRecuperaSlotDePendencia() {
        UUID startupId = UUID.randomUUID();

        PropostaPocResponseDTO p1 = service.criar(criarDTO(startupId, UUID.randomUUID()));
        PropostaPocResponseDTO p2 = service.criar(criarDTO(startupId, UUID.randomUUID()));
        service.criar(criarDTO(startupId, UUID.randomUUID()));

        assertEquals(3, service.contarPendentesPorStartup(startupId));

        // Simula aceite pelo ambiente (passa para EM_TESTE) e recusa (RECUSADA)
        service.obterPorId(p1.id()).setStatus(StatusProposta.EM_TESTE);
        service.obterPorId(p2.id()).setStatus(StatusProposta.RECUSADA);

        assertEquals(1, service.contarPendentesPorStartup(startupId));

        // Pode submeter mais duas propostas pendentes
        service.criar(criarDTO(startupId, UUID.randomUUID()));
        service.criar(criarDTO(startupId, UUID.randomUUID()));
        assertEquals(3, service.contarPendentesPorStartup(startupId));

        // Agora uma sexta proposta estoura o limite de pendencias novamente
        assertThrows(ResponseStatusException.class, () -> service.criar(criarDTO(startupId, UUID.randomUUID())));
    }
}
