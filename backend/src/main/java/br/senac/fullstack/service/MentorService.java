package br.senac.fullstack.service;

import br.senac.fullstack.dto.MentorRequestDTO;
import br.senac.fullstack.dto.MentorResponseDTO;
import br.senac.fullstack.dto.MentorUpdateDTO;
import br.senac.fullstack.model.Mentor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MentorService {

    private final Map<UUID, Mentor> store = new ConcurrentHashMap<>();
    private final AceiteTermoService aceiteTermoService;

    public MentorService(AceiteTermoService aceiteTermoService) {
        this.aceiteTermoService = aceiteTermoService;
    }

    public MentorResponseDTO criar(MentorRequestDTO request) {
        if (!aceiteTermoService.existe(request.aceiteTermoId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aceite de termos obrigatorio");
        }
        Mentor mentor = new Mentor();
        mentor.setNome(request.nome());
        mentor.setEmail(request.email());
        mentor.setAreasAtuacao(request.areasAtuacao());
        mentor.setDisponibilidade(request.disponibilidade());
        store.put(mentor.getId(), mentor);
        return toResponse(mentor);
    }

    public MentorResponseDTO buscar(UUID id) {
        Mentor mentor = store.get(id);
        if (mentor == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mentor nao encontrado");
        }
        return toResponse(mentor);
    }

    public List<MentorResponseDTO> listar() {
        List<MentorResponseDTO> result = new ArrayList<>();
        for (Mentor m : store.values()) {
            result.add(toResponse(m));
        }
        return result;
    }

    public MentorResponseDTO atualizar(UUID id, MentorUpdateDTO request) {
        Mentor mentor = store.get(id);
        if (mentor == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mentor nao encontrado");
        }
        if (request.nome() != null) mentor.setNome(request.nome());
        if (request.areasAtuacao() != null) mentor.setAreasAtuacao(request.areasAtuacao());
        if (request.disponibilidade() != null) mentor.setDisponibilidade(request.disponibilidade());
        return toResponse(mentor);
    }

    private MentorResponseDTO toResponse(Mentor m) {
        return new MentorResponseDTO(
                m.getId(), m.getNome(), m.getEmail(), m.getAreasAtuacao(),
                m.getDisponibilidade(), m.getDataCadastro());
    }
}
