package br.senac.fullstack.service;

import br.senac.fullstack.dto.AvaliacaoMatchRequestDTO;
import br.senac.fullstack.dto.AvaliacaoMatchResponseDTO;
import br.senac.fullstack.model.AvaliacaoMatch;
import br.senac.fullstack.model.OrigemMatch;
import br.senac.fullstack.model.StatusProposta;
import br.senac.fullstack.model.TermoPiloto;
import br.senac.fullstack.model.TipoAvaliadorMatch;
import br.senac.fullstack.repository.AvaliacaoMatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvaliacaoMatchServiceTest {

    @Mock
    private AvaliacaoMatchRepository repository;

    @Mock
    private ObjectProvider<AvaliacaoMatchRepository> repositoryProvider;

    @Mock
    private TermoPilotoService termoService;

    @Mock
    private ObjectProvider<TermoPilotoService> termoServiceProvider;

    @Mock
    private PropostaPocService propostaService;

    @Mock
    private ObjectProvider<PropostaPocService> propostaServiceProvider;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private ObjectProvider<AuditoriaService> auditoriaServiceProvider;

    @Mock
    private Authentication authentication;

    private AvaliacaoMatchService avaliacaoMatchService;

    @BeforeEach
    void setUp() {
        avaliacaoMatchService = new AvaliacaoMatchService(
                repositoryProvider,
                termoServiceProvider,
                propostaServiceProvider,
                auditoriaServiceProvider,
                null,
                null
        );
    }

    @Test
    @DisplayName("Avaliar match com sucesso por Startup autenticada")
    void avaliarMatchPorStartup() {
        UUID termoId = UUID.randomUUID();
        UUID propostaId = UUID.randomUUID();
        TermoPiloto termo = new TermoPiloto();
        termo.setId(termoId);
        termo.setPropostaPocId(propostaId);
        termo.setStatus(StatusProposta.POC_CONCLUIDA);

        when(termoServiceProvider.getIfAvailable()).thenReturn(termoService);
        when(termoService.buscarPorIdOuPropostaId(termoId)).thenReturn(termo);

        when(authentication.getName()).thenReturn("startup@azreal.com");
        doReturnAuthorities(List.of("ROLE_STARTUP"));

        when(repositoryProvider.getIfAvailable()).thenReturn(repository);
        when(repository.findByPropostaPocIdAndTipoAvaliador(propostaId, TipoAvaliadorMatch.STARTUP))
                .thenReturn(Optional.empty());
        when(repository.save(any(AvaliacaoMatch.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(auditoriaServiceProvider.getIfAvailable()).thenReturn(auditoriaService);

        AvaliacaoMatchRequestDTO request = new AvaliacaoMatchRequestDTO(true, 5, "Encaixe perfeito", OrigemMatch.IA);
        AvaliacaoMatchResponseDTO response = avaliacaoMatchService.avaliar(termoId, request, authentication);

        assertNotNull(response);
        assertEquals(propostaId, response.propostaPocId());
        assertEquals(termoId, response.termoPilotoId());
        assertEquals(TipoAvaliadorMatch.STARTUP, response.tipoAvaliador());
        assertEquals("startup@azreal.com", response.usuarioResponsavel());
        assertTrue(response.encaixeFezSentido());
        assertEquals(5, response.notaQualidade());
        assertEquals("Encaixe perfeito", response.comentario());
        assertEquals(OrigemMatch.IA, response.origemMatch());

        verify(auditoriaService).registrar(
                eq(propostaId),
                eq(termoId),
                eq(StatusProposta.POC_CONCLUIDA),
                eq(StatusProposta.POC_CONCLUIDA),
                eq("startup@azreal.com"),
                eq("AVALIACAO_MATCH"),
                any()
        );
    }

    @Test
    @DisplayName("Avaliar match com sucesso por Ambiente autenticado e atualizar avaliacao existente")
    void avaliarMatchPorAmbienteAtualizandoExistente() {
        UUID propostaId = UUID.randomUUID();

        when(authentication.getName()).thenReturn("hospital@einstein.br");
        doReturnAuthorities(List.of("ROLE_AMBIENTE_VALIDACAO"));

        AvaliacaoMatch existente = new AvaliacaoMatch();
        existente.setId(UUID.randomUUID());
        existente.setPropostaPocId(propostaId);
        existente.setTipoAvaliador(TipoAvaliadorMatch.AMBIENTE_VALIDACAO);
        existente.setNotaQualidade(3);

        when(repositoryProvider.getIfAvailable()).thenReturn(repository);
        when(repository.findByPropostaPocIdAndTipoAvaliador(propostaId, TipoAvaliadorMatch.AMBIENTE_VALIDACAO))
                .thenReturn(Optional.of(existente));
        when(repository.save(any(AvaliacaoMatch.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AvaliacaoMatchRequestDTO request = new AvaliacaoMatchRequestDTO(true, 4, "Atualizado", OrigemMatch.REGRAS);
        AvaliacaoMatchResponseDTO response = avaliacaoMatchService.avaliar(propostaId, request, authentication);

        assertNotNull(response);
        assertEquals(4, response.notaQualidade());
        assertEquals(OrigemMatch.REGRAS, response.origemMatch());
        assertEquals(TipoAvaliadorMatch.AMBIENTE_VALIDACAO, response.tipoAvaliador());
    }

    @Test
    @DisplayName("Avaliar match validacoes de campos obrigatorios")
    void validarCamposObrigatorios() {
        UUID id = UUID.randomUUID();

        assertThrows(ResponseStatusException.class, () -> avaliacaoMatchService.avaliar(null, null, authentication));
        assertThrows(ResponseStatusException.class, () -> avaliacaoMatchService.avaliar(id, null, authentication));

        AvaliacaoMatchRequestDTO semEncaixe = new AvaliacaoMatchRequestDTO(null, 5, "Sem encaixe");
        assertThrows(ResponseStatusException.class, () -> avaliacaoMatchService.avaliar(id, semEncaixe, authentication));

        AvaliacaoMatchRequestDTO notaZero = new AvaliacaoMatchRequestDTO(true, 0, "Nota zero");
        assertThrows(ResponseStatusException.class, () -> avaliacaoMatchService.avaliar(id, notaZero, authentication));

        AvaliacaoMatchRequestDTO notaSeis = new AvaliacaoMatchRequestDTO(true, 6, "Nota seis");
        assertThrows(ResponseStatusException.class, () -> avaliacaoMatchService.avaliar(id, notaSeis, authentication));
    }

    @Test
    @DisplayName("Listar avaliacoes por proposta ou termo")
    void listarAvaliacoesPorPropostaOuTermo() {
        assertTrue(avaliacaoMatchService.listarPorPropostaOuTermo(null).isEmpty());

        UUID propostaId = UUID.randomUUID();
        AvaliacaoMatch match = new AvaliacaoMatch();
        match.setPropostaPocId(propostaId);
        match.setTipoAvaliador(TipoAvaliadorMatch.STARTUP);
        match.setNotaQualidade(5);
        match.setEncaixeFezSentido(true);

        when(repositoryProvider.getIfAvailable()).thenReturn(repository);
        when(repository.findByPropostaPocIdOrderByDataCriacaoAsc(propostaId)).thenReturn(List.of(match));

        List<AvaliacaoMatchResponseDTO> lista = avaliacaoMatchService.listarPorPropostaOuTermo(propostaId);
        assertEquals(1, lista.size());
        assertEquals(5, lista.get(0).notaQualidade());
    }

    @Test
    @DisplayName("Avaliar com ROLE_ADMIN sem papel explicito no request infere ADMIN")
    void avaliarComAdminSemPapelExplicito() {
        UUID id = UUID.randomUUID();
        when(authentication.getName()).thenReturn("admin@azreal.com");
        doReturnAuthorities(List.of("ROLE_ADMIN"));

        AvaliacaoMatchRequestDTO req = new AvaliacaoMatchRequestDTO(
                true, 5, "Perfeito", OrigemMatch.IA, null);

        when(repositoryProvider.getIfAvailable()).thenReturn(repository);
        when(repository.findByPropostaPocIdAndTipoAvaliador(id, TipoAvaliadorMatch.ADMIN))
                .thenReturn(Optional.empty());
        when(repository.save(any(AvaliacaoMatch.class))).thenAnswer(inv -> inv.getArgument(0));

        AvaliacaoMatchResponseDTO resp = avaliacaoMatchService.avaliar(id, req, authentication);
        assertNotNull(resp);
        assertEquals(TipoAvaliadorMatch.ADMIN, resp.tipoAvaliador());
    }

    @Test
    @DisplayName("Avaliar com papel explicito no request usa o papel especificado")
    void avaliarComPapelExplicito() {
        UUID id = UUID.randomUUID();
        when(authentication.getName()).thenReturn("admin@azreal.com");

        AvaliacaoMatchRequestDTO req = new AvaliacaoMatchRequestDTO(
                true, 5, "Perfeito", OrigemMatch.IA, TipoAvaliadorMatch.STARTUP);

        when(repositoryProvider.getIfAvailable()).thenReturn(repository);
        when(repository.findByPropostaPocIdAndTipoAvaliador(id, TipoAvaliadorMatch.STARTUP))
                .thenReturn(Optional.empty());
        when(repository.save(any(AvaliacaoMatch.class))).thenAnswer(inv -> inv.getArgument(0));

        AvaliacaoMatchResponseDTO resp = avaliacaoMatchService.avaliar(id, req, authentication);
        assertNotNull(resp);
        assertEquals(TipoAvaliadorMatch.STARTUP, resp.tipoAvaliador());
    }

    @Test
    @DisplayName("Avaliar sem autenticacao usa valores padrao")
    void avaliarSemAutenticacao() {
        UUID id = UUID.randomUUID();
        AvaliacaoMatchRequestDTO req = new AvaliacaoMatchRequestDTO(true, 4, "Sem auth");

        when(repositoryProvider.getIfAvailable()).thenReturn(repository);
        when(repository.findByPropostaPocIdAndTipoAvaliador(id, TipoAvaliadorMatch.STARTUP))
                .thenReturn(Optional.empty());
        when(repository.save(any(AvaliacaoMatch.class))).thenAnswer(inv -> inv.getArgument(0));

        AvaliacaoMatchResponseDTO resp = avaliacaoMatchService.avaliar(id, req, null);
        assertNotNull(resp);
        assertEquals("SISTEMA", resp.usuarioResponsavel());
        assertEquals(TipoAvaliadorMatch.STARTUP, resp.tipoAvaliador());
    }

    @Test
    @DisplayName("Avaliar com fallback em memoria quando repositorio lanca excecao")
    void avaliarComFallbackMemoria() {
        UUID id = UUID.randomUUID();
        AvaliacaoMatchRequestDTO req = new AvaliacaoMatchRequestDTO(true, 5, "Fallback", OrigemMatch.IA);

        when(repositoryProvider.getIfAvailable()).thenReturn(repository);
        when(repository.findByPropostaPocIdAndTipoAvaliador(any(), any()))
                .thenThrow(new RuntimeException("DB down"));
        when(repository.save(any(AvaliacaoMatch.class)))
                .thenThrow(new RuntimeException("DB down"));

        AvaliacaoMatchResponseDTO resp = avaliacaoMatchService.avaliar(id, req, null);
        assertNotNull(resp);
        assertEquals(5, resp.notaQualidade());

        // Consulta usando fallback memoria
        when(repository.findByPropostaPocIdOrderByDataCriacaoAsc(id))
                .thenThrow(new RuntimeException("DB down"));
        List<AvaliacaoMatchResponseDTO> lista = avaliacaoMatchService.listarPorPropostaOuTermo(id);
        assertEquals(1, lista.size());
    }

    @Test
    @DisplayName("Listar avaliacoes buscando por termo no repositorio")
    void listarAvaliacoesPorTermoNoRepositorio() {
        UUID termoId = UUID.randomUUID();
        AvaliacaoMatch match = new AvaliacaoMatch();
        match.setTermoPilotoId(termoId);
        match.setTipoAvaliador(TipoAvaliadorMatch.AMBIENTE_VALIDACAO);
        match.setNotaQualidade(4);
        match.setEncaixeFezSentido(true);

        when(repositoryProvider.getIfAvailable()).thenReturn(repository);
        when(repository.findByPropostaPocIdOrderByDataCriacaoAsc(termoId)).thenReturn(List.of());
        when(repository.findByTermoPilotoIdOrderByDataCriacaoAsc(termoId)).thenReturn(List.of(match));

        List<AvaliacaoMatchResponseDTO> lista = avaliacaoMatchService.listarPorPropostaOuTermo(termoId);
        assertEquals(1, lista.size());
        assertEquals(4, lista.get(0).notaQualidade());
    }

    @SuppressWarnings("unchecked")
    private void doReturnAuthorities(List<String> roles) {
        Collection authorities = roles.stream().map(SimpleGrantedAuthority::new).toList();
        when(authentication.getAuthorities()).thenReturn(authorities);
    }
}
