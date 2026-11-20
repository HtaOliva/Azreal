package br.senac.fullstack.service;

import br.senac.fullstack.dto.AvaliacaoMatchRequestDTO;
import br.senac.fullstack.dto.AvaliacaoMatchResponseDTO;
import br.senac.fullstack.model.AvaliacaoMatch;
import br.senac.fullstack.model.OrigemMatch;
import br.senac.fullstack.model.StatusProposta;
import br.senac.fullstack.model.TermoPiloto;
import br.senac.fullstack.model.TipoAvaliadorMatch;
import br.senac.fullstack.repository.AvaliacaoMatchRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servico para avaliacao pos-conclusao da qualidade do matchmaking (RF21).
 */
@Service
public class AvaliacaoMatchService {

    private final ObjectProvider<AvaliacaoMatchRepository> repositoryProvider;
    private final ObjectProvider<TermoPilotoService> termoServiceProvider;
    private final ObjectProvider<AuditoriaService> auditoriaServiceProvider;

    private final Map<UUID, AvaliacaoMatch> memory = new ConcurrentHashMap<>();

    @Autowired
    public AvaliacaoMatchService(
            ObjectProvider<AvaliacaoMatchRepository> repositoryProvider,
            ObjectProvider<TermoPilotoService> termoServiceProvider,
            ObjectProvider<AuditoriaService> auditoriaServiceProvider) {
        this.repositoryProvider = repositoryProvider;
        this.termoServiceProvider = termoServiceProvider;
        this.auditoriaServiceProvider = auditoriaServiceProvider;
    }

    public AvaliacaoMatchService(ObjectProvider<AvaliacaoMatchRepository> repositoryProvider) {
        this(repositoryProvider, null, null);
    }

    public AvaliacaoMatchService() {
        this(null, null, null);
    }

