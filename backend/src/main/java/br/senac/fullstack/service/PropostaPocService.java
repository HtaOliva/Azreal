package br.senac.fullstack.service;

import br.senac.fullstack.dto.PropostaPocRequestDTO;
import br.senac.fullstack.dto.PropostaPocResponseDTO;
import br.senac.fullstack.model.PropostaPoc;
import br.senac.fullstack.model.StatusProposta;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PropostaPocService {

    private final Map<UUID, PropostaPoc> memory = new ConcurrentHashMap<>();

    public PropostaPocResponseDTO criar(PropostaPocRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados da proposta sao obrigatorios");
        }
        if (Boolean.FALSE.equals(request.aceitePrevioTermoSigilo()) || request.aceitePrevioTermoSigilo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O aceite previo do termo de sigilo e obrigatorio");
        }
        if (request.escopoTeste() == null || request.escopoTeste().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O escopo do teste e obrigatorio");
        }
        if (request.metricaValidacao() == null || request.metricaValidacao().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A metrica de validacao e obrigatoria");
        }
        if (request.startupId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ID da startup e obrigatorio");
        }
        if (request.desafioValidacaoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ID do desafio de validacao e obrigatorio");
        }
        if (request.duracaoSugeridaDias() == null || request.duracaoSugeridaDias() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A duracao sugerida deve ser informada em dias e positiva");
        }

        long pendentes = memory.values().stream()
                .filter(p -> request.startupId().equals(p.getStartupId()) && p.getStatus() == StatusProposta.PENDENTE)
                .count();

        if (pendentes >= 3) {
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

        PropostaPoc proposta = request.toEntity();
        memory.put(proposta.getId(), proposta);
        return PropostaPocResponseDTO.fromEntity(proposta);
    }

    public PropostaPocResponseDTO buscar(UUID id) {
        return PropostaPocResponseDTO.fromEntity(obterPorId(id));
    }

    public PropostaPoc obterPorId(UUID id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID da proposta e obrigatorio");
        }
        PropostaPoc proposta = memory.get(id);
        if (proposta == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposta de POC nao encontrada");
        }
        return proposta;
    }

    public List<PropostaPocResponseDTO> listarPorStartup(UUID startupId) {
        if (startupId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID da startup e obrigatorio");
        }
        return memory.values().stream()
                .filter(p -> startupId.equals(p.getStartupId()))
                .map(PropostaPocResponseDTO::fromEntity)
                .toList();
    }

    public List<PropostaPocResponseDTO> listarPorDesafio(UUID desafioId) {
        if (desafioId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID do desafio e obrigatorio");
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
        return PropostaPocResponseDTO.fromEntity(proposta);
    }
}
