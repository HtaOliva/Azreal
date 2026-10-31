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
        if (request.integrantes() != null) {
            startup.setIntegrantes(request.integrantes());
        }
        if (request.localidadeSede() != null) {
            startup.setLocalidadeSede(request.localidadeSede());
        }
        if (request.localidadeAtuacao() != null) {
            startup.setLocalidadeAtuacao(request.localidadeAtuacao());
        }
        if (request.ligadaIncubadora() != null) {
            startup.setLigadaIncubadora(request.ligadaIncubadora());
        }
        if (request.incubadoraLigada() != null) {
            startup.setIncubadoraLigada(request.incubadoraLigada());
        }
        if (request.ufIbge() != null) {
            startup.setUfIbge(request.ufIbge());
        }
        if (request.municipioIbge() != null) {
            startup.setMunicipioIbge(request.municipioIbge());
        }
        if (request.oQuePrecisaValidar() != null) {
            startup.setOQuePrecisaValidar(request.oQuePrecisaValidar());
        }
        if (request.temasIds() != null) {
            startup.setTemasIds(request.temasIds());
        }
        if (request.modalidadesSuportadas() != null) {
            startup.setModalidadesSuportadas(request.modalidadesSuportadas());
        }

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
        return items.stream().map(this::toResponse).toList();
    }

    public StartupResponseDTO atualizar(UUID id, StartupUpdateDTO request) {
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
        aplicarAtualizacoesBasicas(startup, request);
        aplicarAtualizacoesPocMatch(startup, request);

        if (repo != null) {
            startup = repo.save(startup);
        }
        return toResponse(startup);
    }

    private void aplicarAtualizacoesBasicas(Startup startup, StartupUpdateDTO request) {
        if (request.nome() != null) {
            startup.setNome(request.nome());
        }
        if (request.descricao() != null) {
            startup.setDescricao(request.descricao());
        }
        if (request.areaAtuacao() != null) {
            startup.setAreaAtuacao(request.areaAtuacao());
        }
        if (request.estagioAtual() != null) {
            startup.setEstagioAtual(request.estagioAtual());
        }
        if (request.integrantes() != null) {
            startup.setIntegrantes(request.integrantes());
        }
        if (request.localidadeSede() != null) {
            startup.setLocalidadeSede(request.localidadeSede());
        }
        if (request.localidadeAtuacao() != null) {
            startup.setLocalidadeAtuacao(request.localidadeAtuacao());
        }
        if (request.ligadaIncubadora() != null) {
            startup.setLigadaIncubadora(request.ligadaIncubadora());
        }
        if (request.incubadoraLigada() != null) {
            startup.setIncubadoraLigada(request.incubadoraLigada());
        }
    }

    private void aplicarAtualizacoesPocMatch(Startup startup, StartupUpdateDTO request) {
        if (request.ufIbge() != null) {
            startup.setUfIbge(request.ufIbge());
        }
        if (request.municipioIbge() != null) {
            startup.setMunicipioIbge(request.municipioIbge());
        }
        if (request.oQuePrecisaValidar() != null) {
            startup.setOQuePrecisaValidar(request.oQuePrecisaValidar());
        }
        if (request.temasIds() != null) {
            startup.setTemasIds(request.temasIds());
        }
        if (request.modalidadesSuportadas() != null) {
            startup.setModalidadesSuportadas(request.modalidadesSuportadas());
        }
    }

    private StartupResponseDTO toResponse(Startup s) {
        return new StartupResponseDTO(
                s.getId(), s.getNome(), s.getEmail(), s.getDescricao(), s.getAreaAtuacao(),
                s.getDataFundacao(), s.getEstagioAtual(), s.getIntegrantes(),
                s.getLocalidadeSede(), s.getLocalidadeAtuacao(), s.isLigadaIncubadora(),
                s.getIncubadoraLigada(), s.getDataCadastro(), s.getUfIbge(), s.getMunicipioIbge(),
                s.getOQuePrecisaValidar(), s.getTemasIds(), s.getModalidadesSuportadas());
    }
}
