package br.senac.fullstack.service;

import br.senac.fullstack.dto.DesafioAnonimoResponseDTO;
import br.senac.fullstack.model.DesafioValidacao;
import br.senac.fullstack.model.ModalidadeTeste;
import br.senac.fullstack.model.Startup;
import br.senac.fullstack.model.StatusDesafio;
import br.senac.fullstack.repository.DesafioValidacaoRepository;
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
    private final ObjectProvider<DesafioValidacaoRepository> desafioRepositoryProvider;

    public MatchmakingService(
            ObjectProvider<StartupService> startupServiceProvider,
            ObjectProvider<StartupRepository> startupRepositoryProvider,
            ObjectProvider<DesafioValidacaoRepository> desafioRepositoryProvider) {
        this.startupServiceProvider = startupServiceProvider;
        this.startupRepositoryProvider = startupRepositoryProvider;
        this.desafioRepositoryProvider = desafioRepositoryProvider;
    }

    public boolean atendeCriteriosObjetivos(Startup startup, DesafioValidacao desafio) {
        if (startup == null || desafio == null) {
            return false;
        }

        // 1. Restrição Rígida: Status Ativo do Desafio e da Organização
        if (!desafio.isAtivo()) {
            return false;
        }
        if (desafio.getStatus() != null && desafio.getStatus() != StatusDesafio.ABERTO) {
            return false;
        }
        if (desafio.getAmbienteValidacao() != null && !desafio.getAmbienteValidacao().isAtivo()) {
            return false;
        }

        // 2. Restrição Rígida: Modalidade de Execução do Piloto
        if (!compativelModalidade(startup, desafio)) {
            return false;
        }

        // 3. Restrição Rígida: Localidade Alvo (UF / Município IBGE)
        return compativelLocalidade(startup, desafio);
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

        if (modalidadeDesafio == ModalidadeTeste.REMOTO) {
            return modalidadesStartup.contains(ModalidadeTeste.REMOTO)
                    || modalidadesStartup.contains(ModalidadeTeste.HIBRIDO);
        }
        if (modalidadeDesafio == ModalidadeTeste.PRESENCIAL) {
            return modalidadesStartup.contains(ModalidadeTeste.PRESENCIAL)
                    || modalidadesStartup.contains(ModalidadeTeste.HIBRIDO);
        }
        if (modalidadeDesafio == ModalidadeTeste.HIBRIDO) {
            return modalidadesStartup.contains(ModalidadeTeste.HIBRIDO)
                    || (modalidadesStartup.contains(ModalidadeTeste.PRESENCIAL)
                    && modalidadesStartup.contains(ModalidadeTeste.REMOTO));
        }
        return false;
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

        // Se o desafio presencial ou híbrido possui UF alvo definida
        if (ufDesafio != null && !ufDesafio.isBlank()) {
            if (ufStartup == null || !ufDesafio.trim().equalsIgnoreCase(ufStartup.trim())) {
                return false;
            }
        }

        // Se o desafio presencial possui município definido
        if (modalidadeDesafio == ModalidadeTeste.PRESENCIAL
                && municipioDesafio != null && !municipioDesafio.isBlank()) {
            if (municipioStartup != null && !municipioStartup.isBlank()) {
                if (!municipioDesafio.trim().equalsIgnoreCase(municipioStartup.trim())) {
                    return false;
                }
            }
        }

        return true;
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

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, STARTUP_NAO_ENCONTRADA);
    }

    public List<DesafioAnonimoResponseDTO> preFiltrarDesafiosParaStartup(UUID startupId, List<DesafioValidacao> desafios) {
        Startup startup = obterStartup(startupId);
        return filtrarDesafiosAnonimos(startup, desafios);
    }
}
