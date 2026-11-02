package br.senac.fullstack.service;

import br.senac.fullstack.dto.AmbienteValidacaoRequestDTO;
import br.senac.fullstack.dto.AmbienteValidacaoResponseDTO;
import br.senac.fullstack.dto.AmbienteValidacaoUpdateDTO;
import br.senac.fullstack.model.AmbienteValidacao;
import br.senac.fullstack.repository.AmbienteValidacaoRepository;
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
public class AmbienteValidacaoService {

    private static final String AMBIENTE_NAO_ENCONTRADO = "Ambiente de validacao nao encontrado";

    private final ObjectProvider<AmbienteValidacaoRepository> repositoryProvider;
    private final AceiteTermoService aceiteTermoService;
    private final Map<UUID, AmbienteValidacao> memory = new ConcurrentHashMap<>();

    public AmbienteValidacaoService(ObjectProvider<AmbienteValidacaoRepository> repositoryProvider,
                                    AceiteTermoService aceiteTermoService) {
        this.repositoryProvider = repositoryProvider;
        this.aceiteTermoService = aceiteTermoService;
    }

    private AmbienteValidacaoRepository repository() {
        return repositoryProvider.getIfAvailable();
    }

    public AmbienteValidacaoResponseDTO criar(AmbienteValidacaoRequestDTO request) {
        if (!aceiteTermoService.existe(request.aceiteTermoId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aceite de termos obrigatorio");
        }
        AmbienteValidacao ambiente = new AmbienteValidacao();
        ambiente.setRazaoSocial(request.razaoSocial());
        ambiente.setNomeFantasia(request.nomeFantasia());
        ambiente.setSetor(request.setor());
        ambiente.setPorte(request.porte());
        ambiente.setUfIbge(request.ufIbge());
        ambiente.setMunicipioIbge(request.municipioIbge());
        ambiente.setEmailContato(request.emailContato());
        if (request.telefoneContato() != null) {
            ambiente.setTelefoneContato(request.telefoneContato());
        }
        if (request.linkedinUrl() != null) {
            ambiente.setLinkedinUrl(request.linkedinUrl());
        }

        AmbienteValidacaoRepository repo = repository();
        if (repo != null) {
            ambiente = repo.save(ambiente);
        } else {
            memory.put(ambiente.getId(), ambiente);
        }
        return toResponse(ambiente);
    }

    public AmbienteValidacaoResponseDTO buscar(UUID id) {
        AmbienteValidacaoRepository repo = repository();
        AmbienteValidacao ambiente;
        if (repo != null) {
            ambiente = repo.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AMBIENTE_NAO_ENCONTRADO));
        } else {
            ambiente = memory.get(id);
            if (ambiente == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, AMBIENTE_NAO_ENCONTRADO);
            }
        }
        return toResponse(ambiente);
    }

    public List<AmbienteValidacaoResponseDTO> listar() {
        AmbienteValidacaoRepository repo = repository();
        List<AmbienteValidacao> items;
        if (repo != null) {
            items = repo.findAll();
        } else {
            items = new ArrayList<>(memory.values());
        }
        return items.stream().map(this::toResponse).toList();
    }

    public AmbienteValidacaoResponseDTO atualizar(UUID id, AmbienteValidacaoUpdateDTO request) {
        AmbienteValidacaoRepository repo = repository();
        AmbienteValidacao ambiente;
        if (repo != null) {
            ambiente = repo.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AMBIENTE_NAO_ENCONTRADO));
        } else {
            ambiente = memory.get(id);
            if (ambiente == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, AMBIENTE_NAO_ENCONTRADO);
            }
        }
        if (request.razaoSocial() != null) {
            ambiente.setRazaoSocial(request.razaoSocial());
        }
        if (request.nomeFantasia() != null) {
            ambiente.setNomeFantasia(request.nomeFantasia());
        }
        if (request.setor() != null) {
            ambiente.setSetor(request.setor());
        }
        if (request.porte() != null) {
            ambiente.setPorte(request.porte());
        }
        if (request.ufIbge() != null) {
            ambiente.setUfIbge(request.ufIbge());
        }
        if (request.municipioIbge() != null) {
            ambiente.setMunicipioIbge(request.municipioIbge());
        }
        if (request.emailContato() != null) {
            ambiente.setEmailContato(request.emailContato());
        }
        if (request.telefoneContato() != null) {
            ambiente.setTelefoneContato(request.telefoneContato());
        }
        if (request.linkedinUrl() != null) {
            ambiente.setLinkedinUrl(request.linkedinUrl());
        }

        if (repo != null) {
            ambiente = repo.save(ambiente);
        }
        return toResponse(ambiente);
    }

    private AmbienteValidacaoResponseDTO toResponse(AmbienteValidacao a) {
        return new AmbienteValidacaoResponseDTO(
                a.getId(), a.getRazaoSocial(), a.getNomeFantasia(), a.getSetor(),
                a.getPorte(), a.getUfIbge(), a.getMunicipioIbge(), a.getEmailContato(),
                a.getTelefoneContato(), a.getLinkedinUrl(), a.isAtivo(), a.getDataCadastro());
    }
}
