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

    public HistoricoService(SolicitacaoService solicitacaoService, ApoioBuscadoService apoioBuscadoService,
                            RecomendacaoService recomendacaoService, AnotacaoService anotacaoService) {
        this.solicitacaoService = solicitacaoService;
        this.apoioBuscadoService = apoioBuscadoService;
        this.recomendacaoService = recomendacaoService;
        this.anotacaoService = anotacaoService;
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
