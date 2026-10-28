package br.senac.fullstack.service;

import br.senac.fullstack.dto.StartupRequestDTO;
import br.senac.fullstack.dto.StartupResponseDTO;
import br.senac.fullstack.model.Estagio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StartupServiceTest {

    @Mock
    private AceiteTermoService aceiteTermoService;

    @Mock
    private ObjectProvider<br.senac.fullstack.repository.StartupRepository> repositoryProvider;

    @InjectMocks
    private StartupService startupService;

    @Test
    void criarSemAceiteLanca400() {
        UUID aceiteId = UUID.randomUUID();
        when(aceiteTermoService.existe(aceiteId)).thenReturn(false);

        StartupRequestDTO request = new StartupRequestDTO(
                "Startup X", "sx@example.com", "senha123", "desc", "tech",
                LocalDate.now(), Estagio.IDEACAO, java.util.List.of("Joao"),
                "PE-Recife", "PE-Recife", Boolean.FALSE, null, aceiteId);

        assertThrows(ResponseStatusException.class, () -> startupService.criar(request));
    }

    @Test
    void buscarInexistenteLanca404() {
        UUID id = UUID.randomUUID();
        assertThrows(ResponseStatusException.class, () -> startupService.buscar(id));
    }
}
