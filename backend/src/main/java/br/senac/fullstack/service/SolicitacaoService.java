package br.senac.fullstack.service;

import br.senac.fullstack.dto.FilaItemResponseDTO;
import br.senac.fullstack.dto.RespostaPedidoRequestDTO;
import br.senac.fullstack.dto.SolicitacaoRequestDTO;
import br.senac.fullstack.dto.SolicitacaoResponseDTO;
import br.senac.fullstack.model.SolicitacaoOrientacao;
import br.senac.fullstack.model.StatusSolicitacao;
import br.senac.fullstack.repository.SolicitacaoOrientacaoRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SolicitacaoService {

    private final ObjectProvider<SolicitacaoOrientacaoRepository> repositoryProvider;
    private final StartupService startupService;
    private final MentorService mentorService;
    private final VinculoService vinculoService;
    private final Map<UUID, SolicitacaoOrientacao> memory = new ConcurrentHashMap<>();

    public SolicitacaoService(ObjectProvider<SolicitacaoOrientacaoRepository> repositoryProvider,
                              StartupService startupService, MentorService mentorService,
                              VinculoService vinculoService) {
        this.repositoryProvider = repositoryProvider;
        this.startupService = startupService;
        this.mentorService = mentorService;
        this.vinculoService = vinculoService;
    }

    private SolicitacaoOrientacaoRepository repository() {
        return repositoryProvider.getIfAvailable();
    }

    public SolicitacaoResponseDTO criar(SolicitacaoRequestDTO request) {
        // valida startup e mentor
        startupService.buscar(request.startupId());
        mentorService.buscar(request.mentorId());

        // limite de 2 pedidos em aberto
        List<SolicitacaoOrientacao> abertos = listarAbertosPorStartup(request.startupId());
        if (abertos.size() >= 2) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Limite de 2 pedidos em aberto excedido");
        }

        SolicitacaoOrientacao sol = new SolicitacaoOrientacao();
        sol.setStartupId(request.startupId());
        sol.setMentorId(request.mentorId());
        sol.setAssunto(request.assunto());
        sol.setDataExpiracao(calcularExpiracao(sol.getDataSolicitacao()));

        SolicitacaoOrientacaoRepository repo = repository();
        if (repo != null) {
            sol = repo.save(sol);
        } else {
            memory.put(sol.getId(), sol);
        }
        return toResponse(sol);
    }

    public SolicitacaoResponseDTO responder(UUID id, UUID mentorId, RespostaPedidoRequestDTO request) {
        SolicitacaoOrientacao sol = buscar(id);
        if (!sol.getMentorId().equals(mentorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pedido nao pertence a este mentor");
        }
        if (!sol.getStatus().equals(StatusSolicitacao.PENDENTE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Pedido ja respondido ou expirado");
        }
        if (LocalDateTime.now(java.time.ZoneId.systemDefault()).isAfter(sol.getDataExpiracao())) {
            sol.setStatus(StatusSolicitacao.EXPIRADO);
            salvar(sol);
            throw new ResponseStatusException(HttpStatus.GONE, "Pedido expirado");
        }

        if (Boolean.TRUE.equals(request.aceitar())) {
            sol.setStatus(StatusSolicitacao.ACEITO);
            vinculoService.criar(sol.getStartupId(), sol.getMentorId());
        } else {
            sol.setStatus(StatusSolicitacao.RECUSADO);
        }
        salvar(sol);
        return toResponse(sol);
    }

    public List<FilaItemResponseDTO> filaDoMentor(UUID mentorId) {
        List<SolicitacaoOrientacao> pendentes = listarPendentesPorMentor(mentorId);
        List<FilaItemResponseDTO> result = new ArrayList<>();
        for (SolicitacaoOrientacao sol : pendentes) {
            String nome = "Startup " + sol.getStartupId();
            String descricao = "";
            List<String> integrantes = List.of();
            try {
                var startup = startupService.buscar(sol.getStartupId());
                nome = startup.nome();
                descricao = startup.descricao() != null ? startup.descricao() : "";
                integrantes = startup.integrantes() != null ? startup.integrantes() : List.of();
            } catch (Exception ignored) { /* empty */ }
            result.add(new FilaItemResponseDTO(sol.getId(), nome, descricao, sol.getAssunto(), integrantes));
        }
        return result;
    }

    // helpers
    private List<SolicitacaoOrientacao> listarAbertosPorStartup(UUID startupId) {
        SolicitacaoOrientacaoRepository repo = repository();
        if (repo != null) {
            return repo.findByStartupId(startupId).stream()
                    .filter(s -> s.getStatus().equals(StatusSolicitacao.PENDENTE)).toList();
        }
        return memory.values().stream()
                .filter(s -> s.getStartupId().equals(startupId) && s.getStatus().equals(StatusSolicitacao.PENDENTE))
                .toList();
    }

    private List<SolicitacaoOrientacao> listarPendentesPorMentor(UUID mentorId) {
        SolicitacaoOrientacaoRepository repo = repository();
        if (repo != null) {
            return repo.findByMentorIdAndStatus(mentorId, StatusSolicitacao.PENDENTE);
        }
        return memory.values().stream()
                .filter(s -> s.getMentorId().equals(mentorId) && s.getStatus().equals(StatusSolicitacao.PENDENTE))
                .toList();
    }

    private SolicitacaoOrientacao buscar(UUID id) {
        SolicitacaoOrientacaoRepository repo = repository();
        if (repo != null) {
            return repo.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitacao nao encontrada"));
        }
        SolicitacaoOrientacao sol = memory.get(id);
        if (sol == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitacao nao encontrada");
        }
        return sol;
    }

    private void salvar(SolicitacaoOrientacao sol) {
        SolicitacaoOrientacaoRepository repo = repository();
        if (repo != null) {
            repo.save(sol);
        } else {
            memory.put(sol.getId(), sol);
        }
    }

    private LocalDateTime calcularExpiracao(LocalDateTime inicio) {
        LocalDateTime data = inicio;
        int dias = 0;
        while (dias < 5) {
            data = data.plusDays(1);
            if (!data.getDayOfWeek().equals(DayOfWeek.SATURDAY) && !data.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
                dias++;
            }
        }
        return data;
    }

    public List<br.senac.fullstack.dto.SolicitacaoResponseDTO> listarPorStartup(UUID startupId) {
        SolicitacaoOrientacaoRepository repo = repository();
        List<SolicitacaoOrientacao> items;
        if (repo != null) {
            items = repo.findByStartupId(startupId);
        } else {
            items = memory.values().stream()
                    .filter(s -> s.getStartupId().equals(startupId)).collect(java.util.stream.Collectors.toList());
        }
        return items.stream().map(this::toResponse).collect(java.util.stream.Collectors.toList());
    }

    private SolicitacaoResponseDTO toResponse(SolicitacaoOrientacao s) {
        return new SolicitacaoResponseDTO(
                s.getId(), s.getStartupId(), s.getMentorId(), s.getAssunto(),
                s.getStatus(), s.getDataSolicitacao(), s.getDataExpiracao());
    }
}
