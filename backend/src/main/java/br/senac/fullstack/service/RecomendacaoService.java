package br.senac.fullstack.service;

import br.senac.fullstack.dto.RecomendacaoRequestDTO;
import br.senac.fullstack.dto.RecomendacaoResponseDTO;
import br.senac.fullstack.model.Recomendacao;
import br.senac.fullstack.repository.RecomendacaoRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class RecomendacaoService {

    private final ObjectProvider<RecomendacaoRepository> repositoryProvider;
    private final VinculoService vinculoService;
    private final Map<UUID, Recomendacao> memory = new ConcurrentHashMap<>();

    public RecomendacaoService(ObjectProvider<RecomendacaoRepository> repositoryProvider, VinculoService vinculoService) {
        this.repositoryProvider = repositoryProvider;
        this.vinculoService = vinculoService;
    }

    private RecomendacaoRepository repository() {
        return repositoryProvider.getIfAvailable();
    }

    public RecomendacaoResponseDTO criar(RecomendacaoRequestDTO request) {
        if (!vinculoService.temVinculoAtivo(request.startupId(), request.mentorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mentor nao vinculado a startup");
        }
        Recomendacao r = new Recomendacao();
        r.setMentorId(request.mentorId());
        r.setStartupId(request.startupId());
        r.setTipoApoio(request.tipoApoio());
        r.setObservacao(request.observacao());

        RecomendacaoRepository repo = repository();
        if (repo != null) {
            r = repo.save(r);
        } else {
            memory.put(r.getId(), r);
        }
        return toResponse(r);
    }

    public List<RecomendacaoResponseDTO> listarPorStartup(UUID startupId) {
        RecomendacaoRepository repo = repository();
        List<Recomendacao> items;
        if (repo != null) {
            items = repo.findByStartupId(startupId);
        } else {
            items = memory.values().stream()
                    .filter(r -> r.getStartupId().equals(startupId)).collect(Collectors.toList());
        }
        return items.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private RecomendacaoResponseDTO toResponse(Recomendacao r) {
        return new RecomendacaoResponseDTO(r.getId(), r.getMentorId(), r.getStartupId(),
                r.getTipoApoio(), r.getObservacao(), r.getDataHora());
    }
}
