package br.senac.fullstack.service;

import br.senac.fullstack.dto.StartupRequestDTO;
import br.senac.fullstack.dto.StartupResponseDTO;
import br.senac.fullstack.dto.StartupUpdateDTO;
import br.senac.fullstack.model.Startup;
import br.senac.fullstack.repository.StartupRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StartupService {

    private final ObjectProvider<StartupRepository> repositoryProvider;
    private final AceiteTermoService aceiteTermoService;
    private final Map<UUID, Startup> memory = new ConcurrentHashMap<>();

    public StartupService(ObjectProvider<StartupRepository> repositoryProvider, AceiteTermoService aceiteTermoService) {
        this.repositoryProvider = repositoryProvider;
        this.aceiteTermoService = aceiteTermoService;
    }

    private StartupRepository repository() {
        return repositoryProvider.getIfAvailable();
    }

    public StartupResponseDTO criar(StartupRequestDTO request) {
        if (!aceiteTermoService.existe(request.aceiteTermoId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aceite de termos obrigatorio");
        }
        Startup startup = new Startup();
        startup.setNome(request.nome());
        startup.setEmail(request.email());
        startup.setDescricao(request.descricao());
        startup.setAreaAtuacao(request.areaAtuacao());
        startup.setDataFundacao(request.dataFundacao());
        startup.setEstagioAtual(request.estagioAtual());
        startup.setSituacao("ATIVA");

        StartupRepository repo = repository();
        if (repo != null) {
            startup = repo.save(startup);
        } else {
            memory.put(startup.getId(), startup);
        }
        return toResponse(startup);
    }

    public StartupResponseDTO buscar(UUID id) {
        StartupRepository repo = repository();
        Startup startup;
        if (repo != null) {
            startup = repo.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Startup nao encontrada"));
        } else {
            startup = memory.get(id);
            if (startup == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Startup nao encontrada");
            }
        }
        return toResponse(startup);
    }

    public List<StartupResponseDTO> listar() {
        StartupRepository repo = repository();
        List<Startup> items;
        if (repo != null) {
            items = repo.findAll();
        } else {
            items = new ArrayList<>(memory.values());
        }
        List<StartupResponseDTO> result = new ArrayList<>();
        for (Startup s : items) {
            result.add(toResponse(s));
        }
        return result;
    }

    public StartupResponseDTO atualizar(UUID id, br.senac.fullstack.dto.StartupUpdateDTO request) {
        StartupRepository repo = repository();
        Startup startup;
        if (repo != null) {
            startup = repo.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Startup nao encontrada"));
        } else {
            startup = memory.get(id);
            if (startup == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Startup nao encontrada");
            }
        }
        if (request.nome() != null) startup.setNome(request.nome());
        if (request.descricao() != null) startup.setDescricao(request.descricao());
        if (request.areaAtuacao() != null) startup.setAreaAtuacao(request.areaAtuacao());
        if (request.estagioAtual() != null) startup.setEstagioAtual(request.estagioAtual());
        if (request.situacao() != null) startup.setSituacao(request.situacao());

        if (repo != null) {
            startup = repo.save(startup);
        }
        return toResponse(startup);
    }

    private StartupResponseDTO toResponse(Startup s) {
        return new StartupResponseDTO(
                s.getId(), s.getNome(), s.getEmail(), s.getDescricao(), s.getAreaAtuacao(),
                s.getDataFundacao(), s.getEstagioAtual(), s.getSituacao(), s.getDataCadastro());
    }
}
