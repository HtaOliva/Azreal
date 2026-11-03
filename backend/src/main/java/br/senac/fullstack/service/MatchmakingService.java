package br.senac.fullstack.service;

import br.senac.fullstack.dto.DesafioAnonimoResponseDTO;
import br.senac.fullstack.dto.StartupResponseDTO;
import br.senac.fullstack.model.DesafioValidacao;
import br.senac.fullstack.model.ModalidadeTeste;
import br.senac.fullstack.model.Startup;
import br.senac.fullstack.model.StatusDesafio;
import br.senac.fullstack.repository.StartupRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class MatchmakingService {

    private static final String STARTUP_NAO_ENCONTRADA = "Startup nao encontrada";

    private final ObjectProvider<StartupService> startupServiceProvider;
    private final ObjectProvider<StartupRepository> startupRepositoryProvider;

    public MatchmakingService(
            ObjectProvider<StartupService> startupServiceProvider,
            ObjectProvider<StartupRepository> startupRepositoryProvider) {
        this.startupServiceProvider = startupServiceProvider;
        this.startupRepositoryProvider = startupRepositoryProvider;
    }

    public boolean atendeCriteriosObjetivos(Startup startup, DesafioValidacao desafio) {
        if (startup == null || desafio == null) {
            return false;
        }

        boolean ativo = desafio.isAtivo()
                && (desafio.getStatus() == null || desafio.getStatus() == StatusDesafio.ABERTO)
                && (desafio.getAmbienteValidacao() == null || desafio.getAmbienteValidacao().isAtivo());

        return ativo
                && compativelModalidade(startup, desafio)
                && compativelLocalidade(startup, desafio);
    }

    private boolean compativelModalidade(Startup startup, DesafioValidacao desafio) {
        ModalidadeTeste modalidadeDesafio = desafio.getModalidade();
        if (modalidadeDesafio == null) {
            return true;
        }

        List<ModalidadeTeste> modalidadesStartup = startup.getModalidadesSuportadas();
        if (modalidadesStartup == null || modalidadesStartup.isEmpty()) {
            return false;
        }

        boolean remoto = modalidadeDesafio == ModalidadeTeste.REMOTO
                && (modalidadesStartup.contains(ModalidadeTeste.REMOTO)
                || modalidadesStartup.contains(ModalidadeTeste.HIBRIDO));

        boolean presencial = modalidadeDesafio == ModalidadeTeste.PRESENCIAL
                && (modalidadesStartup.contains(ModalidadeTeste.PRESENCIAL)
                || modalidadesStartup.contains(ModalidadeTeste.HIBRIDO));

        boolean hibrido = modalidadeDesafio == ModalidadeTeste.HIBRIDO
                && (modalidadesStartup.contains(ModalidadeTeste.HIBRIDO)
                || (modalidadesStartup.contains(ModalidadeTeste.PRESENCIAL)
                && modalidadesStartup.contains(ModalidadeTeste.REMOTO)));

        return remoto || presencial || hibrido;
    }

    private boolean compativelLocalidade(Startup startup, DesafioValidacao desafio) {
        ModalidadeTeste modalidadeDesafio = desafio.getModalidade();
        // Em modalidade 100% remota, a localidade geográfica não é restritiva
        if (modalidadeDesafio == ModalidadeTeste.REMOTO) {
            return true;
        }

        String ufDesafio = desafio.getUfIbge();
        String municipioDesafio = desafio.getMunicipioIbge();

        String ufStartup = obterUfStartup(startup);
        String municipioStartup = obterMunicipioStartup(startup);

        boolean ufInvalida = ufDesafio != null && !ufDesafio.isBlank()
                && (ufStartup == null || !ufDesafio.trim().equalsIgnoreCase(ufStartup.trim()));

        boolean municipioInvalido = modalidadeDesafio == ModalidadeTeste.PRESENCIAL
                && municipioDesafio != null && !municipioDesafio.isBlank()
                && municipioStartup != null && !municipioStartup.isBlank()
                && !municipioDesafio.trim().equalsIgnoreCase(municipioStartup.trim());

        return !ufInvalida && !municipioInvalido;
    }

    private String obterUfStartup(Startup startup) {
        if (startup.getUfIbge() != null && !startup.getUfIbge().isBlank()) {
            return startup.getUfIbge();
        }
        return startup.getLocalidadeAtuacao();
    }

    private String obterMunicipioStartup(Startup startup) {
        if (startup.getMunicipioIbge() != null && !startup.getMunicipioIbge().isBlank()) {
            return startup.getMunicipioIbge();
        }
        return startup.getLocalidadeSede();
    }

    public List<DesafioValidacao> filtrar(Startup startup, List<DesafioValidacao> desafios) {
        if (startup == null || desafios == null) {
            return List.of();
        }
        return desafios.stream()
                .filter(d -> atendeCriteriosObjetivos(startup, d))
                .toList();
    }

    public List<DesafioAnonimoResponseDTO> filtrarDesafiosAnonimos(Startup startup, List<DesafioValidacao> desafios) {
        return filtrar(startup, desafios).stream()
                .map(DesafioAnonimoResponseDTO::fromEntity)
                .toList();
    }

    public Startup obterStartup(UUID startupId) {
        if (startupId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID da startup e obrigatorio");
        }

        StartupRepository repo = startupRepositoryProvider.getIfAvailable();
        if (repo != null) {
            return repo.findById(startupId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, STARTUP_NAO_ENCONTRADA));
        }

        StartupService service = startupServiceProvider.getIfAvailable();
        if (service != null) {
            var dto = service.buscar(startupId);
            return converterDtoParaStartup(dto);
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, STARTUP_NAO_ENCONTRADA);
    }

    private Startup converterDtoParaStartup(StartupResponseDTO dto) {
        Startup startup = new Startup();
        startup.setId(dto.id());
        startup.setNome(dto.nome());
        startup.setEmail(dto.email());
        startup.setUfIbge(dto.ufIbge());
        startup.setMunicipioIbge(dto.municipioIbge());
        startup.setLocalidadeSede(dto.localidadeSede());
        startup.setLocalidadeAtuacao(dto.localidadeAtuacao());
        startup.setModalidadesSuportadas(dto.modalidadesSuportadas());
        startup.setTemasIds(dto.temasIds());
        return startup;
    }

    public List<DesafioAnonimoResponseDTO> preFiltrarDesafiosParaStartup(UUID startupId, List<DesafioValidacao> desafios) {
        Startup startup = obterStartup(startupId);
        return filtrarDesafiosAnonimos(startup, desafios);
    }
}
