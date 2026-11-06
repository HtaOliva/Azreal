package br.senac.fullstack.controller;

import br.senac.fullstack.dto.AmbienteValidacaoResponseDTO;
import br.senac.fullstack.dto.DesafioAnonimoResponseDTO;
import br.senac.fullstack.dto.DesafioValidacaoResponseDTO;
import br.senac.fullstack.dto.PropostaPocResponseDTO;
import br.senac.fullstack.dto.StartupResponseDTO;
import br.senac.fullstack.dto.TermoPilotoResponseDTO;
import br.senac.fullstack.model.AmbienteValidacao;
import br.senac.fullstack.model.DesafioValidacao;
import br.senac.fullstack.model.Estagio;
import br.senac.fullstack.model.ModalidadeTeste;
import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.model.StatusDesafio;
import br.senac.fullstack.model.StatusProposta;
import br.senac.fullstack.model.TermoPiloto;
import br.senac.fullstack.service.AmbienteValidacaoService;
import br.senac.fullstack.service.DesafioValidacaoService;
import br.senac.fullstack.service.PropostaPocService;
import br.senac.fullstack.service.StartupService;
import br.senac.fullstack.service.TermoPilotoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Task 3.11 (Tests):
 * Teste que valida que os contatos do Ambiente de Validação permanecem confidenciais
 * até que o endpoint /aceitar seja invocado, sendo verificados desmascarados após o aceite (RF15 / RNF04).
 */
@WebMvcTest(controllers = {PropostaPocController.class, TermoPilotoController.class, DesafioValidacaoController.class})
class ConfidencialidadeContatosRf15Test {

    private static final String STARTUP_EMAIL = "startup@azreal.com";
    private static final String OUTRA_STARTUP_EMAIL = "outra.startup@azreal.com";
    private static final String AMBIENTE_EMAIL = "diretoria@hospitalesperanca.com.br";
    private static final String OUTRO_AMBIENTE_EMAIL = "contato@clinicaoutro.com.br";
    private static final String ADMIN_EMAIL = "admin@azreal.com";

    private static final String RAZAO_SOCIAL_CONFIDENCIAL = "Hospital Esperanca S.A.";
    private static final String NOME_FANTASIA_CONFIDENCIAL = "Hospital Esperanca Recife";
    private static final String EMAIL_CONTATO_CONFIDENCIAL = "diretoria@hospitalesperanca.com.br";
    private static final String TELEFONE_CONTATO_CONFIDENCIAL = "81999998888";

    private static final String URI_DESAFIOS = "/desafios";
    private static final String URI_PROPOSTA_ID = "/propostas-poc/{id}";
    private static final String URI_ACEITAR = "/propostas-poc/{id}/aceitar";
    private static final String URI_TERMOS_PROPOSTA = "/termos-piloto/{propostaId}";
    private static final String URI_PROPOSTA_TERMO = "/propostas-poc/{id}/termo";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PropostaPocService propostaPocService;

    @MockBean
    private TermoPilotoService termoPilotoService;

    @MockBean
    private DesafioValidacaoService desafioValidacaoService;

    @MockBean
    private AmbienteValidacaoService ambienteValidacaoService;

    @MockBean
    private StartupService startupService;

    private AmbienteValidacaoResponseDTO criarAmbienteCompletoDTO(UUID id) {
        return new AmbienteValidacaoResponseDTO(
                id,
                RAZAO_SOCIAL_CONFIDENCIAL,
                NOME_FANTASIA_CONFIDENCIAL,
                "Saúde",
                PorteOrganizacao.GRANDE,
                "2611606",
                "Recife",
                EMAIL_CONTATO_CONFIDENCIAL,
                TELEFONE_CONTATO_CONFIDENCIAL,
                "https://linkedin.com/hospitalesperanca",
                true,
                LocalDateTime.now()
        );
    }

    private StartupResponseDTO criarStartupDTO(UUID id, String email) {
        return new StartupResponseDTO(
                id,
                "MedTech Soluções",
                email,
                "Plataforma de triagem hospitalar inteligente",
                "Saúde",
                LocalDate.of(2023, 1, 15),
                Estagio.MVP_FUNCIONAL,
                List.of("Dra. Beatriz", "Carlos Eng"),
                "Recife",
                "Pernambuco",
                true,
                "Porto Digital",
                LocalDateTime.now()
        );
    }

    private DesafioAnonimoResponseDTO criarDesafioAnonimoDTO(UUID id) {
        return new DesafioAnonimoResponseDTO(
                id,
                "Triagem de Pacientes em Emergência",
                "Reduzir tempo de espera na triagem de emergência",
                "Saúde",
                PorteOrganizacao.GRANDE,
                "2611606",
                "Recife",
                ModalidadeTeste.PRESENCIAL,
                "Pacientes SUS",
                500,
                30,
                StatusDesafio.ABERTO,
                LocalDateTime.now()
        );
    }

