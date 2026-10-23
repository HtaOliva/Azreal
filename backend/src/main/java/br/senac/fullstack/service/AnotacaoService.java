package br.senac.fullstack.service;

import br.senac.fullstack.dto.AnotacaoRequestDTO;
import br.senac.fullstack.dto.AnotacaoResponseDTO;
import br.senac.fullstack.model.AnotacaoMentoria;
import br.senac.fullstack.repository.AnotacaoMentoriaRepository;
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
public class AnotacaoService {

    private final ObjectProvider<AnotacaoMentoriaRepository> repositoryProvider;
    private final VinculoService vinculoService;
    private final Map<UUID, AnotacaoMentoria> memory = new ConcurrentHashMap<>();

    public AnotacaoService(ObjectProvider<AnotacaoMentoriaRepository> repositoryProvider, VinculoService vinculoService) {
        this.repositoryProvider = repositoryProvider;
        this.vinculoService = vinculoService;
    }

    private AnotacaoMentoriaRepository repository() {
        return repositoryProvider.getIfAvailable();
    }

    public AnotacaoResponseDTO criar(AnotacaoRequestDTO request) {
        if (!vinculoService.temVinculoAtivo(request.startupId(), request.mentorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mentor nao vinculado a startup");
        }
        AnotacaoMentoria a = new AnotacaoMentoria();
        a.setMentorId(request.mentorId());
        a.setStartupId(request.startupId());
        a.setConteudo(request.conteudo());

        AnotacaoMentoriaRepository repo = repository();
        if (repo != null) {
            a = repo.save(a);
        } else {
            memory.put(a.getId(), a);
        }
        return new AnotacaoResponseDTO(a.getId(), a.getMentorId(), a.getStartupId(), a.getConteudo(), a.getDataHora());
    }

    public List<AnotacaoResponseDTO> listarPorStartup(UUID startupId) {
        AnotacaoMentoriaRepository repo = repository();
        List<AnotacaoMentoria> items;
        if (repo != null) {
            items = repo.findByStartupId(startupId);
        } else {
            items = memory.values().stream()
                    .filter(a -> a.getStartupId().equals(startupId)).collect(Collectors.toList());
        }
        return items.stream()
                .map(a -> new AnotacaoResponseDTO(a.getId(), a.getMentorId(), a.getStartupId(), a.getConteudo(), a.getDataHora()))
                .collect(Collectors.toList());
    }
}
