package br.senac.fullstack.service;

import br.senac.fullstack.dto.AceiteTermoRequestDTO;
import br.senac.fullstack.dto.AmbienteValidacaoRequestDTO;
import br.senac.fullstack.dto.AmbienteValidacaoResponseDTO;
import br.senac.fullstack.dto.AmbienteValidacaoUpdateDTO;
import br.senac.fullstack.model.PorteOrganizacao;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AmbienteValidacaoServiceTest {

    private final AceiteTermoService aceiteService = new AceiteTermoService(new NullObjectProvider<>());

    private AmbienteValidacaoService service() {
        return new AmbienteValidacaoService(new NullObjectProvider<>(), aceiteService);
    }

    private UUID aceiteId() {
        return aceiteService.registrar(new AceiteTermoRequestDTO("v1", "127.0.0.1"), null, "127.0.0.1").id();
    }

    @Test
    void criarSemAceiteLancaBadRequest() {
        AmbienteValidacaoService s = service();
        AmbienteValidacaoRequestDTO req = new AmbienteValidacaoRequestDTO(
                "Hospital Alfa S.A.", "Hospital Alfa", "Saude", PorteOrganizacao.GRANDE,
                "PE", "Recife", "inovacao@hospitalalfa.com.br", "81999990000",
                "https://linkedin.com/company/hospitalalfa", "senha123", null);
        assertThrows(ResponseStatusException.class, () -> s.criar(req));
    }

    @Test
    void criarBuscarListarEAtualizarEmMemoriaFuncionam() {
        AmbienteValidacaoService s = service();
        assertTrue(s.listar().isEmpty());

        AmbienteValidacaoRequestDTO req = new AmbienteValidacaoRequestDTO(
                "Hospital Alfa S.A.", "Hospital Alfa", "Saude", PorteOrganizacao.GRANDE,
                "PE", "Recife", "inovacao@hospitalalfa.com.br", "81999990000",
                "https://linkedin.com/company/hospitalalfa", "senha123", aceiteId());
        AmbienteValidacaoResponseDTO criado = s.criar(req);
        assertNotNull(criado);
        assertEquals("Hospital Alfa", criado.nomeFantasia());

        AmbienteValidacaoResponseDTO buscado = s.buscar(criado.id());
        assertEquals("Saude", buscado.setor());
        assertFalse(s.listar().isEmpty());

        AmbienteValidacaoUpdateDTO update = new AmbienteValidacaoUpdateDTO(
                "Hospital Alfa Renovado S.A.", "Hospital Alfa Recife", "Saude Digital",
                PorteOrganizacao.INSTITUICAO_ENSINO_SAUDE, "PE", "Olinda",
                "contato@hospitalalfa.com.br", "81988887777", "https://linkedin.com/company/alfa2");
        AmbienteValidacaoResponseDTO atualizado = s.atualizar(criado.id(), update);
        assertEquals("Hospital Alfa Recife", atualizado.nomeFantasia());
        assertEquals("Olinda", atualizado.municipioIbge());
    }

    @Test
    void buscarOuAtualizarInexistenteLanca404() {
        AmbienteValidacaoService s = service();
        UUID id = UUID.randomUUID();
        AmbienteValidacaoUpdateDTO update = new AmbienteValidacaoUpdateDTO(
                null, null, null, null, null, null, null, null, null);
        assertThrows(ResponseStatusException.class, () -> s.buscar(id));
        assertThrows(ResponseStatusException.class, () -> s.atualizar(id, update));
    }
}