    private PropostaPocResponseDTO criarPropostaPendenteDTO(UUID propostaId, UUID startupId, UUID desafioId) {
        return new PropostaPocResponseDTO(
                propostaId,
                startupId,
                desafioId,
                "Implementação do modelo de IA para classificação de risco Manchester",
                20,
                "Startup disponibiliza modelo pré-treinado e API de integração",
                "Ambiente disponibiliza prontuários anonimizados e acompanhamento médico",
                "Acurácia superior a 92% com sensibilidade mínima de 95%",
                true,
                StatusProposta.PENDENTE,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7)
        );
    }

    private TermoPiloto criarTermoPilotoCongelado(
            UUID id, UUID propostaId, UUID startupId, UUID desafioId, UUID ambienteId) {
        TermoPiloto termo = new TermoPiloto();
        termo.setId(id);
        termo.setPropostaPocId(propostaId);
        termo.setStartupId(startupId);
        termo.setDesafioValidacaoId(desafioId);
        termo.setAmbienteValidacaoId(ambienteId);
        termo.setEscopoTeste("Implementação do modelo de IA para classificação de risco Manchester");
        termo.setDuracaoDias(20);
        termo.setResponsabilidadesStartup("Startup disponibiliza modelo pré-treinado e API de integração");
        termo.setResponsabilidadesAmbiente("Ambiente disponibiliza prontuários anonimizados e acompanhamento médico");
        termo.setMetricaValidacao("Acurácia superior a 92% com sensibilidade mínima de 95%");
        termo.setDataCriacao(LocalDateTime.now());
        return termo;
    }

    private DesafioValidacaoResponseDTO criarDesafioDTO(UUID id, UUID ambienteId) {
        DesafioValidacao desafio = new DesafioValidacao();
        desafio.setId(id);
        AmbienteValidacao amb = new AmbienteValidacao();
        amb.setId(ambienteId);
        desafio.setAmbienteValidacao(amb);
        desafio.setTitulo("Triagem de Pacientes em Emergência");
        desafio.setStatus(StatusDesafio.ABERTO);
        desafio.setAtivo(true);
        desafio.setDataCriacao(LocalDateTime.now());
        return DesafioValidacaoResponseDTO.fromEntity(desafio);
    }

    @Test
    @DisplayName("RF06/RNF04: Desafios públicos ativos devem ocultar razão social, nome fantasia e contatos do parceiro")
    void desafiosPublicosNaoExpoemContatosDoParceiro() throws Exception {
        UUID desafioId = UUID.randomUUID();
        when(desafioValidacaoService.listarAtivosAnonimos())
                .thenReturn(List.of(criarDesafioAnonimoDTO(desafioId)));

        mockMvc.perform(get(URI_DESAFIOS)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Triagem de Pacientes em Emergência"))
                .andExpect(jsonPath("$[0].setor").value("Saúde"))
                .andExpect(jsonPath("$[0].porte").value("GRANDE"))
                // Confidencialidade garantida: campos nominais e de contato não devem existir no payload
                .andExpect(jsonPath("$[0].razaoSocial").doesNotExist())
                .andExpect(jsonPath("$[0].nomeFantasia").doesNotExist())
                .andExpect(jsonPath("$[0].emailContato").doesNotExist())
                .andExpect(jsonPath("$[0].telefoneContato").doesNotExist())
                .andExpect(jsonPath("$[0].cnpj").doesNotExist());
    }

    @Test
    @DisplayName("RNF04: Antes do aceite, consulta da proposta de POC pendente não expõe contatos do ambiente")
    @WithMockUser(username = STARTUP_EMAIL, roles = {"STARTUP"})
    void propostaPendenteNaoExpeContatosDoParceiro() throws Exception {
        UUID propostaId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        when(propostaPocService.buscar(propostaId))
                .thenReturn(criarPropostaPendenteDTO(propostaId, startupId, desafioId));

        mockMvc.perform(get(URI_PROPOSTA_ID, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(propostaId.toString()))
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.escopoTeste").exists())
                // Confidencialidade: a proposta não possui nem expõe contatos do ambiente parceiro
                .andExpect(jsonPath("$.razaoSocial").doesNotExist())
                .andExpect(jsonPath("$.nomeFantasia").doesNotExist())
                .andExpect(jsonPath("$.emailContato").doesNotExist())
                .andExpect(jsonPath("$.telefoneContato").doesNotExist());
    }

    @Test
    @DisplayName("RNF04: Antes do aceite, consultar /termos-piloto retorna 404 e contatos permanecem protegidos")
    @WithMockUser(username = STARTUP_EMAIL, roles = {"STARTUP"})
    void consultaTermoAntesDoAceiteRetornaNotFoundMantendoSigilo() throws Exception {
        UUID propostaId = UUID.randomUUID();

        // Antes de aceitar, não há TermoPiloto gerado
        when(termoPilotoService.buscarPorPropostaId(propostaId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Termo de piloto nao encontrado"));

        mockMvc.perform(get(URI_TERMOS_PROPOSTA, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("RF15/RNF04: Ao invocar /aceitar, transiciona para EM_TESTE e revela os contatos do ambiente pela primeira vez")
    @WithMockUser(username = AMBIENTE_EMAIL, roles = {"AMBIENTE_VALIDACAO"})
    void aoInvocarAceitarStatusMudaParaEmTesteEContatosSaoDesmascarados() throws Exception {
        UUID propostaId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();

        AmbienteValidacaoResponseDTO ambienteCompleto = criarAmbienteCompletoDTO(ambienteId);
        when(ambienteValidacaoService.buscarPorEmail(AMBIENTE_EMAIL)).thenReturn(ambienteCompleto);
        when(ambienteValidacaoService.buscar(ambienteId)).thenReturn(ambienteCompleto);

        PropostaPocResponseDTO propostaPendente = criarPropostaPendenteDTO(propostaId, startupId, desafioId);
        when(propostaPocService.buscar(propostaId)).thenReturn(propostaPendente);
        when(desafioValidacaoService.buscar(desafioId)).thenReturn(criarDesafioDTO(desafioId, ambienteId));

        TermoPiloto termoCongelado = criarTermoPilotoCongelado(
                UUID.randomUUID(), propostaId, startupId, desafioId, ambienteId);
        when(propostaPocService.aceitar(propostaId, ambienteId)).thenReturn(termoCongelado);

        // Invocação de POST /propostas-poc/{id}/aceitar
        mockMvc.perform(post(URI_ACEITAR, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Status transicionado para EM_TESTE
                .andExpect(jsonPath("$.statusProposta").value("EM_TESTE"))
                // Escopo e métricas congelados
                .andExpect(jsonPath("$.propostaPocId").value(propostaId.toString()))
                .andExpect(jsonPath("$.escopoTeste").value(termoCongelado.getEscopoTeste()))
                .andExpect(jsonPath("$.duracaoDias").value(20))
                .andExpect(jsonPath("$.metricaValidacao").value(termoCongelado.getMetricaValidacao()))
                // Contatos e identidade revelados pela primeira vez (RF15)
                .andExpect(jsonPath("$.razaoSocial").value(RAZAO_SOCIAL_CONFIDENCIAL))
                .andExpect(jsonPath("$.nomeFantasia").value(NOME_FANTASIA_CONFIDENCIAL))
                .andExpect(jsonPath("$.emailContato").value(EMAIL_CONTATO_CONFIDENCIAL))
                .andExpect(jsonPath("$.telefoneContato").value(TELEFONE_CONTATO_CONFIDENCIAL))
                .andExpect(jsonPath("$.setor").value("Saúde"))
                .andExpect(jsonPath("$.porte").value("GRANDE"))
                // Objeto aninhado de identidade revelada também conferido
                .andExpect(jsonPath("$.identidadeAmbienteRevelada.razaoSocial").value(RAZAO_SOCIAL_CONFIDENCIAL))
                .andExpect(jsonPath("$.identidadeAmbienteRevelada.emailContato").value(EMAIL_CONTATO_CONFIDENCIAL))
                .andExpect(jsonPath("$.identidadeAmbienteRevelada.telefoneContato").value(TELEFONE_CONTATO_CONFIDENCIAL));
    }

    @Test
    @DisplayName("RF15/RNF04: Após aceite, Startup proprietária consulta /termos-piloto e obtém contatos desmascarados")
    @WithMockUser(username = STARTUP_EMAIL, roles = {"STARTUP"})
    void aposAceiteStartupProprietariaConsultaContatosReveladosComSucesso() throws Exception {
        UUID propostaId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        TermoPiloto termo = criarTermoPilotoCongelado(UUID.randomUUID(), propostaId, startupId, desafioId, ambienteId);
        when(termoPilotoService.buscarPorPropostaId(propostaId)).thenReturn(termo);
        when(startupService.buscarPorEmail(STARTUP_EMAIL)).thenReturn(criarStartupDTO(startupId, STARTUP_EMAIL));
        when(ambienteValidacaoService.buscar(ambienteId)).thenReturn(criarAmbienteCompletoDTO(ambienteId));

        mockMvc.perform(get(URI_TERMOS_PROPOSTA, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusProposta").value("EM_TESTE"))
                .andExpect(jsonPath("$.razaoSocial").value(RAZAO_SOCIAL_CONFIDENCIAL))
                .andExpect(jsonPath("$.nomeFantasia").value(NOME_FANTASIA_CONFIDENCIAL))
                .andExpect(jsonPath("$.emailContato").value(EMAIL_CONTATO_CONFIDENCIAL))
                .andExpect(jsonPath("$.telefoneContato").value(TELEFONE_CONTATO_CONFIDENCIAL));
    }

    @Test
    @DisplayName("RF15/RNF04: Após aceite, endpoint alternativo /propostas-poc/{id}/termo também retorna contatos para Startup")
    @WithMockUser(username = STARTUP_EMAIL, roles = {"STARTUP"})
    void aposAceiteEndpointAlternativoRetornaContatosDesmascarados() throws Exception {
        UUID propostaId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        TermoPiloto termo = criarTermoPilotoCongelado(UUID.randomUUID(), propostaId, startupId, desafioId, ambienteId);
        when(propostaPocService.obterTermoPorPropostaId(propostaId)).thenReturn(termo);
        when(startupService.buscarPorEmail(STARTUP_EMAIL)).thenReturn(criarStartupDTO(startupId, STARTUP_EMAIL));
        when(ambienteValidacaoService.buscar(ambienteId)).thenReturn(criarAmbienteCompletoDTO(ambienteId));

        mockMvc.perform(get(URI_PROPOSTA_TERMO, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusProposta").value("EM_TESTE"))
                .andExpect(jsonPath("$.razaoSocial").value(RAZAO_SOCIAL_CONFIDENCIAL))
                .andExpect(jsonPath("$.emailContato").value(EMAIL_CONTATO_CONFIDENCIAL))
                .andExpect(jsonPath("$.telefoneContato").value(TELEFONE_CONTATO_CONFIDENCIAL));
    }

    @Test
    @DisplayName("RNF04: Após aceite, outra Startup não pode consultar os contatos revelados (retorna 403 Forbidden)")
    @WithMockUser(username = OUTRA_STARTUP_EMAIL, roles = {"STARTUP"})
    void aposAceiteOutraStartupNaoTemAcessoAosContatosReveladosRetornaForbidden() throws Exception {
        UUID propostaId = UUID.randomUUID();
        UUID startupProprietariaId = UUID.randomUUID();
        UUID outraStartupId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        TermoPiloto termo = criarTermoPilotoCongelado(
                UUID.randomUUID(), propostaId, startupProprietariaId, desafioId, ambienteId);
        when(termoPilotoService.buscarPorPropostaId(propostaId)).thenReturn(termo);
        when(startupService.buscarPorEmail(OUTRA_STARTUP_EMAIL))
                .thenReturn(criarStartupDTO(outraStartupId, OUTRA_STARTUP_EMAIL));

        mockMvc.perform(get(URI_TERMOS_PROPOSTA, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RNF04: Após aceite, outro Ambiente de Validação não pode consultar contatos (retorna 403 Forbidden)")
    @WithMockUser(username = OUTRO_AMBIENTE_EMAIL, roles = {"AMBIENTE_VALIDACAO"})
    void aposAceiteOutroAmbienteNaoTemAcessoAosContatosRetornaForbidden() throws Exception {
        UUID propostaId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID ambienteProprietarioId = UUID.randomUUID();
        UUID outroAmbienteId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        TermoPiloto termo = criarTermoPilotoCongelado(
                UUID.randomUUID(), propostaId, startupId, desafioId, ambienteProprietarioId);
        when(termoPilotoService.buscarPorPropostaId(propostaId)).thenReturn(termo);
        when(ambienteValidacaoService.buscarPorEmail(OUTRO_AMBIENTE_EMAIL))
                .thenReturn(criarAmbienteCompletoDTO(outroAmbienteId));

        mockMvc.perform(get(URI_TERMOS_PROPOSTA, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RF15: Administrador pode consultar termo de qualquer proposta aceita e ver contatos revelados")
    @WithMockUser(username = ADMIN_EMAIL, roles = {"ADMIN"})
    void adminPodeConsultarContatosReveladosDeQualquerPropostaAceita() throws Exception {
        UUID propostaId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        TermoPiloto termo = criarTermoPilotoCongelado(UUID.randomUUID(), propostaId, startupId, desafioId, ambienteId);
        when(termoPilotoService.buscarPorPropostaId(propostaId)).thenReturn(termo);
        when(ambienteValidacaoService.buscar(ambienteId)).thenReturn(criarAmbienteCompletoDTO(ambienteId));

        mockMvc.perform(get(URI_TERMOS_PROPOSTA, propostaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusProposta").value("EM_TESTE"))
                .andExpect(jsonPath("$.razaoSocial").value(RAZAO_SOCIAL_CONFIDENCIAL))
                .andExpect(jsonPath("$.emailContato").value(EMAIL_CONTATO_CONFIDENCIAL));
    }
}
