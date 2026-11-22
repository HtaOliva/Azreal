package br.senac.fullstack.service;

import br.senac.fullstack.dto.PropostaPocRequestDTO;
import br.senac.fullstack.dto.PropostaPocResponseDTO;
import br.senac.fullstack.model.PropostaPoc;
import br.senac.fullstack.model.StatusProposta;
import br.senac.fullstack.model.TermoPiloto;
import br.senac.fullstack.util.CalendarioPernambucoUtil;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final Map<UUID, PropostaPoc> memory = new ConcurrentHashMap<>();
    private final Map<UUID, TermoPiloto> termosMemory = new ConcurrentHashMap<>();
    private final ObjectProvider<TermoPilotoService> termoPilotoServiceProvider;
    private final ObjectProvider<AuditoriaService> auditoriaServiceProvider;

    @Autowired
    public PropostaPocService(
            ObjectProvider<TermoPilotoService> termoPilotoServiceProvider,
            ObjectProvider<AuditoriaService> auditoriaServiceProvider) {
        this.termoPilotoServiceProvider = termoPilotoServiceProvider;
        this.auditoriaServiceProvider = auditoriaServiceProvider;
    }

    public PropostaPocService(ObjectProvider<TermoPilotoService> termoPilotoServiceProvider) {
        this(termoPilotoServiceProvider, null);
    }

    public PropostaPocService() {
        this(null, null);
    }

    public PropostaPocResponseDTO criar(PropostaPocRequestDTO request) {
        validarCamposObrigatorios(request);
        validarRegrasNegocioCriacao(request);

        PropostaPoc proposta = request.toEntity();
        if (proposta.getPrazoRespostaAmbiente() == null && proposta.getDataCriacao() != null) {
            proposta.setPrazoRespostaAmbiente(CalendarioPernambucoUtil.calcularPrazo5DiasUteis(proposta.getDataCriacao()));
        }
        memory.put(proposta.getId(), proposta);
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

        boolean desafioDuplicado = memory.values().stream()
                .anyMatch(p -> request.startupId().equals(p.getStartupId())
                        && request.desafioValidacaoId().equals(p.getDesafioValidacaoId())
                        && (p.getStatus() == StatusProposta.PENDENTE || p.getStatus() == StatusProposta.EM_TESTE));

        if (desafioDuplicado) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Startup ja possui proposta ativa para este desafio de validacao");
        }
    }

    public List<PropostaPocResponseDTO> listarFilaAmbiente(Collection<UUID> desafioIds) {
        if (desafioIds == null || desafioIds.isEmpty()) {
            return List.of();
        }
        return memory.values().stream()
                .filter(p -> p.getStatus() == StatusProposta.PENDENTE
                        && desafioIds.contains(p.getDesafioValidacaoId()))
                .sorted(Comparator.comparing(PropostaPoc::getDataCriacao))
                .map(PropostaPocResponseDTO::fromEntity)
                .toList();
    }

    public PropostaPocResponseDTO buscar(UUID id) {
        return PropostaPocResponseDTO.fromEntity(obterPorId(id));
    }

    public PropostaPoc obterPorId(UUID id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ID_PROPOSTA_OBRIGATORIO);
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
        return memory.values().stream()
                .filter(p -> startupId.equals(p.getStartupId()))
                .map(PropostaPocResponseDTO::fromEntity)
                .toList();
    }

    public List<PropostaPocResponseDTO> listarPorDesafio(UUID desafioId) {
        if (desafioId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ID_DESAFIO_OBRIGATORIO);
        }
        return memory.values().stream()
                .filter(p -> desafioId.equals(p.getDesafioValidacaoId()))
                .map(PropostaPocResponseDTO::fromEntity)
                .toList();
    }

    public List<PropostaPocResponseDTO> listarTodas() {
        return memory.values().stream()
                .map(PropostaPocResponseDTO::fromEntity)
                .toList();
    }

    public long contarPendentesPorStartup(UUID startupId) {
        if (startupId == null) {
            return 0;
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
        PropostaPoc proposta = memory.get(propostaId);
        if (proposta != null) {
            proposta.setStatus(status);
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
