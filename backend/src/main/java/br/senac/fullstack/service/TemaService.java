package br.senac.fullstack.service;

import br.senac.fullstack.dto.TemaRequestDTO;
import br.senac.fullstack.dto.TemaResponseDTO;
import br.senac.fullstack.model.Tema;
import br.senac.fullstack.repository.TemaRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class TemaService {

    private final ObjectProvider<TemaRepository> repositoryProvider;
    private final Map<UUID, Tema> memory = new ConcurrentHashMap<>();

    public TemaService(ObjectProvider<TemaRepository> repositoryProvider) {
        this.repositoryProvider = repositoryProvider;
    }

    private TemaRepository repository() {
        return repositoryProvider.getIfAvailable();
    }

    public TemaResponseDTO criar(TemaRequestDTO request) {
        Tema t = new Tema();
        t.setNome(request.nome());
        t.setDescricao(request.descricao());
        if (request.ativo() != null) t.setAtivo(request.ativo());

        TemaRepository repo = repository();
        if (repo != null) {
            t = repo.save(t);
        } else {
            memory.put(t.getId(), t);
        }
        return toResponse(t);
    }

    public TemaResponseDTO atualizar(UUID id, TemaRequestDTO request) {
        TemaRepository repo = repository();
        Tema t;
        if (repo != null) {
            t = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Tema nao encontrado"));
        } else {
            t = memory.get(id);
            if (t == null) throw new IllegalArgumentException("Tema nao encontrado");
        }
        if (request.nome() != null) t.setNome(request.nome());
        if (request.descricao() != null) t.setDescricao(request.descricao());
        if (request.ativo() != null) t.setAtivo(request.ativo());
        if (repo != null) t = repo.save(t); else memory.put(t.getId(), t);
        return toResponse(t);
    }

    public void definirAtivo(UUID id, boolean ativo) {
        TemaRepository repo = repository();
        Tema t;
        if (repo != null) {
            t = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Tema nao encontrado"));
        } else {
            t = memory.get(id);
            if (t == null) throw new IllegalArgumentException("Tema nao encontrado");
        }
        t.setAtivo(ativo);
        if (repo != null) repo.save(t); else memory.put(t.getId(), t);
    }

    public List<TemaResponseDTO> listarAtivos() {
        TemaRepository repo = repository();
        List<Tema> items;
        if (repo != null) {
            items = repo.findByAtivoTrue();
        } else {
            items = memory.values().stream().filter(Tema::isAtivo).collect(Collectors.toList());
        }
        return items.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<TemaResponseDTO> listarTodos() {
        TemaRepository repo = repository();
        List<Tema> items;
        if (repo != null) {
            items = repo.findAll();
        } else {
            items = new java.util.ArrayList<>(memory.values());
        }
        return items.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private TemaResponseDTO toResponse(Tema t) {
        return new TemaResponseDTO(t.getId(), t.getNome(), t.getDescricao(), t.isAtivo(), t.getDataCadastro());
    }
}