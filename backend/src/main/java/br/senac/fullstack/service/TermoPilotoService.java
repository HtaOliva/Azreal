package br.senac.fullstack.service;

import br.senac.fullstack.dto.AmbienteValidacaoResponseDTO;
import br.senac.fullstack.dto.ConfirmacaoExecucaoRequestDTO;
import br.senac.fullstack.dto.EntregaPilotoRequestDTO;
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
    private final ObjectProvider<PropostaPocService> propostaServiceProvider;
    private final ObjectProvider<RegistroOficialPocService> registroServiceProvider;
    private final Map<UUID, TermoPiloto> memory = new ConcurrentHashMap<>();

    @Autowired
    public TermoPilotoService(
            ObjectProvider<TermoPilotoRepository> repositoryProvider,
            ObjectProvider<AmbienteValidacaoService> ambienteServiceProvider,
            ObjectProvider<StartupService> startupServiceProvider,
            ObjectProvider<PropostaPocService> propostaServiceProvider,
            ObjectProvider<RegistroOficialPocService> registroServiceProvider) {
        this.repositoryProvider = repositoryProvider;
        this.ambienteServiceProvider = ambienteServiceProvider;
        this.startupServiceProvider = startupServiceProvider;
        this.propostaServiceProvider = propostaServiceProvider;
        this.registroServiceProvider = registroServiceProvider;
    }

    public TermoPilotoService(
            ObjectProvider<TermoPilotoRepository> repositoryProvider,
            ObjectProvider<AmbienteValidacaoService> ambienteServiceProvider,
            ObjectProvider<StartupService> startupServiceProvider,
            ObjectProvider<PropostaPocService> propostaServiceProvider) {
        this(repositoryProvider, ambienteServiceProvider, startupServiceProvider, propostaServiceProvider, null);
    }

    public TermoPilotoService(
            ObjectProvider<TermoPilotoRepository> repositoryProvider,
            ObjectProvider<AmbienteValidacaoService> ambienteServiceProvider,
            ObjectProvider<StartupService> startupServiceProvider) {
        this(repositoryProvider, ambienteServiceProvider, startupServiceProvider, null, null);
    }

    public TermoPilotoService(ObjectProvider<TermoPilotoRepository> repositoryProvider) {
        this(repositoryProvider, null, null, null, null);
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

    public TermoPiloto buscarPorIdOuPropostaId(UUID id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID e obrigatorio");
        }
        TermoPilotoRepository repo = repositoryProvider != null ? repositoryProvider.getIfAvailable() : null;
        if (repo != null) {
            try {
                var porId = repo.findById(id);
                if (porId.isPresent()) {
                    return porId.get();
                }
                var porProposta = repo.findByPropostaPocId(id);
                if (porProposta.isPresent()) {
                    return porProposta.get();
                }
            } catch (Exception ignored) {
                // fallback to memory
            }
        }
        TermoPiloto termo = memory.get(id);
        if (termo != null) {
            return termo;
        }
        return memory.values().stream()
                .filter(t -> id.equals(t.getPropostaPocId()))
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

    public TermoPiloto registrarEntrega(UUID id, EntregaPilotoRequestDTO request, Authentication authentication) {
        if (request == null || request.resumoEntrega() == null || request.resumoEntrega().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O resumo do que foi entregue e obrigatorio");
        }
        TermoPiloto termo = buscarPorIdOuPropostaId(id);

        if (authentication != null && !isAdmin(authentication)
                && !isStartupDonaDoTermo(termo.getStartupId(), authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (termo.getStatus() != StatusProposta.EM_TESTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Apenas pilotos com status EM_TESTE podem registrar entrega");
        }

        termo.registrarEntrega(request.resumoEntrega(), request.linksEvidencias(), request.observacoes());
        return salvar(termo);
    }

    public TermoPiloto confirmarExecucao(
            UUID id,
            ConfirmacaoExecucaoRequestDTO request,
            Authentication authentication) {
        if (request == null || request.pilotoExecutado() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A confirmacao de execucao do piloto e obrigatoria");
        }
        TermoPiloto termo = buscarPorIdOuPropostaId(id);

        if (authentication != null && !isAdmin(authentication)
                && !isAmbienteDonoDoTermo(termo.getAmbienteValidacaoId(), authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (termo.getStatus() != StatusProposta.EM_TESTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Apenas pilotos com status EM_TESTE podem ter execucao confirmada");
        }

        if (Boolean.TRUE.equals(request.pilotoExecutado())) {
            if (request.avaliacaoMetrica() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Avaliacao da metrica e obrigatoria quando o piloto foi executado");
            }
            if (request.parecer() == null || request.parecer().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Parecer avaliativo e obrigatorio quando o piloto foi executado");
            }
            if (request.modoPrivacidade() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Modo de privacidade e obrigatorio quando o piloto foi executado");
            }
        }

        termo.confirmarExecucao(
                request.pilotoExecutado(),
                request.avaliacaoMetrica(),
                request.parecer(),
                request.modoPrivacidade(),
                request.direcionamentoPosPoc(),
                request.justificativaNaoExecucao()
        );

        TermoPiloto salvo = salvar(termo);

        PropostaPocService propostaService = propostaServiceProvider != null
                ? propostaServiceProvider.getIfAvailable() : null;
        if (propostaService != null) {
            propostaService.atualizarStatus(termo.getPropostaPocId(), termo.getStatus());
        }

        if (salvo.getStatus() == StatusProposta.POC_CONCLUIDA) {
            RegistroOficialPocService registroService = registroServiceProvider != null
                    ? registroServiceProvider.getIfAvailable() : null;
            if (registroService != null) {
                try {
                    registroService.emitir(salvo);
                    salvo.setCertificadoEmitido(true);
                    salvo = salvar(salvo);
                } catch (Exception ignored) {
                    // segue em caso de falha controlada
                }
            }
        }

        return salvo;
    }

    public TermoPilotoResponseDTO obterTermoComIdentidadeRevelada(UUID propostaId, Authentication authentication) {
        TermoPiloto termo = buscarPorPropostaId(propostaId);
        if (isAcessoTermoNaoAutorizado(termo, authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        AmbienteValidacaoResponseDTO ambienteRevelado = buscarAmbienteRevelado(termo.getAmbienteValidacaoId());
        return TermoPilotoResponseDTO.fromEntity(termo, termo.getStatus(), ambienteRevelado);
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

    public boolean isStartupDonaDoTermo(UUID startupId, Authentication authentication) {
        StartupService startupService = startupServiceProvider != null
                ? startupServiceProvider.getIfAvailable() : null;
        if (startupService == null || startupId == null || authentication == null) {
            return false;
        }
        try {
            StartupResponseDTO startup = startupService.buscarPorEmail(authentication.getName());
            return startup != null && startupId.equals(startup.id());
        } catch (Exception ignored) {
            return false;
        }
    }

    public boolean isAmbienteDonoDoTermo(UUID ambienteId, Authentication authentication) {
        AmbienteValidacaoService ambienteService = ambienteServiceProvider != null
                ? ambienteServiceProvider.getIfAvailable() : null;
        if (ambienteService == null || ambienteId == null || authentication == null) {
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
        return TermoPilotoResponseDTO.fromEntity(termo, termo != null ? termo.getStatus() : StatusProposta.EM_TESTE,
                ambiente);
    }
}
