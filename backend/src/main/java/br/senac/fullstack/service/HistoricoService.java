package br.senac.fullstack.service;

import br.senac.fullstack.dto.HistoricoResponseDTO;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HistoricoService {

    private final SolicitacaoService solicitacaoService;
    private final ApoioBuscadoService apoioBuscadoService;
    private final RecomendacaoService recomendacaoService;
    private final AnotacaoService anotacaoService;
    private final StartupService startupService;
    private final MentorService mentorService;
    private final VinculoService vinculoService;

    public HistoricoService(SolicitacaoService solicitacaoService, ApoioBuscadoService apoioBuscadoService,
                            RecomendacaoService recomendacaoService, AnotacaoService anotacaoService,
                            StartupService startupService, MentorService mentorService,
                            VinculoService vinculoService) {
        this.solicitacaoService = solicitacaoService;
        this.apoioBuscadoService = apoioBuscadoService;
        this.recomendacaoService = recomendacaoService;
        this.anotacaoService = anotacaoService;
        this.startupService = startupService;
        this.mentorService = mentorService;
        this.vinculoService = vinculoService;
    }

    public void verificarAcesso(UUID startupId, String principal, boolean isAdmin) {
        if (isAdmin) return;
        try {
            var startup = startupService.buscar(startupId);
            if (startup.email() != null && startup.email().equals(principal)) {
                return;
            }
        } catch (Exception ignored) {
        }
        // mentor vinculado?
        try {
            var mentor = mentorService.buscarEntidadePorEmail(principal);
            if (vinculoService.temVinculoAtivo(startupId, mentor.getId())) {
                return;
            }
        } catch (Exception ignored) {
        }
        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.FORBIDDEN, "Sem permissao para acessar o historico");
    }

    public HistoricoResponseDTO buscar(UUID startupId) {
        // RNF07: visibilidade - por enquanto retorna tudo; restricao de vinculo a ser aplicada no controller
        return new HistoricoResponseDTO(
                solicitacaoService.listarPorStartup(startupId),
                apoioBuscadoService.listarPorStartup(startupId),
                recomendacaoService.listarPorStartup(startupId),
                anotacaoService.listarPorStartup(startupId));
    }
}
