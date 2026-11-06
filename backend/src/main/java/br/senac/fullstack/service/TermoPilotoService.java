package br.senac.fullstack.service;

import br.senac.fullstack.dto.AmbienteValidacaoResponseDTO;
import br.senac.fullstack.dto.TermoPilotoResponseDTO;
import br.senac.fullstack.model.StatusProposta;
import br.senac.fullstack.model.TermoPiloto;
import br.senac.fullstack.repository.TermoPilotoRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    private final Map<UUID, TermoPiloto> memory = new ConcurrentHashMap<>();

    @Autowired
    public TermoPilotoService(ObjectProvider<TermoPilotoRepository> repositoryProvider) {
        this.repositoryProvider = repositoryProvider;
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

    public TermoPilotoResponseDTO construirResponseDTO(
            TermoPiloto termo,
            AmbienteValidacaoResponseDTO ambiente) {
        return TermoPilotoResponseDTO.fromEntity(termo, StatusProposta.EM_TESTE, ambiente);
    }
}
