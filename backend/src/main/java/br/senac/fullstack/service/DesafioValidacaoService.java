package br.senac.fullstack.service;

import br.senac.fullstack.dto.DesafioAnonimoResponseDTO;
import br.senac.fullstack.dto.DesafioValidacaoRequestDTO;
import br.senac.fullstack.dto.DesafioValidacaoResponseDTO;
import br.senac.fullstack.dto.DesafioValidacaoUpdateDTO;
import br.senac.fullstack.model.AmbienteValidacao;
import br.senac.fullstack.model.DesafioValidacao;
import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.model.StatusDesafio;
import br.senac.fullstack.repository.AmbienteValidacaoRepository;
import br.senac.fullstack.repository.DesafioValidacaoRepository;
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
public class DesafioValidacaoService {

    private static final String DESAFIO_NAO_ENCONTRADO = "Desafio de validacao nao encontrado";

    private final ObjectProvider<DesafioValidacaoRepository> repositoryProvider;
    private final ObjectProvider<AmbienteValidacaoRepository> ambienteRepositoryProvider;
    private final ObjectProvider<AmbienteValidacaoService> ambienteServiceProvider;
    private final Map<UUID, DesafioValidacao> memory = new ConcurrentHashMap<>();

    public DesafioValidacaoService(
            ObjectProvider<DesafioValidacaoRepository> repositoryProvider,
            ObjectProvider<AmbienteValidacaoRepository> ambienteRepositoryProvider,
            ObjectProvider<AmbienteValidacaoService> ambienteServiceProvider) {
        this.repositoryProvider = repositoryProvider;
        this.ambienteRepositoryProvider = ambienteRepositoryProvider;
        this.ambienteServiceProvider = ambienteServiceProvider;
    }

    private DesafioValidacaoRepository repository() {
        return repositoryProvider.getIfAvailable();
    }

