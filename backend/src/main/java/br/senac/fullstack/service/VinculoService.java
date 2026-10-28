package br.senac.fullstack.service;

import br.senac.fullstack.dto.VinculoResponseDTO;
import br.senac.fullstack.model.StatusVinculo;
import br.senac.fullstack.model.VinculoMentoria;
import br.senac.fullstack.repository.VinculoMentoriaRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VinculoService {

    private final ObjectProvider<VinculoMentoriaRepository> repositoryProvider;
    private final MentorService mentorService;
    private final Map<UUID, VinculoMentoria> memory = new ConcurrentHashMap<>();

    public VinculoService(ObjectProvider<VinculoMentoriaRepository> repositoryProvider, MentorService mentorService) {
        this.repositoryProvider = repositoryProvider;
        this.mentorService = mentorService;
    }

    private VinculoMentoriaRepository repository() {
        return repositoryProvider.getIfAvailable();
    }

    public VinculoMentoria criar(UUID startupId, UUID mentorId) {
        VinculoMentoria v = new VinculoMentoria();
        v.setStartupId(startupId);
        v.setMentorId(mentorId);
        VinculoMentoriaRepository repo = repository();
        if (repo != null) {
            v = repo.save(v);
        } else {
            memory.put(v.getId(), v);
        }
        return v;
    }

    public boolean temVinculoAtivo(UUID startupId, UUID mentorId) {
        VinculoMentoriaRepository repo = repository();
        List<VinculoMentoria> items;
        if (repo != null) {
            items = repo.findByStartupId(startupId);
        } else {
            items = memory.values().stream()
                    .filter(v -> v.getStartupId().equals(startupId)).toList();
        }
        return items.stream().anyMatch(v ->
                v.getMentorId().equals(mentorId) && v.getStatus().equals(StatusVinculo.ATIVO));
    }

    public void encerrar(UUID id) {
        VinculoMentoriaRepository repo = repository();
        VinculoMentoria v;
        if (repo != null) {
            v = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vinculo nao encontrado"));
        } else {
            v = memory.get(id);
            if (v == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vinculo nao encontrado");
            }
        }
        v.setStatus(StatusVinculo.ENCERRADO);
        v.setDataFim(java.time.LocalDateTime.now(java.time.ZoneId.systemDefault()));
        if (repo != null) repo.save(v); else memory.put(v.getId(), v);
    }

    public VinculoResponseDTO buscarPorStartup(UUID startupId) {
        VinculoMentoriaRepository repo = repository();
        List<VinculoMentoria> items;
        if (repo != null) {
            items = repo.findByStartupId(startupId);
        } else {
            items = memory.values().stream()
                    .filter(v -> v.getStartupId().equals(startupId)).toList();
        }
        if (items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sem vinculo de mentoria");
        }
        VinculoMentoria v = items.get(0);
        // contato do mentor (email) - buscar via mentor service
        String contato = "";
        try {
            var mentor = mentorService.buscar(v.getMentorId());
            contato = mentor.email();
        } catch (Exception ignored) { /* empty */ }
        String regras = "Regras da plataforma: respeitar cronograma, manter comunicacao e registrar orientacoes.";
        return new VinculoResponseDTO(v.getId(), v.getStartupId(), v.getMentorId(),
                v.getDataInicio(), v.getDataFim(), v.getStatus(), regras, contato);
    }
}
