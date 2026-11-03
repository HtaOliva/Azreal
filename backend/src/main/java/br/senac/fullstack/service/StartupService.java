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

    private static final String STARTUP_NAO_ENCONTRADA = "Startup nao encontrada";

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
        startup.setLocalidadeSede(request.localidadeSede());
        startup.setLocalidadeAtuacao(request.localidadeAtuacao());
        startup.setIncubadoraLigada(request.incubadoraLigada());
        startup.setUfIbge(request.ufIbge());
        startup.setMunicipioIbge(request.municipioIbge());
        startup.setOQuePrecisaValidar(request.oQuePrecisaValidar());
        setIfPresent(request.integrantes(), startup::setIntegrantes);
        setIfPresent(request.ligadaIncubadora(), startup::setLigadaIncubadora);
        setIfPresent(request.temasIds(), startup::setTemasIds);
        setIfPresent(request.modalidadesSuportadas(), startup::setModalidadesSuportadas);

        StartupRepository repo = repository();
        if (repo != null) {
            startup = repo.save(startup);
        } else {
            memory.put(startup.getId(), startup);
        }
        return toResponse(startup);
    }

    private Startup obterPorId(UUID id) {
        StartupRepository repo = repository();
        if (repo != null) {
            return repo.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, STARTUP_NAO_ENCONTRADA));
        }
        Startup startup = memory.get(id);
        if (startup == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, STARTUP_NAO_ENCONTRADA);
        }
        return startup;
    }

    public StartupResponseDTO buscar(UUID id) {
        return toResponse(obterPorId(id));
    }

    public StartupResponseDTO buscarPorEmail(String email) {
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, STARTUP_NAO_ENCONTRADA);
        }
        StartupRepository repo = repository();
        if (repo != null) {
            return repo.findByEmail(email)
                    .map(this::toResponse)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, STARTUP_NAO_ENCONTRADA));
        }
        return memory.values().stream()
                .filter(s -> email.equalsIgnoreCase(s.getEmail()))
                .findFirst()
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, STARTUP_NAO_ENCONTRADA));
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
        Startup startup = obterPorId(id);

        setIfPresent(request.nome(), startup::setNome);
        setIfPresent(request.descricao(), startup::setDescricao);
        setIfPresent(request.areaAtuacao(), startup::setAreaAtuacao);
        setIfPresent(request.estagioAtual(), startup::setEstagioAtual);
        setIfPresent(request.integrantes(), startup::setIntegrantes);
        setIfPresent(request.localidadeSede(), startup::setLocalidadeSede);
        setIfPresent(request.localidadeAtuacao(), startup::setLocalidadeAtuacao);
        setIfPresent(request.ligadaIncubadora(), startup::setLigadaIncubadora);
        setIfPresent(request.incubadoraLigada(), startup::setIncubadoraLigada);
        setIfPresent(request.ufIbge(), startup::setUfIbge);
        setIfPresent(request.municipioIbge(), startup::setMunicipioIbge);
        setIfPresent(request.oQuePrecisaValidar(), startup::setOQuePrecisaValidar);
        setIfPresent(request.temasIds(), startup::setTemasIds);
        setIfPresent(request.modalidadesSuportadas(), startup::setModalidadesSuportadas);

        if (repo != null) {
            startup = repo.save(startup);
        }
        return toResponse(startup);
    }

    private static <T> void setIfPresent(T value, java.util.function.Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
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
