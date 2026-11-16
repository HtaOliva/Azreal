package br.senac.fullstack.dto;

import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.model.StatusProposta;
import br.senac.fullstack.model.TermoPiloto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TermoPilotoDTOTest {

    @Test
    @DisplayName("Deve mapear TermoPilotoResponseDTO a partir de TermoPiloto e AmbienteValidacao com revelação de identidade")
    void deveMapearResponseDTOCompleto() {
        UUID id = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();
        LocalDateTime agora = LocalDateTime.now();

        TermoPiloto termo = new TermoPiloto();
        termo.setId(id);
        termo.setPropostaPocId(propostaId);
        termo.setStartupId(startupId);
        termo.setDesafioValidacaoId(desafioId);
        termo.setAmbienteValidacaoId(ambienteId);
        termo.setEscopoTeste("Escopo congelado");
        termo.setDuracaoDias(30);
        termo.setResponsabilidadesStartup("Startup entrega APK");
        termo.setResponsabilidadesAmbiente("Ambiente fornece leitos");
        termo.setMetricaValidacao("Acuracia > 95%");
        termo.setDataCriacao(agora);

        AmbienteValidacaoResponseDTO ambiente = new AmbienteValidacaoResponseDTO(
                ambienteId,
                "Hospital Santa Joana S.A.",
                "Hospital Santa Joana",
                "Saúde",
                PorteOrganizacao.GRANDE,
                "2611606",
                "Recife",
                "contato@santajoana.com.br",
                "81999990001",
                "https://linkedin.com/santajoana",
                true,
                agora
        );

        TermoPilotoResponseDTO dto = TermoPilotoResponseDTO.fromEntity(termo, StatusProposta.EM_TESTE, ambiente);

        assertNotNull(dto);
        assertEquals(id, dto.id());
        assertEquals(propostaId, dto.propostaPocId());
        assertEquals(startupId, dto.startupId());
        assertEquals(desafioId, dto.desafioValidacaoId());
        assertEquals(ambienteId, dto.ambienteValidacaoId());
        assertEquals("Escopo congelado", dto.escopoTeste());
        assertEquals(30, dto.duracaoDias());
        assertEquals("Startup entrega APK", dto.responsabilidadesStartup());
        assertEquals("Ambiente fornece leitos", dto.responsabilidadesAmbiente());
        assertEquals("Acuracia > 95%", dto.metricaValidacao());
        assertEquals(agora, dto.dataCriacao());
        assertEquals(StatusProposta.EM_TESTE, dto.statusProposta());
        // Identidade revelada (RF15 / RNF04)
        assertEquals("Hospital Santa Joana S.A.", dto.razaoSocial());
        assertEquals("Hospital Santa Joana", dto.nomeFantasia());
        assertEquals("contato@santajoana.com.br", dto.emailContato());
        assertEquals("81999990001", dto.telefoneContato());
        assertEquals("Saúde", dto.setor());
        assertEquals(PorteOrganizacao.GRANDE, dto.porte());
        assertEquals(ambiente, dto.identidadeAmbienteRevelada());
    }

    @Test
    @DisplayName("Deve lidar com valores nulos ao mapear TermoPilotoResponseDTO")
    void deveLidarComValoresNulos() {
        assertNull(TermoPilotoResponseDTO.fromEntity(null, null));
        assertNull(TermoPilotoResponseDTO.fromEntity(null, StatusProposta.EM_TESTE, null));

        TermoPiloto termo = new TermoPiloto();
        TermoPilotoResponseDTO dto = TermoPilotoResponseDTO.fromEntity(termo, null);

        assertNotNull(dto);
        assertEquals(StatusProposta.EM_TESTE, dto.statusProposta());
        assertNull(dto.razaoSocial());
        assertNull(dto.nomeFantasia());
        assertNull(dto.emailContato());
        assertNull(dto.telefoneContato());
        assertNull(dto.identidadeAmbienteRevelada());
    }

    @Test
    @DisplayName("Deve permitir instanciar com construtor sobrecarregado sem identidade incorporada")
    void deveInstanciarComConstrutorSobrecarregado() {
        UUID id = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();
        LocalDateTime agora = LocalDateTime.now();

        TermoPilotoResponseDTO dto = new TermoPilotoResponseDTO(
                id, propostaId, startupId, desafioId, ambienteId,
                "Escopo", 20, "Resp Startup", "Resp Ambiente", "Metrica",
                agora, StatusProposta.EM_TESTE, "Razao", "Fantasia",
                "contato@email.com", "81999998888", "Tecnologia", PorteOrganizacao.MEDIO
        );

        assertEquals(id, dto.id());
        assertEquals("Razao", dto.razaoSocial());
        assertNull(dto.identidadeAmbienteRevelada());
    }
}