    public AvaliacaoMatchResponseDTO avaliar(
            UUID id,
            AvaliacaoMatchRequestDTO request,
            Authentication authentication) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID e obrigatorio");
        }
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados da avaliacao sao obrigatorios");
        }
        if (request.encaixeFezSentido() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A indicacao se o encaixe fez sentido e obrigatoria");
        }
        if (request.notaQualidade() == null || request.notaQualidade() < 1 || request.notaQualidade() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A nota de qualidade deve ser entre 1 e 5");
        }

        TermoPiloto termo = resolverTermo(id);
        UUID propostaId = termo != null ? termo.getPropostaPocId() : id;
        UUID termoId = termo != null ? termo.getId() : null;

        TipoAvaliadorMatch papelAvaliador = determinarPapelAvaliador(request, authentication);
        String usuario = authentication != null ? authentication.getName() : "SISTEMA";

        // Verifica se ja existe avaliacao deste tipo para a proposta
        Optional<AvaliacaoMatch> existente = buscarExistente(propostaId, papelAvaliador);
        AvaliacaoMatch avaliacao = existente.orElseGet(AvaliacaoMatch::new);

        avaliacao.setPropostaPocId(propostaId);
        avaliacao.setTermoPilotoId(termoId);
        avaliacao.setTipoAvaliador(papelAvaliador);
        avaliacao.setUsuarioResponsavel(usuario);
        avaliacao.setEncaixeFezSentido(request.encaixeFezSentido());
        avaliacao.setNotaQualidade(request.notaQualidade());
        avaliacao.setComentario(request.comentario() != null ? request.comentario().trim() : null);
        avaliacao.setOrigemMatch(request.origemMatch() != null ? request.origemMatch() : OrigemMatch.IA);

        AvaliacaoMatch salva = salvar(avaliacao);

        // Registro de Auditoria (RNF08)
        AuditoriaService auditoriaService = auditoriaServiceProvider != null
                ? auditoriaServiceProvider.getIfAvailable() : null;
        if (auditoriaService != null) {
            StatusProposta statusAtual = termo != null ? termo.getStatus() : null;
            auditoriaService.registrar(
                    propostaId,
                    termoId,
                    statusAtual,
                    statusAtual,
                    usuario,
                    "AVALIACAO_MATCH",
                    String.format("Avaliacao do match registrada por %s: nota %d, fez sentido: %s, origem: %s",
                            papelAvaliador, salva.getNotaQualidade(), salva.getEncaixeFezSentido(), salva.getOrigemMatch())
            );
        }

        return AvaliacaoMatchResponseDTO.fromEntity(salva);
    }

    private TermoPiloto resolverTermo(UUID id) {
        TermoPilotoService termoService = termoServiceProvider != null
                ? termoServiceProvider.getIfAvailable() : null;
        if (termoService != null) {
            try {
                return termoService.buscarPorIdOuPropostaId(id);
            } catch (Exception ignored) {
                // segue nulo se nao encontrar
            }
        }
        return null;
    }

    private TipoAvaliadorMatch determinarPapelAvaliador(
            AvaliacaoMatchRequestDTO request,
            Authentication authentication) {
        if (request.tipoAvaliador() != null) {
            return request.tipoAvaliador();
        }

        if (authentication != null) {
            boolean isStartup = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_STARTUP"));
            if (isStartup) {
                return TipoAvaliadorMatch.STARTUP;
            }

            boolean isAmbiente = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_AMBIENTE_VALIDACAO"));
            if (isAmbiente) {
                return TipoAvaliadorMatch.AMBIENTE_VALIDACAO;
            }

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (isAdmin) {
                return TipoAvaliadorMatch.ADMIN;
            }
        }

        return TipoAvaliadorMatch.STARTUP;
    }

    private Optional<AvaliacaoMatch> buscarExistente(UUID propostaId, TipoAvaliadorMatch tipo) {
        AvaliacaoMatchRepository repo = repositoryProvider != null ? repositoryProvider.getIfAvailable() : null;
        if (repo != null) {
            try {
                Optional<AvaliacaoMatch> doBanco = repo.findByPropostaPocIdAndTipoAvaliador(propostaId, tipo);
                if (doBanco.isPresent()) {
                    return doBanco;
                }
            } catch (Exception ignored) {
                // fallback to memory
            }
        }

        return memory.values().stream()
                .filter(a -> propostaId.equals(a.getPropostaPocId()) && tipo == a.getTipoAvaliador())
                .findFirst();
    }

    private AvaliacaoMatch salvar(AvaliacaoMatch avaliacao) {
        AvaliacaoMatchRepository repo = repositoryProvider != null ? repositoryProvider.getIfAvailable() : null;
        if (repo != null) {
            try {
                AvaliacaoMatch salva = repo.save(avaliacao);
                memory.put(salva.getId(), salva);
                return salva;
            } catch (Exception ignored) {
                // fallback to memory
            }
        }

        memory.put(avaliacao.getId(), avaliacao);
        return avaliacao;
    }

    public List<AvaliacaoMatchResponseDTO> listarPorPropostaOuTermo(UUID id) {
        if (id == null) {
            return List.of();
        }

        TermoPiloto termo = resolverTermo(id);
        UUID propostaId = termo != null ? termo.getPropostaPocId() : id;
        UUID termoId = termo != null ? termo.getId() : id;

        AvaliacaoMatchRepository repo = repositoryProvider != null ? repositoryProvider.getIfAvailable() : null;
        if (repo != null) {
            try {
                List<AvaliacaoMatch> lista = repo.findByPropostaPocIdOrderByDataCriacaoAsc(propostaId);
                if (!lista.isEmpty()) {
                    return lista.stream().map(AvaliacaoMatchResponseDTO::fromEntity).toList();
                }
                List<AvaliacaoMatch> porTermo = repo.findByTermoPilotoIdOrderByDataCriacaoAsc(termoId);
                if (!porTermo.isEmpty()) {
                    return porTermo.stream().map(AvaliacaoMatchResponseDTO::fromEntity).toList();
                }
            } catch (Exception ignored) {
                // fallback to memory
            }
        }

        return memory.values().stream()
                .filter(a -> propostaId.equals(a.getPropostaPocId()) || termoId.equals(a.getTermoPilotoId()))
                .sorted(Comparator.comparing(AvaliacaoMatch::getDataCriacao))
                .map(AvaliacaoMatchResponseDTO::fromEntity)
                .toList();
    }
}
