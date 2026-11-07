package br.senac.fullstack.service;

import br.senac.fullstack.dto.AmbienteValidacaoResponseDTO;
import br.senac.fullstack.dto.StartupResponseDTO;
import br.senac.fullstack.dto.TermoPilotoResponseDTO;
import br.senac.fullstack.model.StatusProposta;
import br.senac.fullstack.model.TermoPiloto;
import br.senac.fullstack.repository.TermoPilotoRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TermoPilotoService {

    private static final String TERMO_NAO_ENCONTRADO = "Termo de piloto nao encontrado";

    private final ObjectProvider<TermoPilotoRepository> repositoryProvider;
    private final ObjectProvider<AmbienteValidacaoService> ambienteServiceProvider;
    private final ObjectProvider<StartupService> startupServiceProvider;
    private final Map<UUID, TermoPiloto> memory = new ConcurrentHashMap<>();

    @Autowired
    public TermoPilotoService(
            ObjectProvider<TermoPilotoRepository> repositoryProvider,
            ObjectProvider<AmbienteValidacaoService> ambienteServiceProvider,
            ObjectProvider<StartupService> startupServiceProvider) {
        this.repositoryProvider = repositoryProvider;
        this.ambienteServiceProvider = ambienteServiceProvider;
        this.startupServiceProvider = startupServiceProvider;
    }

    public TermoPilotoService(ObjectProvider<TermoPilotoRepository> repositoryProvider) {
        this(repositoryProvider, null, null);
    }

    public TermoPiloto salvar(TermoPiloto termo) {
        if (termo == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Termo de piloto nao pode ser nulo");
        }
        TermoPilotoRepository repo = repositoryProvider != null ? repositoryProvider.getIfAvailable() : null;
        if (repo != null) {
            try {
                TermoPiloto saved = repo.save(termo);
                memory.put(saved.getId(), saved);
                return saved;
            } catch (Exception ignored) {
                // fallback to memory
            }
        }
        memory.put(termo.getId(), termo);
        return termo;
    }

    public TermoPiloto buscarPorId(UUID id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID do termo e obrigatorio");
        }
        TermoPilotoRepository repo = repositoryProvider != null ? repositoryProvider.getIfAvailable() : null;
        if (repo != null) {
            try {
                return repo.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, TERMO_NAO_ENCONTRADO));
            } catch (ResponseStatusException e) {
                throw e;
            } catch (Exception ignored) {
                // fallback to memory
            }
        }
        TermoPiloto termo = memory.get(id);
        if (termo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, TERMO_NAO_ENCONTRADO);
        }
        return termo;
    }

    public TermoPiloto buscarPorPropostaId(UUID propostaId) {
        if (propostaId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID da proposta e obrigatorio");
        }
        TermoPilotoRepository repo = repositoryProvider != null ? repositoryProvider.getIfAvailable() : null;
        if (repo != null) {
            try {
                var found = repo.findByPropostaPocId(propostaId);
                if (found.isPresent()) {
                    return found.get();
                }
            } catch (Exception ignored) {
                // fallback to memory
            }
        }
        return memory.values().stream()
                .filter(t -> propostaId.equals(t.getPropostaPocId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, TERMO_NAO_ENCONTRADO));
    }

    public List<TermoPiloto> listar() {
        TermoPilotoRepository repo = repositoryProvider != null ? repositoryProvider.getIfAvailable() : null;
        if (repo != null) {
            try {
                return repo.findAll();
            } catch (Exception ignored) {
                // fallback to memory
            }
        }
        return new ArrayList<>(memory.values());
    }

    public TermoPilotoResponseDTO obterTermoComIdentidadeRevelada(UUID propostaId, Authentication authentication) {
        TermoPiloto termo = buscarPorPropostaId(propostaId);
        if (isAcessoTermoNaoAutorizado(termo, authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        AmbienteValidacaoResponseDTO ambienteRevelado = buscarAmbienteRevelado(termo.getAmbienteValidacaoId());
        return TermoPilotoResponseDTO.fromEntity(termo, StatusProposta.EM_TESTE, ambienteRevelado);
    }

    public boolean isAcessoTermoNaoAutorizado(TermoPiloto termo, Authentication authentication) {
        if (authentication == null || isAdmin(authentication) || termo == null) {
            return false;
        }
        if (isStartupDonaDoTermo(termo.getStartupId(), authentication)) {
            return false;
        }
        return !isAmbienteDonoDoTermo(termo.getAmbienteValidacaoId(), authentication);
    }

    private boolean isStartupDonaDoTermo(UUID startupId, Authentication authentication) {
        StartupService startupService = startupServiceProvider != null
                ? startupServiceProvider.getIfAvailable() : null;
        if (startupService == null || startupId == null) {
            return false;
        }
        try {
            StartupResponseDTO startup = startupService.buscarPorEmail(authentication.getName());
            return startup != null && startupId.equals(startup.id());
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isAmbienteDonoDoTermo(UUID ambienteId, Authentication authentication) {
        AmbienteValidacaoService ambienteService = ambienteServiceProvider != null
                ? ambienteServiceProvider.getIfAvailable() : null;
        if (ambienteService == null || ambienteId == null) {
            return false;
        }
        try {
            AmbienteValidacaoResponseDTO ambiente = ambienteService.buscarPorEmail(authentication.getName());
            return ambiente != null && ambienteId.equals(ambiente.id());
        } catch (Exception ignored) {
            return false;
        }
    }

    public AmbienteValidacaoResponseDTO buscarAmbienteRevelado(UUID ambienteId) {
        if (ambienteId == null) {
            return null;
        }
        AmbienteValidacaoService ambienteService = ambienteServiceProvider != null
                ? ambienteServiceProvider.getIfAvailable() : null;
        if (ambienteService == null) {
            return null;
        }
        try {
            return ambienteService.buscar(ambienteId);
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public TermoPilotoResponseDTO construirResponseDTO(
            TermoPiloto termo,
            AmbienteValidacaoResponseDTO ambiente) {
        return TermoPilotoResponseDTO.fromEntity(termo, StatusProposta.EM_TESTE, ambiente);
    }
}
