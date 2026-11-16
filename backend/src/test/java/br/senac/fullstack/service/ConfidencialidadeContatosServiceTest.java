package br.senac.fullstack.service;

import br.senac.fullstack.dto.AmbienteValidacaoResponseDTO;
import br.senac.fullstack.dto.PropostaPocRequestDTO;
import br.senac.fullstack.dto.PropostaPocResponseDTO;
import br.senac.fullstack.dto.TermoPilotoResponseDTO;
import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.model.StatusProposta;
import br.senac.fullstack.model.TermoPiloto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Task 3.11 (Tests):
 * Testes no nível de domínio e serviço que garantem que os dados de contato do parceiro
 * permanecem inacessíveis no TermoPiloto até que o aceite seja executado, e são devidamente
 * desmascarados ao aceitar (RF15 / RNF04).
 */
class ConfidencialidadeContatosServiceTest {

    private static final String RAZAO_SOCIAL = "Complexo Hospitalar do Recife S.A.";
    private static final String NOME_FANTASIA = "Hospital Esperança";
    private static final String EMAIL_CONTATO = "parceria@hospitalesperanca.com.br";
    private static final String TELEFONE_CONTATO = "81988887777";

    private PropostaPocService propostaService;
    private TermoPilotoService termoService;

    @BeforeEach
    void setUp() {
        termoService = new TermoPilotoService(null);
        propostaService = new PropostaPocService();
    }

    private PropostaPocRequestDTO criarRequestValido(UUID startupId, UUID desafioId) {
        return new PropostaPocRequestDTO(
                startupId,
                desafioId,
                "Validação de modelo preditivo em ambiente de UTI",
                15,
                "Disponibilizar endpoint com latência inferior a 200ms",
                "Fornecer infraestrutura de telemetria segura",
                "Acurácia superior a 90%",
                true
        );
    }

    private AmbienteValidacaoResponseDTO criarAmbienteDTO(UUID ambienteId) {
        return new AmbienteValidacaoResponseDTO(
                ambienteId,
                RAZAO_SOCIAL,
                NOME_FANTASIA,
                "Saúde",
                PorteOrganizacao.GRANDE,
                "2611606",
                "Recife",
                EMAIL_CONTATO,
                TELEFONE_CONTATO,
                "https://linkedin.com/hospitalesperanca",
                true,
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("RNF04: Antes do aceite, proposta não possui termo gerado e busca lança NOT_FOUND")
    void antesDoAceiteNaoHaTermoEContatosNaoSaoRevelados() {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        PropostaPocResponseDTO propostaCriada = propostaService.criar(criarRequestValido(startupId, desafioId));
        UUID propostaId = propostaCriada.id();

        assertEquals(StatusProposta.PENDENTE, propostaCriada.status());

        // Antes do aceite, nenhuma informação de contato do ambiente parceiro é vinculada à proposta
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> propostaService.obterTermoPorPropostaId(propostaId));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Termo de piloto nao encontrado", ex.getReason());
    }

    @Test
    @DisplayName("RF15/RNF04: Após aceite, termo é congelado e contatos são revelados no TermoPilotoResponseDTO")
    void aposAceiteTermoCongeladoContemDadosEContatosSaoDesmascarados() {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();

        PropostaPocResponseDTO propostaCriada = propostaService.criar(criarRequestValido(startupId, desafioId));
        UUID propostaId = propostaCriada.id();

        // Execução do fluxo de aceite (RF15)
        TermoPiloto termoCongelado = propostaService.aceitar(propostaId, ambienteId);
        assertNotNull(termoCongelado);
        assertEquals(propostaId, termoCongelado.getPropostaPocId());
        assertEquals(startupId, termoCongelado.getStartupId());
        assertEquals(ambienteId, termoCongelado.getAmbienteValidacaoId());
        assertEquals(desafioId, termoCongelado.getDesafioValidacaoId());
        assertEquals("Validação de modelo preditivo em ambiente de UTI", termoCongelado.getEscopoTeste());
        assertEquals(15, termoCongelado.getDuracaoDias());
        assertEquals("Acurácia superior a 90%", termoCongelado.getMetricaValidacao());

        // Status transiciona para EM_TESTE
        PropostaPocResponseDTO propostaAtualizada = propostaService.buscar(propostaId);
        assertEquals(StatusProposta.EM_TESTE, propostaAtualizada.status());

        // Agora o termo existe e pode ser consultado
        TermoPiloto termoConsultado = propostaService.obterTermoPorPropostaId(propostaId);
        assertEquals(termoCongelado.getId(), termoConsultado.getId());

        // Construção do DTO revelando os contatos do ambiente
        AmbienteValidacaoResponseDTO ambienteDTO = criarAmbienteDTO(ambienteId);
        TermoPilotoResponseDTO responseDTO = termoService.construirResponseDTO(termoConsultado, ambienteDTO);

        assertNotNull(responseDTO);
        assertEquals(StatusProposta.EM_TESTE, responseDTO.statusProposta());
        // Contatos formalmente desmascarados e verificados
        assertEquals(RAZAO_SOCIAL, responseDTO.razaoSocial());
        assertEquals(NOME_FANTASIA, responseDTO.nomeFantasia());
        assertEquals(EMAIL_CONTATO, responseDTO.emailContato());
        assertEquals(TELEFONE_CONTATO, responseDTO.telefoneContato());
        assertEquals("Saúde", responseDTO.setor());
        assertEquals(PorteOrganizacao.GRANDE, responseDTO.porte());
        assertEquals(ambienteDTO, responseDTO.identidadeAmbienteRevelada());
    }

    @Test
    @DisplayName("RF15: Não é permitido aceitar proposta mais de uma vez ou que não esteja PENDENTE")
    void naoPermiteAceitarPropostaMaisDeUmaVez() {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();

        PropostaPocResponseDTO propostaCriada = propostaService.criar(criarRequestValido(startupId, desafioId));
        UUID propostaId = propostaCriada.id();

        // Primeiro aceite
        propostaService.aceitar(propostaId, ambienteId);

        // Segundo aceite deve falhar
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> propostaService.aceitar(propostaId, ambienteId));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Apenas propostas pendentes podem ser aceitas", ex.getReason());
    }
}
