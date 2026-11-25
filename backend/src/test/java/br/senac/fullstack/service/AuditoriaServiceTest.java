package br.senac.fullstack.service;

import br.senac.fullstack.dto.RegistroAuditoriaResponseDTO;
import br.senac.fullstack.model.RegistroAuditoria;
import br.senac.fullstack.model.StatusProposta;
import br.senac.fullstack.repository.RegistroAuditoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    private RegistroAuditoriaRepository repository;

    @Mock
    private ObjectProvider<RegistroAuditoriaRepository> repositoryProvider;

    private AuditoriaService auditoriaService;

    @BeforeEach
    void setUp() {
        auditoriaService = new AuditoriaService(repositoryProvider);
    }

    @Test
    @DisplayName("Registrar auditoria com sucesso persistindo no repositorio")
    void registrarComSucessoRepositorio() {
        when(repositoryProvider.getIfAvailable()).thenReturn(repository);
        when(repository.save(any(RegistroAuditoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UUID propostaId = UUID.randomUUID();
        UUID termoId = UUID.randomUUID();

        RegistroAuditoriaResponseDTO response = auditoriaService.registrar(
                propostaId,
                termoId,
                StatusProposta.PENDENTE,
                StatusProposta.EM_TESTE,
                "admin@azreal.com",
                "ACEITE_TERMO",
                "Termo aceito com sucesso"
        );

        assertNotNull(response);
        assertEquals(propostaId, response.propostaPocId());
        assertEquals(termoId, response.termoPilotoId());
        assertEquals(StatusProposta.PENDENTE, response.statusAnterior());
        assertEquals(StatusProposta.EM_TESTE, response.statusNovo());
        assertEquals("admin@azreal.com", response.usuarioResponsavel());
        assertEquals("ACEITE_TERMO", response.acao());
    }

    @Test
    @DisplayName("Registrar com propostaId nulo retorna nulo")
    void registrarComPropostaNula() {
        assertNull(auditoriaService.registrar(null, null, null, null, null, null, null));
    }

    @Test
    @DisplayName("Registrar auditoria com fallback em memoria quando repositorio lanca excecao")
    void registrarComFallbackMemoria() {
        when(repositoryProvider.getIfAvailable()).thenReturn(repository);
        when(repository.save(any(RegistroAuditoria.class))).thenThrow(new RuntimeException("Database error"));

        UUID propostaId = UUID.randomUUID();
        RegistroAuditoriaResponseDTO response = auditoriaService.registrar(
                propostaId,
                null,
                null,
                StatusProposta.PENDENTE,
                null,
                null,
                "Criacao em memoria"
        );

        assertNotNull(response);
        assertEquals(propostaId, response.propostaPocId());
        assertEquals("SISTEMA", response.usuarioResponsavel());
        assertEquals("TRANSICAO_STATUS", response.acao());

        // Consulta via memoria
        when(repository.findByPropostaPocIdOrderByDataHoraAsc(propostaId)).thenThrow(new RuntimeException("DB error"));
        List<RegistroAuditoriaResponseDTO> lista = auditoriaService.listarPorProposta(propostaId);
        assertEquals(1, lista.size());
    }

    @Test
    @DisplayName("Listar por proposta nula ou termo nulo retorna lista vazia")
    void listarComIdNulo() {
        assertTrue(auditoriaService.listarPorProposta(null).isEmpty());
        assertTrue(auditoriaService.listarPorTermo(null).isEmpty());
    }

    @Test
    @DisplayName("Listar por termo via repositorio")
    void listarPorTermoRepositorio() {
        UUID termoId = UUID.randomUUID();
        RegistroAuditoria audit = new RegistroAuditoria(UUID.randomUUID(), termoId,
                StatusProposta.EM_TESTE, StatusProposta.POC_CONCLUIDA, "partner@azreal.com", "CONFIRMAR", "Ok");

        when(repositoryProvider.getIfAvailable()).thenReturn(repository);
        when(repository.findByTermoPilotoIdOrderByDataHoraAsc(termoId)).thenReturn(List.of(audit));

        List<RegistroAuditoriaResponseDTO> lista = auditoriaService.listarPorTermo(termoId);
        assertEquals(1, lista.size());
        assertEquals(termoId, lista.get(0).termoPilotoId());
    }

    @Test
    @DisplayName("Listar por termo com fallback em memoria")
    void listarPorTermoFallbackMemoria() {
        when(repositoryProvider.getIfAvailable()).thenReturn(repository);
        when(repository.save(any())).thenThrow(new RuntimeException("DB down"));

        UUID termoId = UUID.randomUUID();
        auditoriaService.registrar(UUID.randomUUID(), termoId, StatusProposta.EM_TESTE,
                StatusProposta.POC_CONCLUIDA, "u", "a", "d");

        when(repository.findByTermoPilotoIdOrderByDataHoraAsc(termoId)).thenThrow(new RuntimeException("DB down"));
        List<RegistroAuditoriaResponseDTO> lista = auditoriaService.listarPorTermo(termoId);
        assertEquals(1, lista.size());

        auditoriaService.limparMemoria();
        lista = auditoriaService.listarPorTermo(termoId);
        assertEquals(0, lista.size());
    }
}
