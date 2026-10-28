package br.senac.fullstack.service;

import br.senac.fullstack.dto.CursoRequestDTO;
import br.senac.fullstack.dto.CursoResponseDTO;
import br.senac.fullstack.model.Curso;
import br.senac.fullstack.repository.CursoRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CursoService {

    private final ObjectProvider<CursoRepository> repositoryProvider;
    private final Map<UUID, Curso> memory = new ConcurrentHashMap<>();

    public CursoService(ObjectProvider<CursoRepository> repositoryProvider) {
        this.repositoryProvider = repositoryProvider;
    }

    private CursoRepository repository() {
        return repositoryProvider.getIfAvailable();
    }

    public CursoResponseDTO criar(CursoRequestDTO request) {
        Curso c = new Curso();
        c.setTitulo(request.titulo());
        c.setDescricao(request.descricao());
        c.setUrl(request.url());
        if (request.ativo() != null) c.setAtivo(request.ativo());

        CursoRepository repo = repository();
        if (repo != null) {
            c = repo.save(c);
        } else {
            memory.put(c.getId(), c);
        }
        return toResponse(c);
    }

    public CursoResponseDTO atualizar(UUID id, CursoRequestDTO request) {
        CursoRepository repo = repository();
        Curso c;
        if (repo != null) {
            c = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Curso nao encontrado"));
        } else {
            c = memory.get(id);
            if (c == null) throw new IllegalArgumentException("Curso nao encontrado");
        }
        if (request.titulo() != null) c.setTitulo(request.titulo());
        if (request.descricao() != null) c.setDescricao(request.descricao());
        if (request.url() != null) c.setUrl(request.url());
        if (request.ativo() != null) c.setAtivo(request.ativo());
        if (repo != null) c = repo.save(c); else memory.put(c.getId(), c);
        return toResponse(c);
    }

    public void definirAtivo(UUID id, boolean ativo) {
        CursoRepository repo = repository();
        Curso c;
        if (repo != null) {
            c = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Curso nao encontrado"));
        } else {
            c = memory.get(id);
            if (c == null) throw new IllegalArgumentException("Curso nao encontrado");
        }
        c.setAtivo(ativo);
        if (repo != null) repo.save(c); else memory.put(c.getId(), c);
    }

    public List<CursoResponseDTO> listarAtivos() {
        CursoRepository repo = repository();
        List<Curso> items;
        if (repo != null) {
            items = repo.findByAtivoTrue();
        } else {
            items = memory.values().stream().filter(Curso::isAtivo).toList();
        }
        return items.stream().map(this::toResponse).toList();
    }

    public List<CursoResponseDTO> listarTodos() {
        CursoRepository repo = repository();
        List<Curso> items;
        if (repo != null) {
            items = repo.findAll();
        } else {
            items = new java.util.ArrayList<>(memory.values());
        }
        return items.stream().map(this::toResponse).toList();
    }

    private CursoResponseDTO toResponse(Curso c) {
        return new CursoResponseDTO(c.getId(), c.getTitulo(), c.getDescricao(), c.getUrl(), c.isAtivo(), c.getDataCadastro());
    }
}