    private AmbienteValidacao obterAmbienteValidacao(UUID ambienteId) {
        if (ambienteId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID do ambiente de validacao e obrigatorio");
        }

        AmbienteValidacaoRepository repo = ambienteRepositoryProvider.getIfAvailable();
        if (repo != null) {
            return repo.findById(ambienteId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ambiente de validacao nao encontrado"));
        }

        AmbienteValidacaoService service = ambienteServiceProvider.getIfAvailable();
        if (service != null) {
            var dto = service.buscar(ambienteId);
            AmbienteValidacao ambiente = new AmbienteValidacao();
            ambiente.setId(dto.id());
            ambiente.setRazaoSocial(dto.razaoSocial());
            ambiente.setNomeFantasia(dto.nomeFantasia());
            ambiente.setSetor(dto.setor());
            ambiente.setPorte(dto.porte());
            ambiente.setUfIbge(dto.ufIbge());
            ambiente.setMunicipioIbge(dto.municipioIbge());
            ambiente.setEmailContato(dto.emailContato());
            ambiente.setTelefoneContato(dto.telefoneContato());
            ambiente.setLinkedinUrl(dto.linkedinUrl());
            return ambiente;
        }

        // Fallback em memória quando executado isolado sem banco ou servicos
        AmbienteValidacao fallback = new AmbienteValidacao();
        fallback.setId(ambienteId);
        fallback.setNomeFantasia("Ambiente Validação");
        fallback.setRazaoSocial("Organização Parceira Ltda");
        fallback.setSetor("Tecnologia e Inovação");
        fallback.setPorte(PorteOrganizacao.MEDIO);
        fallback.setUfIbge("PE");
        fallback.setMunicipioIbge("Recife");
        fallback.setEmailContato("contato@ambiente.com.br");
        return fallback;
    }

    public DesafioValidacaoResponseDTO criar(DesafioValidacaoRequestDTO request) {
        AmbienteValidacao ambiente = obterAmbienteValidacao(request.ambienteValidacaoId());

        DesafioValidacao desafio = new DesafioValidacao();
        desafio.setAmbienteValidacao(ambiente);
        desafio.setTitulo(request.titulo());
        desafio.setDescricaoProblema(request.descricaoProblema());
        desafio.setPublicoAlvo(request.publicoAlvo());
        desafio.setTamanhoAmostra(request.tamanhoAmostra());
        desafio.setModalidade(request.modalidade());
        desafio.setDuracaoMaxima(request.duracaoMaxima());
        desafio.setStatus(StatusDesafio.ABERTO);
        desafio.setAtivo(true);

        DesafioValidacaoRepository repo = repository();
        if (repo != null) {
            desafio = repo.save(desafio);
        } else {
            memory.put(desafio.getId(), desafio);
        }
        return DesafioValidacaoResponseDTO.fromEntity(desafio);
    }

    private DesafioValidacao obterPorId(UUID id) {
        DesafioValidacaoRepository repo = repository();
        if (repo != null) {
            return repo.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, DESAFIO_NAO_ENCONTRADO));
        }
        DesafioValidacao desafio = memory.get(id);
        if (desafio == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, DESAFIO_NAO_ENCONTRADO);
        }
        return desafio;
    }

    public DesafioValidacaoResponseDTO buscar(UUID id) {
        return DesafioValidacaoResponseDTO.fromEntity(obterPorId(id));
    }

    public DesafioValidacaoResponseDTO atualizar(UUID id, DesafioValidacaoUpdateDTO request) {
        DesafioValidacao desafio = obterPorId(id);

        if (request.titulo() != null) {
            desafio.setTitulo(request.titulo());
        }
        if (request.descricaoProblema() != null) {
            desafio.setDescricaoProblema(request.descricaoProblema());
        }
        if (request.publicoAlvo() != null) {
            desafio.setPublicoAlvo(request.publicoAlvo());
        }
        if (request.tamanhoAmostra() != null) {
            desafio.setTamanhoAmostra(request.tamanhoAmostra());
        }
        if (request.modalidade() != null) {
            desafio.setModalidade(request.modalidade());
        }
        if (request.duracaoMaxima() != null) {
            desafio.setDuracaoMaxima(request.duracaoMaxima());
        }
        if (request.status() != null) {
            desafio.setStatus(request.status());
        }

        DesafioValidacaoRepository repo = repository();
        if (repo != null) {
            desafio = repo.save(desafio);
        }
        return DesafioValidacaoResponseDTO.fromEntity(desafio);
    }

    public DesafioValidacaoResponseDTO atualizar(UUID id, DesafioValidacaoRequestDTO request) {
        return atualizar(id, new DesafioValidacaoUpdateDTO(
                request.titulo(),
                request.descricaoProblema(),
                request.publicoAlvo(),
                request.tamanhoAmostra(),
                request.modalidade(),
                request.duracaoMaxima(),
                null
        ));
    }

    public DesafioValidacaoResponseDTO inativar(UUID id) {
        DesafioValidacao desafio = obterPorId(id);
        desafio.setAtivo(false);

        DesafioValidacaoRepository repo = repository();
        if (repo != null) {
            desafio = repo.save(desafio);
        }
        return DesafioValidacaoResponseDTO.fromEntity(desafio);
    }

    public List<DesafioValidacaoResponseDTO> listarPorAmbiente(UUID ambienteValidacaoId) {
        if (ambienteValidacaoId == null) {
            return List.of();
        }

        DesafioValidacaoRepository repo = repository();
        List<DesafioValidacao> lista;
        if (repo != null) {
            lista = repo.findByAmbienteValidacaoId(ambienteValidacaoId);
        } else {
            lista = memory.values().stream()
                    .filter(d -> d.getAmbienteValidacao() != null
                            && ambienteValidacaoId.equals(d.getAmbienteValidacao().getId()))
                    .toList();
        }
        return lista.stream().map(DesafioValidacaoResponseDTO::fromEntity).toList();
    }

    public List<DesafioAnonimoResponseDTO> listarAtivosAnonimos() {
        DesafioValidacaoRepository repo = repository();
        List<DesafioValidacao> lista;
        if (repo != null) {
            lista = repo.findByAtivoTrue();
        } else {
            lista = memory.values().stream()
                    .filter(DesafioValidacao::isAtivo)
                    .toList();
        }
        return lista.stream().map(DesafioAnonimoResponseDTO::fromEntity).toList();
    }
}
