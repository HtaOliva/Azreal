package br.senac.fullstack.dto;

import br.senac.fullstack.model.PropostaPoc;
import br.senac.fullstack.model.StatusProposta;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropostaPocDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void requestDTOValidoDevePassarNaValidacaoEConverterParaEntidade() {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();

        PropostaPocRequestDTO dto = new PropostaPocRequestDTO(
                startupId,
                desafioId,
                "Integracao de sensor IoT em esteira",
                30,
                "Fornecer hardware e firmware",
                "Fornecer infraestrutura e conectividade",
                "Disponibilidade > 99.5% e latencia < 200ms",
                true
        );

        Set<ConstraintViolation<PropostaPocRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());

        PropostaPoc entity = dto.toEntity();
        assertNotNull(entity.getId());
        assertEquals(startupId, entity.getStartupId());
        assertEquals(desafioId, entity.getDesafioValidacaoId());
        assertEquals("Integracao de sensor IoT em esteira", entity.getEscopoTeste());
        assertEquals(30, entity.getDuracaoSugeridaDias());
        assertEquals("Fornecer hardware e firmware", entity.getResponsabilidadesStartup());
        assertEquals("Fornecer infraestrutura e conectividade", entity.getResponsabilidadesAmbiente());
        assertEquals("Disponibilidade > 99.5% e latencia < 200ms", entity.getMetricaValidacao());
        assertTrue(entity.isAceitePrevioTermoSigilo());
        assertEquals(StatusProposta.PENDENTE, entity.getStatus());
        assertNotNull(entity.getDataCriacao());
    }

    @Test
    void requestDTOComEscopoEMetricaVaziosDeveFalharNaValidacao() {
        PropostaPocRequestDTO dto = new PropostaPocRequestDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "   ",
                15,
                "Resp startup",
                "Resp ambiente",
                "",
                true
        );

        Set<ConstraintViolation<PropostaPocRequestDTO>> violations = validator.validate(dto);
        assertEquals(2, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("escopoTeste")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("metricaValidacao")));
    }

    @Test
    void requestDTOComAceitePrevioFalsoOuNuloDeveFalharNaValidacao() {
        PropostaPocRequestDTO comFalso = new PropostaPocRequestDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Escopo valido",
                15,
                null,
                null,
                "Metrica valida",
                false
        );

        Set<ConstraintViolation<PropostaPocRequestDTO>> violationsFalso = validator.validate(comFalso);
        assertFalse(violationsFalso.isEmpty());
        assertTrue(violationsFalso.stream().anyMatch(v -> v.getPropertyPath().toString().equals("aceitePrevioTermoSigilo")));

        PropostaPocRequestDTO comNulo = new PropostaPocRequestDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Escopo valido",
                15,
                null,
                null,
                "Metrica valida",
                null
        );

        Set<ConstraintViolation<PropostaPocRequestDTO>> violationsNulo = validator.validate(comNulo);
        assertFalse(violationsNulo.isEmpty());
        assertTrue(violationsNulo.stream().anyMatch(v -> v.getPropertyPath().toString().equals("aceitePrevioTermoSigilo")));
    }

    @Test
    void requestDTOComCamposObrigatoriosNulosOuInvalidos() {
        PropostaPocRequestDTO dto = new PropostaPocRequestDTO(
                null,
                null,
                null,
                -5,
                null,
                null,
                null,
                true
        );

        Set<ConstraintViolation<PropostaPocRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("startupId")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("desafioValidacaoId")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("escopoTeste")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("duracaoSugeridaDias")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("metricaValidacao")));
    }

    @Test
    void responseDTOfromEntityDeveMapearCorretamenteTodosOsCampos() {
        UUID id = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        LocalDateTime agora = LocalDateTime.now();

        PropostaPoc proposta = new PropostaPoc();
        proposta.setId(id);
        proposta.setStartupId(startupId);
        proposta.setDesafioValidacaoId(desafioId);
        proposta.setEscopoTeste("Teste de visao computacional");
        proposta.setDuracaoSugeridaDias(15);
        proposta.setResponsabilidadesStartup("Instalar cameras e modelo");
        proposta.setResponsabilidadesAmbiente("Autorizar acesso a sala");
        proposta.setMetricaValidacao("Taxa de acerto > 95%");
        proposta.setAceitePrevioTermoSigilo(true);
        proposta.setStatus(StatusProposta.EM_TESTE);
        proposta.setDataCriacao(agora);

        PropostaPocResponseDTO dto = PropostaPocResponseDTO.fromEntity(proposta);

        assertEquals(id, dto.id());
        assertEquals(startupId, dto.startupId());
        assertEquals(desafioId, dto.desafioValidacaoId());
        assertEquals("Teste de visao computacional", dto.escopoTeste());
        assertEquals(15, dto.duracaoSugeridaDias());
        assertEquals("Instalar cameras e modelo", dto.responsabilidadesStartup());
        assertEquals("Autorizar acesso a sala", dto.responsabilidadesAmbiente());
        assertEquals("Taxa de acerto > 95%", dto.metricaValidacao());
        assertTrue(dto.aceitePrevioTermoSigilo());
        assertEquals(StatusProposta.EM_TESTE, dto.status());
        assertEquals(agora, dto.dataCriacao());
        assertNotNull(dto.prazoRespostaAmbiente());
    }

    @Test
    void responseDTOConstrutorLegadoSemPrazoDeveDefinirPrazoComoNulo() {
        UUID id = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        LocalDateTime agora = LocalDateTime.now();

        PropostaPocResponseDTO dto = new PropostaPocResponseDTO(
                id, startupId, desafioId, "Escopo", 15, "Resp S", "Resp A", "Metrica", true, StatusProposta.PENDENTE, agora
        );

        assertEquals(id, dto.id());
        org.junit.jupiter.api.Assertions.assertNull(dto.prazoRespostaAmbiente());
        org.junit.jupiter.api.Assertions.assertNull(dto.motivoRecusa());
    }

    @Test
    void recusaPropostaRequestDTOValidoDevePassarNaValidacaoETrimar() {
        RecusaPropostaRequestDTO dto = new RecusaPropostaRequestDTO("  Escopo incompativel com cronograma  ");
        Set<ConstraintViolation<RecusaPropostaRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
        assertEquals("Escopo incompativel com cronograma", dto.motivoRecusa());
    }

    @Test
    void recusaPropostaRequestDTOComMotivoVazioDeveFalharNaValidacao() {
        RecusaPropostaRequestDTO dto = new RecusaPropostaRequestDTO("   ");
        Set<ConstraintViolation<RecusaPropostaRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void recusaPropostaRequestDTOComMotivoNuloDeveFalharNaValidacao() {
        RecusaPropostaRequestDTO dto = new RecusaPropostaRequestDTO(null);
        Set<ConstraintViolation<RecusaPropostaRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void recusaPropostaRequestDTOComLimiteExato500CaracteresDevePassar() {
        String motivo500 = "a".repeat(500);
        RecusaPropostaRequestDTO dto = new RecusaPropostaRequestDTO(motivo500);
        Set<ConstraintViolation<RecusaPropostaRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
        assertEquals(500, dto.motivoRecusa().length());
    }

    @Test
    void recusaPropostaRequestDTOComMaisDe500CaracteresDeveFalharNaValidacao() {
        String motivo501 = "a".repeat(501);
        RecusaPropostaRequestDTO dto = new RecusaPropostaRequestDTO(motivo501);
        Set<ConstraintViolation<RecusaPropostaRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("motivoRecusa")));
    }
}
