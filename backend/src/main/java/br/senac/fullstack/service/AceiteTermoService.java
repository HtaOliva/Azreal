package br.senac.fullstack.service;

import br.senac.fullstack.dto.AceiteTermoRequestDTO;
import br.senac.fullstack.dto.AceiteTermoResponseDTO;
import br.senac.fullstack.model.AceiteTermo;
import br.senac.fullstack.repository.AceiteTermoRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AceiteTermoService {

    private final ObjectProvider<AceiteTermoRepository> repositoryProvider;
    private final Map<UUID, AceiteTermo> memory = new ConcurrentHashMap<>();

    public AceiteTermoService(ObjectProvider<AceiteTermoRepository> repositoryProvider) {
        this.repositoryProvider = repositoryProvider;
    }

    private AceiteTermoRepository repository() {
        return repositoryProvider.getIfAvailable();
    }

    public AceiteTermoResponseDTO registrar(AceiteTermoRequestDTO request, UUID usuarioId, String ip) {
        if (Boolean.FALSE.equals(request.consentimentoIaAnonimizado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Consentimento para processamento anonimizado pela IA e obrigatorio (RNF05)");
        }
        AceiteTermo aceite = new AceiteTermo();
        aceite.setUsuarioId(usuarioId);
        aceite.setVersaoTermo(request.versaoTermo());
        aceite.setConsentimentoIaAnonimizado(true);
        aceite.setIp(ip != null ? ip : request.ip());
        AceiteTermoRepository repo = repository();
        if (repo != null) {
            aceite = repo.save(aceite);
        } else {
            memory.put(aceite.getId(), aceite);
        }
        return new AceiteTermoResponseDTO(
                aceite.getId(), aceite.getVersaoTermo(), aceite.getDataHora(), aceite.isConsentimentoIaAnonimizado());
    }

    public boolean existe(UUID id) {
        if (id == null) {
            return false;
        }
        AceiteTermoRepository repo = repository();
        if (repo != null) {
            return repo.existsById(id);
        }
        return memory.containsKey(id);
    }

    public AceiteTermo buscar(UUID id) {
        AceiteTermoRepository repo = repository();
        if (repo != null) {
            return repo.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Aceite de termos nao encontrado"));
        }
        AceiteTermo aceite = memory.get(id);
        if (aceite == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aceite de termos nao encontrado");
        }
        return aceite;
    }
}
