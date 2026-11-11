package br.senac.fullstack.dto;

import br.senac.fullstack.model.AvaliacaoMetrica;
import br.senac.fullstack.model.ModoPrivacidadePoc;
import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.model.RegistroOficialPoc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistroOficialPocDTOTest {

    @Test
    @DisplayName("Deve mapear RegistroOficialPocResponseDTO com exibicao nominal")
    void deveMapearDTOExibicaoNominal() {
        RegistroOficialPoc registro = new RegistroOficialPoc();
        registro.setCodigoVerificador("POC-2026-ABC12345");
        registro.setTermoPilotoId(UUID.randomUUID());
        registro.setPropostaPocId(UUID.randomUUID());
        registro.setStartupId(UUID.randomUUID());
        registro.setAmbienteValidacaoId(UUID.randomUUID());
        registro.setDesafioValidacaoId(UUID.randomUUID());
        registro.setEscopoTestado("Deploy de solucao de IA");
        registro.setDuracaoDias(30);
        registro.setAvaliacaoMetrica(AvaliacaoMetrica.ATENDEU_TOTALMENTE);
        registro.setParecer("Parecer positivo");
        registro.setModoPrivacidade(ModoPrivacidadePoc.EXIBICAO_NOMINAL);
        registro.setNomeOrganizacao("Empresa Parceira S.A.");
        registro.setSetorOrganizacao("Logistica");
        registro.setPorteOrganizacao(PorteOrganizacao.GRANDE);

        RegistroOficialPocResponseDTO dto = RegistroOficialPocResponseDTO.fromEntity(registro);

        assertNotNull(dto);
        assertEquals(registro.getId(), dto.id());
        assertEquals("POC-2026-ABC12345", dto.codigoVerificador());
        assertEquals("Deploy de solucao de IA", dto.escopoTestado());
        assertEquals(30, dto.duracaoDias());
        assertEquals(AvaliacaoMetrica.ATENDEU_TOTALMENTE, dto.avaliacaoMetrica());
        assertEquals("Parecer positivo", dto.parecer());
        assertEquals(ModoPrivacidadePoc.EXIBICAO_NOMINAL, dto.modoPrivacidade());
        assertEquals("Empresa Parceira S.A.", dto.nomeOrganizacao());
        assertEquals("Logistica", dto.setorOrganizacao());
        assertEquals(PorteOrganizacao.GRANDE, dto.porteOrganizacao());
        assertFalse(dto.anonimo());
    }

    @Test
    @DisplayName("Deve mapear RegistroOficialPocResponseDTO com exibicao anonima qualificada")
    void deveMapearDTOExibicaoAnonimaQualificada() {
        RegistroOficialPoc registro = new RegistroOficialPoc();
        registro.setCodigoVerificador("POC-2026-XYZ98765");
        registro.setStartupId(UUID.randomUUID());
        registro.setEscopoTestado("Validacao de API");
        registro.setDuracaoDias(10);
        registro.setAvaliacaoMetrica(AvaliacaoMetrica.ATENDEU_PARCIALMENTE);
        registro.setParecer("Parecer confidencial");
        registro.setModoPrivacidade(ModoPrivacidadePoc.ANONIMO_QUALIFICADO);
        registro.setNomeOrganizacao("Hospital Secreto");
        registro.setSetorOrganizacao("Saude");
        registro.setPorteOrganizacao(PorteOrganizacao.MEDIO);

        RegistroOficialPocResponseDTO dto = RegistroOficialPocResponseDTO.fromEntity(registro);

        assertNotNull(dto);
        assertEquals("Organização Parceira Anônima", dto.nomeOrganizacao());
        assertEquals("Saude", dto.setorOrganizacao());
        assertEquals(PorteOrganizacao.MEDIO, dto.porteOrganizacao());
        assertTrue(dto.anonimo());
    }

    @Test
    @DisplayName("Deve mapear RegistroOficialPocDTO imutavel")
    void deveMapearRegistroOficialPocDTO() {
        assertNull(RegistroOficialPocDTO.fromEntity(null));
        assertNull(RegistroOficialPocResponseDTO.fromEntity(null));

        RegistroOficialPoc registro = new RegistroOficialPoc();
        registro.setCodigoVerificador("POC-2026-DTO00001");
        registro.setEscopoTestado("Escopo DTO");
        registro.setDuracaoDias(20);
        registro.setAvaliacaoMetrica(AvaliacaoMetrica.NAO_ATINGIU_METRICA);
        registro.setParecer("Parecer DTO");
        registro.setModoPrivacidade(ModoPrivacidadePoc.EXIBICAO_NOMINAL);
        registro.setNomeOrganizacao("Empresa Alpha");
        registro.setSetorOrganizacao("Varejo");
        registro.setPorteOrganizacao(PorteOrganizacao.PEQUENO);

        RegistroOficialPocDTO dto = RegistroOficialPocDTO.fromEntity(registro);

        assertNotNull(dto);
        assertEquals("POC-2026-DTO00001", dto.codigoVerificador());
        assertEquals("Escopo DTO", dto.escopoTestado());
        assertEquals(20, dto.duracaoDias());
        assertEquals(AvaliacaoMetrica.NAO_ATINGIU_METRICA, dto.avaliacaoMetrica());
        assertEquals("Parecer DTO", dto.parecer());
        assertEquals("Empresa Alpha", dto.nomeOrganizacao());
        assertEquals("Varejo", dto.setorOrganizacao());
        assertEquals(PorteOrganizacao.PEQUENO, dto.porteOrganizacao());
    }
}
