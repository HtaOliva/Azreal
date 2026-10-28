package br.senac.fullstack.service;

import br.senac.fullstack.dto.MentorRequestDTO;
import br.senac.fullstack.dto.MentorResponseDTO;
import br.senac.fullstack.dto.MentorUpdateDTO;
import br.senac.fullstack.model.Mentor;
import br.senac.fullstack.repository.MentorRepository;
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
public class MentorService {

    private static final String MENTOR_NAO_ENCONTRADO = "Mentor nao encontrado";

    private final ObjectProvider<MentorRepository> repositoryProvider;
    private final AceiteTermoService aceiteTermoService;
    private final Map<UUID, Mentor> memory = new ConcurrentHashMap<>();

    public MentorService(ObjectProvider<MentorRepository> repositoryProvider, AceiteTermoService aceiteTermoService) {
        this.repositoryProvider = repositoryProvider;
        this.aceiteTermoService = aceiteTermoService;
    }

    private MentorRepository repository() {
        return repositoryProvider.getIfAvailable();
    }

    public MentorResponseDTO criar(MentorRequestDTO request) {
        if (!aceiteTermoService.existe(request.aceiteTermoId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aceite de termos obrigatorio");
        }
        Mentor mentor = new Mentor();
        mentor.setNome(request.nome());
        mentor.setEmail(request.email());
        mentor.setAreasAtuacao(request.areasAtuacao());
        if (request.temas() != null) mentor.setTemas(request.temas());
        mentor.setExperiencia(request.experiencia());
        mentor.setDisponibilidade(request.disponibilidade());
        if (request.localidade() != null) mentor.setLocalidade(request.localidade());
        if (request.telefone() != null) mentor.setTelefone(request.telefone());
        if (request.linkedin() != null) mentor.setLinkedin(request.linkedin());
        mentor.setAprovado(mentor.isPerfilCompleto());

        MentorRepository repo = repository();
        if (repo != null) {
            mentor = repo.save(mentor);
        } else {
            memory.put(mentor.getId(), mentor);
        }
        return toResponse(mentor);
    }

    public Mentor buscarEntidadePorEmail(String email) {
        MentorRepository repo = repository();
        if (repo != null) {
            return repo.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MENTOR_NAO_ENCONTRADO));
        }
        return memory.values().stream()
                .filter(m -> email.equals(m.getEmail()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MENTOR_NAO_ENCONTRADO));
    }

    public void exigirPerfilCompleto(UUID id) {
        MentorResponseDTO m = buscar(id);
        if (!m.perfilCompleto()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Perfil do mentor incompleto");
        }
    }

    public MentorResponseDTO buscar(UUID id) {
        MentorRepository repo = repository();
        Mentor mentor;
        if (repo != null) {
            mentor = repo.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MENTOR_NAO_ENCONTRADO));
        } else {
            mentor = memory.get(id);
            if (mentor == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, MENTOR_NAO_ENCONTRADO);
            }
        }
        return toResponse(mentor);
    }

    public List<MentorResponseDTO> listar() {
        MentorRepository repo = repository();
        List<Mentor> items;
        if (repo != null) {
            items = repo.findAll();
        } else {
            items = new ArrayList<>(memory.values());
        }
        List<MentorResponseDTO> result = new ArrayList<>();
        for (Mentor m : items) {
            result.add(toResponse(m));
        }
        return result;
    }

    public MentorResponseDTO atualizar(UUID id, MentorUpdateDTO request) {
        MentorRepository repo = repository();
        Mentor mentor;
        if (repo != null) {
            mentor = repo.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MENTOR_NAO_ENCONTRADO));
        } else {
            mentor = memory.get(id);
            if (mentor == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, MENTOR_NAO_ENCONTRADO);
            }
        }
        if (request.nome() != null) mentor.setNome(request.nome());
        if (request.areasAtuacao() != null) mentor.setAreasAtuacao(request.areasAtuacao());
        if (request.temas() != null) mentor.setTemas(request.temas());
        if (request.experiencia() != null) mentor.setExperiencia(request.experiencia());
        if (request.disponibilidade() != null) mentor.setDisponibilidade(request.disponibilidade());
        if (request.localidade() != null) mentor.setLocalidade(request.localidade());
        if (request.telefone() != null) mentor.setTelefone(request.telefone());
        if (request.linkedin() != null) mentor.setLinkedin(request.linkedin());
        mentor.setAprovado(mentor.isAprovado() || mentor.isPerfilCompleto());

        if (repo != null) {
            mentor = repo.save(mentor);
        }
        return toResponse(mentor);
    }

    private MentorResponseDTO toResponse(Mentor m) {
        return new MentorResponseDTO(
                m.getId(), m.getNome(), m.getEmail(), m.getAreasAtuacao(), m.getTemas(),
                m.getExperiencia(), m.getDisponibilidade(), m.getLocalidade(), m.getTelefone(),
                m.getLinkedin(), m.isPerfilCompleto(), m.isAprovado(), m.getDataCadastro());
    }
}