package br.senac.fullstack.service;

import br.senac.fullstack.dto.IncubadoraRequestDTO;
import br.senac.fullstack.dto.IncubadoraResponseDTO;
import br.senac.fullstack.model.Incubadora;
import br.senac.fullstack.repository.IncubadoraRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IncubadoraService {

    private final ObjectProvider<IncubadoraRepository> repositoryProvider;
    private final Map<UUID, Incubadora> memory = new ConcurrentHashMap<>();

    public IncubadoraService(ObjectProvider<IncubadoraRepository> repositoryProvider) {
        this.repositoryProvider = repositoryProvider;
    }

    private IncubadoraRepository repository() {
        return repositoryProvider.getIfAvailable();
    }

    public IncubadoraResponseDTO criar(IncubadoraRequestDTO request) {
        Incubadora i = new Incubadora();
        i.setNome(request.nome());
        i.setFoco(request.foco());
        i.setCriteriosEntrada(request.criteriosEntrada());
        i.setLocalidade(request.localidade());
        if (request.abrangencia() != null) {
            i.setAbrangencia(request.abrangencia());
        }
        i.setModalidade(request.modalidade());
        i.setDescricao(request.descricao());
        i.setLink(request.link());
        if (request.ativo() != null) {
            i.setAtivo(request.ativo());
        }

        IncubadoraRepository repo = repository();
        if (repo != null) {
            i = repo.save(i);
        } else {
            memory.put(i.getId(), i);
        }
        return toResponse(i);
    }

    public IncubadoraResponseDTO atualizar(UUID id, IncubadoraRequestDTO request) {
        IncubadoraRepository repo = repository();
        Incubadora i;
        if (repo != null) {
            i = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Incubadora nao encontrada"));
        } else {
            i = memory.get(id);
            if (i == null) {
                throw new IllegalArgumentException("Incubadora nao encontrada");
            }
        }
        if (request.nome() != null) {
            i.setNome(request.nome());
        }
        if (request.foco() != null) {
            i.setFoco(request.foco());
        }
        if (request.criteriosEntrada() != null) {
            i.setCriteriosEntrada(request.criteriosEntrada());
        }
        if (request.localidade() != null) {
            i.setLocalidade(request.localidade());
        }
        if (request.abrangencia() != null) {
            i.setAbrangencia(request.abrangencia());
        }
        if (request.modalidade() != null) {
            i.setModalidade(request.modalidade());
        }
        if (request.descricao() != null) {
            i.setDescricao(request.descricao());
        }
        if (request.link() != null) {
            i.setLink(request.link());
        }
        if (request.ativo() != null) {
            i.setAtivo(request.ativo());
        }
        if (repo != null) {
            i = repo.save(i);
        } else {
            memory.put(i.getId(), i);
        }
        return toResponse(i);
    }

    public void definirAtivo(UUID id, boolean ativo) {
        IncubadoraRepository repo = repository();
        Incubadora i;
        if (repo != null) {
            i = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Incubadora nao encontrada"));
        } else {
            i = memory.get(id);
            if (i == null) {
                throw new IllegalArgumentException("Incubadora nao encontrada");
            }
        }
        i.setAtivo(ativo);
        if (repo != null) {
            repo.save(i);
        } else {
            memory.put(i.getId(), i);
        }
    }

    public List<IncubadoraResponseDTO> listarAtivas() {
        IncubadoraRepository repo = repository();
        List<Incubadora> items;
        if (repo != null) {
            items = repo.findByAtivoTrue();
        } else {
            items = memory.values().stream().filter(Incubadora::isAtivo).toList();
        }
        return items.stream().map(this::toResponse).toList();
    }

    public List<IncubadoraResponseDTO> listarTodos() {
        IncubadoraRepository repo = repository();
        List<Incubadora> items;
        if (repo != null) {
            items = repo.findAll();
        } else {
            items = new java.util.ArrayList<>(memory.values());
        }
        return items.stream().map(this::toResponse).toList();
    }

    private IncubadoraResponseDTO toResponse(Incubadora i) {
        return new IncubadoraResponseDTO(i.getId(), i.getNome(), i.getFoco(), i.getCriteriosEntrada(),
                i.getLocalidade(), i.getAbrangencia(), i.getModalidade(), i.getDescricao(), i.getLink(),
                i.isAtivo(), i.getDataCadastro());
    }
}