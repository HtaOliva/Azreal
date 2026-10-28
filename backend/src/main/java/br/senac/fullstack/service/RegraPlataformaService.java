package br.senac.fullstack.service;

import br.senac.fullstack.dto.RegraPlataformaRequestDTO;
import br.senac.fullstack.dto.RegraPlataformaResponseDTO;
import br.senac.fullstack.model.RegraPlataforma;
import br.senac.fullstack.repository.RegraPlataformaRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class RegraPlataformaService {

    private final ObjectProvider<RegraPlataformaRepository> repositoryProvider;
    private final Map<UUID, RegraPlataforma> memory = new ConcurrentHashMap<>();

    public RegraPlataformaService(ObjectProvider<RegraPlataformaRepository> repositoryProvider) {
        this.repositoryProvider = repositoryProvider;
    }

    private RegraPlataformaRepository repository() {
        return repositoryProvider.getIfAvailable();
    }

    public RegraPlataformaResponseDTO criar(RegraPlataformaRequestDTO request) {
        RegraPlataforma r = new RegraPlataforma();
        r.setTexto(request.texto());
        if (request.ativo() != null) r.setAtivo(request.ativo());
        r.setDataAtualizacao(java.time.LocalDateTime.now(java.time.ZoneId.systemDefault()));

        RegraPlataformaRepository repo = repository();
        if (repo != null) {
            r = repo.save(r);
        } else {
            memory.put(r.getId(), r);
        }
        return toResponse(r);
    }

    public RegraPlataformaResponseDTO atualizar(UUID id, RegraPlataformaRequestDTO request) {
        RegraPlataformaRepository repo = repository();
        RegraPlataforma r;
        if (repo != null) {
            r = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Regra nao encontrada"));
        } else {
            r = memory.get(id);
            if (r == null) throw new IllegalArgumentException("Regra nao encontrada");
        }
        if (request.texto() != null) r.setTexto(request.texto());
        if (request.ativo() != null) r.setAtivo(request.ativo());
        r.setDataAtualizacao(java.time.LocalDateTime.now(java.time.ZoneId.systemDefault()));
        if (repo != null) r = repo.save(r); else memory.put(r.getId(), r);
        return toResponse(r);
    }

    public void definirAtivo(UUID id, boolean ativo) {
        RegraPlataformaRepository repo = repository();
        RegraPlataforma r;
        if (repo != null) {
            r = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Regra nao encontrada"));
        } else {
            r = memory.get(id);
            if (r == null) throw new IllegalArgumentException("Regra nao encontrada");
        }
        r.setAtivo(ativo);
        r.setDataAtualizacao(java.time.LocalDateTime.now(java.time.ZoneId.systemDefault()));
        if (repo != null) repo.save(r); else memory.put(r.getId(), r);
    }

    public List<RegraPlataformaResponseDTO> listar() {
        RegraPlataformaRepository repo = repository();
        List<RegraPlataforma> items;
        if (repo != null) {
            items = repo.findAll();
        } else {
            items = new java.util.ArrayList<>(memory.values());
        }
        return items.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public RegraPlataformaResponseDTO buscarAtiva() {
        RegraPlataformaRepository repo = repository();
        List<RegraPlataforma> items;
        if (repo != null) {
            items = repo.findByAtivoTrue();
        } else {
            items = memory.values().stream().filter(RegraPlataforma::isAtivo).collect(Collectors.toList());
        }
        if (items.isEmpty()) throw new IllegalArgumentException("Regra nao encontrada");
        return toResponse(items.get(0));
    }

    private RegraPlataformaResponseDTO toResponse(RegraPlataforma r) {
        return new RegraPlataformaResponseDTO(r.getId(), r.getTexto(), r.isAtivo(), r.getDataAtualizacao());
    }
}