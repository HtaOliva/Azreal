package br.senac.fullstack.service;

import br.senac.fullstack.dto.AmbienteValidacaoResponseDTO;
import br.senac.fullstack.dto.RegistroOficialPocResponseDTO;
import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.model.RegistroOficialPoc;
import br.senac.fullstack.model.StatusProposta;
import br.senac.fullstack.model.TermoPiloto;
import br.senac.fullstack.repository.RegistroOficialPocRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RegistroOficialPocService {

    private static final String REGISTRO_NAO_ENCONTRADO = "Registro Oficial de POC nao encontrado";
    private static final String ID_STARTUP_OBRIGATORIO = "ID da startup e obrigatorio";

    private final ObjectProvider<RegistroOficialPocRepository> repositoryProvider;
    private final ObjectProvider<AmbienteValidacaoService> ambienteServiceProvider;
    private final ObjectProvider<AuditoriaService> auditoriaServiceProvider;
    private final Map<UUID, RegistroOficialPoc> memory = new ConcurrentHashMap<>();

    @Autowired
    public RegistroOficialPocService(
            ObjectProvider<RegistroOficialPocRepository> repositoryProvider,
            ObjectProvider<AmbienteValidacaoService> ambienteServiceProvider,
            ObjectProvider<AuditoriaService> auditoriaServiceProvider) {
        this.repositoryProvider = repositoryProvider;
        this.ambienteServiceProvider = ambienteServiceProvider;
        this.auditoriaServiceProvider = auditoriaServiceProvider;
    }

    public RegistroOficialPocService(
            ObjectProvider<RegistroOficialPocRepository> repositoryProvider,
            ObjectProvider<AmbienteValidacaoService> ambienteServiceProvider) {
        this(repositoryProvider, ambienteServiceProvider, null);
    }

    public RegistroOficialPocService(ObjectProvider<RegistroOficialPocRepository> repositoryProvider) {
        this(repositoryProvider, null, null);
    }

    public RegistroOficialPocService() {
        this(null, null, null);
    }

    public RegistroOficialPocResponseDTO emitir(TermoPiloto termo) {
        validarTermoParaEmissao(termo);

        // Evita emissao duplicada para o mesmo termo de piloto
        Optional<RegistroOficialPoc> existente = buscarPorTermoPilotoId(termo.getId());
        if (existente.isPresent()) {
            return RegistroOficialPocResponseDTO.fromEntity(existente.get());
        }

        AmbienteValidacaoResponseDTO ambiente = resolverAmbiente(termo.getAmbienteValidacaoId());
        String nomeOrg = extrairNomeOrganizacao(ambiente);
        String setorOrg = ambiente != null ? ambiente.setor() : null;
        PorteOrganizacao porteOrg = ambiente != null ? ambiente.porte() : null;

        RegistroOficialPoc registro = RegistroOficialPoc.emitir(termo, nomeOrg, setorOrg, porteOrg);
        salvar(registro);
        termo.setCertificadoEmitido(true);

        registrarAuditoriaEmissao(termo, registro);

        return RegistroOficialPocResponseDTO.fromEntity(registro);
    }

    private void validarTermoParaEmissao(TermoPiloto termo) {
        if (termo == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Termo de piloto e obrigatorio");
        }
        if (termo.getStatus() != StatusProposta.POC_CONCLUIDA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Apenas pilotos concluidos podem emitir Registro Oficial de POC");
        }
    }

    private AmbienteValidacaoResponseDTO resolverAmbiente(UUID ambienteId) {
        if (ambienteId == null || ambienteServiceProvider == null) {
            return null;
        }
        AmbienteValidacaoService ambienteService = ambienteServiceProvider.getIfAvailable();
        if (ambienteService == null) {
            return null;
        }
        try {
            return ambienteService.buscar(ambienteId);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String extrairNomeOrganizacao(AmbienteValidacaoResponseDTO ambiente) {
        if (ambiente == null) {
            return null;
        }
        return ambiente.razaoSocial() != null ? ambiente.razaoSocial() : ambiente.nomeFantasia();
    }

    private void registrarAuditoriaEmissao(TermoPiloto termo, RegistroOficialPoc registro) {
        AuditoriaService auditoriaService = auditoriaServiceProvider != null
                ? auditoriaServiceProvider.getIfAvailable() : null;
        if (auditoriaService != null) {
            try {
                auditoriaService.registrar(
                        termo.getPropostaPocId(),
                        termo.getId(),
                        StatusProposta.POC_CONCLUIDA,
                        StatusProposta.POC_CONCLUIDA,
                        "SISTEMA",
                        "EMISSAO_REGISTRO_POC",
                        "Registro Oficial de POC emitido com codigo: " + registro.getCodigoVerificador()
                );
            } catch (Exception ignored) {
                // segue sem erro se falhar auditoria
            }
        }
    }

    public RegistroOficialPoc salvar(RegistroOficialPoc registro) {
        if (registro == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Registro nao pode ser nulo");
        }
        RegistroOficialPocRepository repo = repositoryProvider != null ? repositoryProvider.getIfAvailable() : null;
        if (repo != null) {
            try {
                RegistroOficialPoc saved = repo.save(registro);
                memory.put(saved.getId(), saved);
                return saved;
            } catch (Exception ignored) {
                // fallback to memory
            }
        }
        memory.put(registro.getId(), registro);
        return registro;
    }

    public List<RegistroOficialPocResponseDTO> listarPorStartup(UUID startupId) {
        if (startupId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ID_STARTUP_OBRIGATORIO);
        }
        RegistroOficialPocRepository repo = repositoryProvider != null ? repositoryProvider.getIfAvailable() : null;
        if (repo != null) {
            try {
                List<RegistroOficialPoc> lista = repo.findByStartupIdOrderByDataEmissaoDesc(startupId);
                if (lista != null && !lista.isEmpty()) {
                    return lista.stream().map(RegistroOficialPocResponseDTO::fromEntity).toList();
                }
            } catch (Exception ignored) {
                // fallback to memory
            }
        }
        return memory.values().stream()
                .filter(r -> startupId.equals(r.getStartupId()))
                .sorted(Comparator.comparing(RegistroOficialPoc::getDataEmissao).reversed())
                .map(RegistroOficialPocResponseDTO::fromEntity)
                .toList();
    }

    public RegistroOficialPocResponseDTO buscarPorId(UUID id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID do registro e obrigatorio");
        }
        RegistroOficialPocRepository repo = repositoryProvider != null ? repositoryProvider.getIfAvailable() : null;
        if (repo != null) {
            try {
                return repo.findById(id)
                        .map(RegistroOficialPocResponseDTO::fromEntity)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, REGISTRO_NAO_ENCONTRADO));
            } catch (ResponseStatusException e) {
                throw e;
            } catch (Exception ignored) {
                // fallback to memory
            }
        }
        RegistroOficialPoc registro = memory.get(id);
        if (registro == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, REGISTRO_NAO_ENCONTRADO);
        }
        return RegistroOficialPocResponseDTO.fromEntity(registro);
    }

    public RegistroOficialPocResponseDTO buscarPorCodigoVerificador(String codigoVerificador) {
        if (codigoVerificador == null || codigoVerificador.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Codigo verificador e obrigatorio");
        }
        RegistroOficialPocRepository repo = repositoryProvider != null ? repositoryProvider.getIfAvailable() : null;
        if (repo != null) {
            try {
                return repo.findByCodigoVerificador(codigoVerificador.trim())
                        .map(RegistroOficialPocResponseDTO::fromEntity)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, REGISTRO_NAO_ENCONTRADO));
            } catch (ResponseStatusException e) {
                throw e;
            } catch (Exception ignored) {
                // fallback to memory
            }
        }
        return memory.values().stream()
                .filter(r -> codigoVerificador.trim().equalsIgnoreCase(r.getCodigoVerificador()))
                .findFirst()
                .map(RegistroOficialPocResponseDTO::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, REGISTRO_NAO_ENCONTRADO));
    }

    public Optional<RegistroOficialPoc> buscarPorTermoPilotoId(UUID termoPilotoId) {
        if (termoPilotoId == null) {
            return Optional.empty();
        }
        RegistroOficialPocRepository repo = repositoryProvider != null ? repositoryProvider.getIfAvailable() : null;
        if (repo != null) {
            try {
                var found = repo.findByTermoPilotoId(termoPilotoId);
                if (found.isPresent()) {
                    return found;
                }
            } catch (Exception ignored) {
                // fallback to memory
            }
        }
        return memory.values().stream()
                .filter(r -> termoPilotoId.equals(r.getTermoPilotoId()))
                .findFirst();
    }
}
