package br.senac.fullstack.controller;

import br.senac.fullstack.dto.RegistroOficialPocResponseDTO;
import br.senac.fullstack.model.AvaliacaoMetrica;
import br.senac.fullstack.model.ModoPrivacidadePoc;
import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.service.RegistroOficialPocService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegistroOficialPocController.class)
class RegistroOficialPocControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegistroOficialPocService registroOficialPocService;

    private RegistroOficialPocResponseDTO criarDTO(UUID id, String codigo) {
        return new RegistroOficialPocResponseDTO(
                id,
                codigo,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Escopo da POC testada",
                30,
                AvaliacaoMetrica.ATENDEU_TOTALMENTE,
                "Parecer positivo",
                ModoPrivacidadePoc.EXIBICAO_NOMINAL,
                "Interesse em contratacao",
                "Metrica KPI",
                "Resumo entrega",
                "Hospital Parceiro S.A.",
                "Saude",
                PorteOrganizacao.GRANDE,
                LocalDateTime.now(),
                false
        );
    }

    @Test
    @DisplayName("GET /registros-poc/{id} retorna 200 OK")
    void buscarPorIdRetorna200() throws Exception {
        UUID id = UUID.randomUUID();
        RegistroOficialPocResponseDTO dto = criarDTO(id, "POC-2026-ABC12345");
        when(registroOficialPocService.buscarPorId(id)).thenReturn(dto);

        mockMvc.perform(get("/registros-poc/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.codigoVerificador").value("POC-2026-ABC12345"))
                .andExpect(jsonPath("$.nomeOrganizacao").value("Hospital Parceiro S.A."));
    }

    @Test
    @DisplayName("GET /registros-poc/verificar/{codigoVerificador} retorna 200 OK")
    void verificarPorCodigoRetorna200() throws Exception {
        UUID id = UUID.randomUUID();
        String codigo = "POC-2026-XYZ98765";
        RegistroOficialPocResponseDTO dto = criarDTO(id, codigo);
        when(registroOficialPocService.buscarPorCodigoVerificador(codigo)).thenReturn(dto);

        mockMvc.perform(get("/registros-poc/verificar/{codigoVerificador}", codigo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoVerificador").value(codigo));
    }

    @Test
    @DisplayName("GET /pocs/{codigoOuId} funciona com UUID e com codigo alfanumerico")
    void buscarPocPublicaPorUuidECodigo() throws Exception {
        UUID id = UUID.randomUUID();
        String codigo = "POC-2026-PUB00001";
        RegistroOficialPocResponseDTO dto = criarDTO(id, codigo);

        when(registroOficialPocService.buscarPorId(id)).thenReturn(dto);
        when(registroOficialPocService.buscarPorCodigoVerificador(codigo)).thenReturn(dto);

        mockMvc.perform(get("/pocs/{codigoOuId}", id.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        mockMvc.perform(get("/pocs/{codigoOuId}", codigo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoVerificador").value(codigo));
    }

    @Test
    @DisplayName("GET /registros-poc/{id} inexistente retorna 404 Not Found")
    void buscarInexistenteRetorna404() throws Exception {
        UUID id = UUID.randomUUID();
        when(registroOficialPocService.buscarPorId(id))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro nao encontrado"));

        mockMvc.perform(get("/registros-poc/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
