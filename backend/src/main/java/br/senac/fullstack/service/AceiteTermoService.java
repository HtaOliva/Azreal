package br.senac.fullstack.service;

import br.senac.fullstack.dto.AceiteTermoRequestDTO;
import br.senac.fullstack.dto.AceiteTermoResponseDTO;
import br.senac.fullstack.model.AceiteTermo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AceiteTermoService {

    private final Map<UUID, AceiteTermo> store = new ConcurrentHashMap<>();

    public AceiteTermoResponseDTO registrar(AceiteTermoRequestDTO request) {
        AceiteTermo aceite = new AceiteTermo();
        aceite.setVersaoTermo(request.versaoTermo());
        aceite.setIp(request.ip());
        store.put(aceite.getId(), aceite);
        return new AceiteTermoResponseDTO(aceite.getId(), aceite.getVersaoTermo(), aceite.getDataHora());
    }

    public boolean existe(UUID id) {
        return id != null && store.containsKey(id);
    }

    public AceiteTermo buscar(UUID id) {
        AceiteTermo aceite = store.get(id);
        if (aceite == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aceite de termos nao encontrado");
        }
        return aceite;
    }
}
