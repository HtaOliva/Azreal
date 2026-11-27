package br.senac.fullstack.service;

import br.senac.fullstack.dto.PropostaPocRequestDTO;
import br.senac.fullstack.dto.PropostaPocResponseDTO;
import br.senac.fullstack.model.PropostaPoc;
import br.senac.fullstack.model.StatusProposta;
import br.senac.fullstack.model.TermoPiloto;
import br.senac.fullstack.repository.PropostaPocRepository;
import br.senac.fullstack.util.CalendarioPernambucoUtil;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PropostaPocService {

    private static final String ID_STARTUP_OBRIGATORIO = "ID da startup e obrigatorio";
    private static final String ID_DESAFIO_OBRIGATORIO = "ID do desafio e obrigatorio";
    private static final String ID_PROPOSTA_OBRIGATORIO = "ID da proposta e obrigatorio";
    private static final String PROPOSTA_NAO_ENCONTRADA = "Proposta de POC nao encontrada";

    private final ObjectProvider<PropostaPocRepository> repositoryProvider;
    private final ObjectProvider<TermoPilotoService> termoPilotoServiceProvider;
    private final ObjectProvider<AuditoriaService> auditoriaServiceProvider;
    private final Map<UUID, PropostaPoc> memory = new ConcurrentHashMap<>();
    private final Map<UUID, TermoPiloto> termosMemory = new ConcurrentHashMap<>();

    @Autowired
    public PropostaPocService(
            ObjectProvider<PropostaPocRepository> repositoryProvider,
            ObjectProvider<TermoPilotoService> termoPilotoServiceProvider,
            ObjectProvider<AuditoriaService> auditoriaServiceProvider) {
        this.repositoryProvider = repositoryProvider;
        this.termoPilotoServiceProvider = termoPilotoServiceProvider;
        this.auditoriaServiceProvider = auditoriaServiceProvider;
    }

    public PropostaPocService(
            ObjectProvider<TermoPilotoService> termoPilotoServiceProvider,
            ObjectProvider<AuditoriaService> auditoriaServiceProvider) {
        this(null, termoPilotoServiceProvider, auditoriaServiceProvider);
    }

    public PropostaPocService(ObjectProvider<TermoPilotoService> termoPilotoServiceProvider) {
        this(null, termoPilotoServiceProvider, null);
    }

    public PropostaPocService() {
        this((ObjectProvider<PropostaPocRepository>) null, null, null);
    }

    private PropostaPocRepository repository() {
        return repositoryProvider != null ? repositoryProvider.getIfAvailable() : null;
    }

    private PropostaPoc salvar(PropostaPoc proposta) {
        PropostaPocRepository repo = repository();
        if (repo != null) {
            try {
                PropostaPoc saved = repo.save(proposta);
                memory.put(saved.getId(), saved);
                return saved;
            } catch (Exception ignored) {
                // fallback para memoria
            }
        }
        memory.put(proposta.getId(), proposta);
        return proposta;
    }

    public PropostaPocResponseDTO criar(PropostaPocRequestDTO request) {
        validarCamposObrigatorios(request);
        validarRegrasNegocioCriacao(request);

        PropostaPoc proposta = request.toEntity();
        if (proposta.getPrazoRespostaAmbiente() == null && proposta.getDataCriacao() != null) {
            proposta.setPrazoRespostaAmbiente(CalendarioPernambucoUtil.calcularPrazo5DiasUteis(proposta.getDataCriacao()));
        }
        proposta = salvar(proposta);
        gravarAuditoria(proposta.getId(), null, null, StatusProposta.PENDENTE,
                "STARTUP", "CRIACAO_PROPOSTA", "Criacao da proposta de POC");
        return PropostaPocResponseDTO.fromEntity(proposta);
    }

    private void validarCamposObrigatorios(PropostaPocRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados da proposta sao obrigatorios");
        }
        if (!Boolean.TRUE.equals(request.aceitePrevioTermoSigilo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O aceite previo do termo de sigilo e obrigatorio");
        }
        if (request.escopoTeste() == null || request.escopoTeste().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O escopo do teste e obrigatorio");
        }
        if (request.metricaValidacao() == null || request.metricaValidacao().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A metrica de validacao e obrigatoria");
        }
        if (request.startupId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ID_STARTUP_OBRIGATORIO);
        }
        if (request.desafioValidacaoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ID_DESAFIO_OBRIGATORIO);
        }
        if (request.duracaoSugeridaDias() == null || request.duracaoSugeridaDias() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A duracao sugerida deve ser informada em dias e positiva");
        }
    }

    private void validarRegrasNegocioCriacao(PropostaPocRequestDTO request) {
        if (contarPendentesPorStartup(request.startupId()) >= 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Startup atingiu o limite maximo de 3 propostas pendentes simultaneas");
        }

        PropostaPocRepository repo = repository();
        boolean desafioDuplicado;
        if (repo != null) {
            desafioDuplicado = repo.existsByStartupIdAndDesafioValidacaoIdAndStatusIn(
                    request.startupId(),
                    request.desafioValidacaoId(),
                    List.of(StatusProposta.PENDENTE, StatusProposta.EM_TESTE));
        } else {
            desafioDuplicado = memory.values().stream()
                    .anyMatch(p -> request.startupId().equals(p.getStartupId())
                            && request.desafioValidacaoId().equals(p.getDesafioValidacaoId())
                            && (p.getStatus() == StatusProposta.PENDENTE || p.getStatus() == StatusProposta.EM_TESTE));
        }

        if (desafioDuplicado) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Startup ja possui proposta ativa para este desafio de validacao");
        }
    }

    public List<PropostaPocResponseDTO> listarFilaAmbiente(Collection<UUID> desafioIds) {
        if (desafioIds == null || desafioIds.isEmpty()) {
            return List.of();
        }
        PropostaPocRepository repo = repository();
        if (repo != null) {
            return repo.findByStatusAndDesafioValidacaoIdIn(StatusProposta.PENDENTE, desafioIds)
                    .stream()
                    .sorted(Comparator.comparing(PropostaPoc::getDataCriacao))
                    .map(PropostaPocResponseDTO::fromEntity)
                    .toList();
        }
        return memory.values().stream()
                .filter(p -> p.getStatus() == StatusProposta.PENDENTE
                        && desafioIds.contains(p.getDesafioValidacaoId()))
                .sorted(Comparator.comparing(PropostaPoc::getDataCriacao))
                .map(PropostaPocResponseDTO::fromEntity)
                .toList();
    }

    public Page<PropostaPocResponseDTO> listarFilaAmbientePaginado(
            Collection<UUID> desafioIds,
            StatusProposta status,
            Pageable pageable) {
        Pageable effectivePageable = pageable != null ? pageable : PageRequest.of(0, 10);
        if (desafioIds == null || desafioIds.isEmpty()) {
            return new PageImpl<>(List.of(), effectivePageable, 0);
        }
        StatusProposta filtroStatus = status != null ? status : StatusProposta.PENDENTE;
        PropostaPocRepository repo = repository();
        if (repo != null) {
            return repo.findByStatusAndDesafioValidacaoIdIn(filtroStatus, desafioIds, effectivePageable)
                    .map(PropostaPocResponseDTO::fromEntity);
        }
        List<PropostaPoc> filtradas = memory.values().stream()
                .filter(p -> p.getStatus() == filtroStatus && desafioIds.contains(p.getDesafioValidacaoId()))
                .sorted(Comparator.comparing(PropostaPoc::getDataCriacao))
                .toList();

        int start = (int) effectivePageable.getOffset();
        int end = Math.min(start + effectivePageable.getPageSize(), filtradas.size());
        List<PropostaPoc> sublist = start <= end ? filtradas.subList(start, end) : List.of();
        return new PageImpl<>(sublist.stream().map(PropostaPocResponseDTO::fromEntity).toList(),
                effectivePageable, filtradas.size());
    }

    public PropostaPocResponseDTO buscar(UUID id) {
        return PropostaPocResponseDTO.fromEntity(obterPorId(id));
    }

    public PropostaPoc obterPorId(UUID id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ID_PROPOSTA_OBRIGATORIO);
        }
        PropostaPocRepository repo = repository();
        if (repo != null) {
            return repo.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, PROPOSTA_NAO_ENCONTRADA));
        }
        PropostaPoc proposta = memory.get(id);
        if (proposta == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, PROPOSTA_NAO_ENCONTRADA);
        }
        return proposta;
    }

    public List<PropostaPocResponseDTO> listarPorStartup(UUID startupId) {
        if (startupId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ID_STARTUP_OBRIGATORIO);
        }
        PropostaPocRepository repo = repository();
        if (repo != null) {
            return repo.findByStartupId(startupId).stream()
                    .map(PropostaPocResponseDTO::fromEntity)
                    .toList();
        }
        return memory.values().stream()
                .filter(p -> startupId.equals(p.getStartupId()))
                .map(PropostaPocResponseDTO::fromEntity)
                .toList();
    }

    public Page<PropostaPocResponseDTO> listarPorStartupPaginado(
            UUID startupId,
            StatusProposta status,
            Pageable pageable) {
        if (startupId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ID_STARTUP_OBRIGATORIO);
        }
        Pageable effectivePageable = pageable != null ? pageable : PageRequest.of(0, 10);
        PropostaPocRepository repo = repository();
        if (repo != null) {
            Page<PropostaPoc> page = (status != null)
                    ? repo.findByStartupIdAndStatus(startupId, status, effectivePageable)
                    : repo.findByStartupId(startupId, effectivePageable);
            return page.map(PropostaPocResponseDTO::fromEntity);
        }
        List<PropostaPoc> filtradas = memory.values().stream()
                .filter(p -> startupId.equals(p.getStartupId()))
                .filter(p -> status == null || status.equals(p.getStatus()))
                .sorted(Comparator.comparing(PropostaPoc::getDataCriacao).reversed())
                .toList();

        int start = (int) effectivePageable.getOffset();
        int end = Math.min(start + effectivePageable.getPageSize(), filtradas.size());
        List<PropostaPoc> sublist = start <= end ? filtradas.subList(start, end) : List.of();
        return new PageImpl<>(sublist.stream().map(PropostaPocResponseDTO::fromEntity).toList(),
                effectivePageable, filtradas.size());
    }

    public List<PropostaPocResponseDTO> listarPorDesafio(UUID desafioId) {
        if (desafioId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ID_DESAFIO_OBRIGATORIO);
        }
        PropostaPocRepository repo = repository();
        if (repo != null) {
            return repo.findByDesafioValidacaoId(desafioId).stream()
                    .map(PropostaPocResponseDTO::fromEntity)
                    .toList();
        }
        return memory.values().stream()
                .filter(p -> desafioId.equals(p.getDesafioValidacaoId()))
                .map(PropostaPocResponseDTO::fromEntity)
                .toList();
    }

    public List<PropostaPocResponseDTO> listarTodas() {
        PropostaPocRepository repo = repository();
        if (repo != null) {
            return repo.findAll().stream()
                    .map(PropostaPocResponseDTO::fromEntity)
                    .toList();
        }
        return memory.values().stream()
                .map(PropostaPocResponseDTO::fromEntity)
                .toList();
    }

    public long contarPendentesPorStartup(UUID startupId) {
        if (startupId == null) {
            return 0;
        }
        PropostaPocRepository repo = repository();
        if (repo != null) {
            return repo.countByStartupIdAndStatus(startupId, StatusProposta.PENDENTE);
        }
        return memory.values().stream()
                .filter(p -> startupId.equals(p.getStartupId()) && p.getStatus() == StatusProposta.PENDENTE)
                .count();
    }

    public PropostaPocResponseDTO cancelar(UUID id) {
        PropostaPoc proposta = obterPorId(id);
        if (proposta.getStatus() != StatusProposta.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Apenas propostas pendentes podem ser canceladas");
        }
        proposta.setStatus(StatusProposta.CANCELADA);
        salvar(proposta);
        gravarAuditoria(proposta.getId(), null, StatusProposta.PENDENTE, StatusProposta.CANCELADA,
                "STARTUP", "CANCELAMENTO_PROPOSTA", "Proposta cancelada pela startup");
        return PropostaPocResponseDTO.fromEntity(proposta);
    }

    public PropostaPocResponseDTO recusar(UUID id, String motivoRecusa) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ID_PROPOSTA_OBRIGATORIO);
        }
        if (motivoRecusa == null || motivoRecusa.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O motivo da recusa e obrigatorio");
        }

        PropostaPoc proposta = obterPorId(id);
        if (proposta.getStatus() != StatusProposta.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Apenas propostas pendentes podem ser recusadas");
        }

        proposta.setStatus(StatusProposta.RECUSADA);
        proposta.setMotivoRecusa(motivoRecusa.trim());
        salvar(proposta);
        gravarAuditoria(proposta.getId(), null, StatusProposta.PENDENTE, StatusProposta.RECUSADA,
                "AMBIENTE_VALIDACAO", "RECUSA_PROPOSTA", "Proposta recusada pelo parceiro: " + motivoRecusa.trim());
        return PropostaPocResponseDTO.fromEntity(proposta);
    }

    public TermoPiloto aceitar(UUID id, UUID ambienteValidacaoId) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ID_PROPOSTA_OBRIGATORIO);
        }
        if (ambienteValidacaoId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID do ambiente de validacao e obrigatorio");
        }

        PropostaPoc proposta = obterPorId(id);
        if (proposta.getStatus() != StatusProposta.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Apenas propostas pendentes podem ser aceitas");
        }

        proposta.setStatus(StatusProposta.EM_TESTE);
        salvar(proposta);
        TermoPiloto termo = TermoPiloto.congelar(proposta, ambienteValidacaoId);

        TermoPilotoService termoService = termoPilotoServiceProvider != null
                ? termoPilotoServiceProvider.getIfAvailable() : null;
        if (termoService != null) {
            termoService.salvar(termo);
        } else {
            termosMemory.put(proposta.getId(), termo);
        }
        gravarAuditoria(proposta.getId(), termo.getId(), StatusProposta.PENDENTE, StatusProposta.EM_TESTE,
                "AMBIENTE_VALIDACAO", "ACEITE_TERMO_PILOTO", "Proposta aceita e termo de piloto congelado");
        return termo;
    }

    public TermoPiloto obterTermoPorPropostaId(UUID propostaId) {
        if (propostaId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ID_PROPOSTA_OBRIGATORIO);
        }
        TermoPilotoService termoService = termoPilotoServiceProvider != null
                ? termoPilotoServiceProvider.getIfAvailable() : null;
        if (termoService != null) {
            return termoService.buscarPorPropostaId(propostaId);
        }
        TermoPiloto termo = termosMemory.get(propostaId);
        if (termo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Termo de piloto nao encontrado");
        }
        return termo;
    }

    public void atualizarStatus(UUID propostaId, StatusProposta status) {
        if (propostaId == null || status == null) {
            return;
        }
        PropostaPocRepository repo = repository();
        if (repo != null) {
            repo.findById(propostaId).ifPresent(p -> {
                p.setStatus(status);
                repo.save(p);
                memory.put(propostaId, p);
            });
        } else {
            PropostaPoc proposta = memory.get(propostaId);
            if (proposta != null) {
                proposta.setStatus(status);
            }
        }
    }

    private void gravarAuditoria(
            UUID propostaId,
            UUID termoId,
            StatusProposta statusAnterior,
            StatusProposta statusNovo,
            String usuario,
            String acao,
            String detalhes) {
        AuditoriaService auditoriaService = auditoriaServiceProvider != null
                ? auditoriaServiceProvider.getIfAvailable() : null;
        if (auditoriaService != null) {
            try {
                auditoriaService.registrar(propostaId, termoId, statusAnterior, statusNovo, usuario, acao, detalhes);
            } catch (Exception ignored) {
                // segue sem erro se falhar auditoria
            }
        }
    }
}
