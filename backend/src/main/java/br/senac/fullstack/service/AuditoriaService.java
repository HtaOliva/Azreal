package br.senac.fullstack.service;

import br.senac.fullstack.dto.RegistroAuditoriaResponseDTO;
import br.senac.fullstack.model.RegistroAuditoria;
import br.senac.fullstack.model.StatusProposta;
import br.senac.fullstack.repository.RegistroAuditoriaRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servico responsavel pela gravacao e consulta da trilha imutavel de auditoria (RNF08).
 */
@Service
public class AuditoriaService {

    private final ObjectProvider<RegistroAuditoriaRepository> repositoryProvider;
    private final Map<UUID, RegistroAuditoria> memory = new ConcurrentHashMap<>();

    @Autowired
    public AuditoriaService(ObjectProvider<RegistroAuditoriaRepository> repositoryProvider) {
        this.repositoryProvider = repositoryProvider;
    }

    public AuditoriaService() {
        this(null);
    }

    public RegistroAuditoriaResponseDTO registrar(
            UUID propostaPocId,
            UUID termoPilotoId,
            StatusProposta statusAnterior,
            StatusProposta statusNovo,
            String usuarioResponsavel,
            String acao,
            String detalhes) {
        if (propostaPocId == null) {
            return null;
        }

        RegistroAuditoria registro = new RegistroAuditoria(
                propostaPocId,
                termoPilotoId,
                statusAnterior,
                statusNovo,
                usuarioResponsavel != null ? usuarioResponsavel : "SISTEMA",
                acao != null ? acao : "TRANSICAO_STATUS",
                detalhes
        );

        RegistroAuditoriaRepository repo = repositoryProvider != null ? repositoryProvider.getIfAvailable() : null;
        if (repo != null) {
            try {
                RegistroAuditoria salvo = repo.save(registro);
                memory.put(salvo.getId(), salvo);
                return RegistroAuditoriaResponseDTO.fromEntity(salvo);
            } catch (Exception ignored) {
                // fallback to memory
            }
        }

        memory.put(registro.getId(), registro);
        return RegistroAuditoriaResponseDTO.fromEntity(registro);
    }

    public List<RegistroAuditoriaResponseDTO> listarPorProposta(UUID propostaPocId) {
        if (propostaPocId == null) {
            return List.of();
        }

        RegistroAuditoriaRepository repo = repositoryProvider != null ? repositoryProvider.getIfAvailable() : null;
        if (repo != null) {
            try {
                List<RegistroAuditoria> doBanco = repo.findByPropostaPocIdOrderByDataHoraAsc(propostaPocId);
                if (!doBanco.isEmpty()) {
                    return doBanco.stream().map(RegistroAuditoriaResponseDTO::fromEntity).toList();
                }
            } catch (Exception ignored) {
                // fallback to memory
            }
        }

        return memory.values().stream()
                .filter(r -> propostaPocId.equals(r.getPropostaPocId()))
                .sorted(Comparator.comparing(RegistroAuditoria::getDataHora))
                .map(RegistroAuditoriaResponseDTO::fromEntity)
                .toList();
    }

    public List<RegistroAuditoriaResponseDTO> listarPorTermo(UUID termoPilotoId) {
        if (termoPilotoId == null) {
            return List.of();
        }

        RegistroAuditoriaRepository repo = repositoryProvider != null ? repositoryProvider.getIfAvailable() : null;
        if (repo != null) {
            try {
                List<RegistroAuditoria> doBanco = repo.findByTermoPilotoIdOrderByDataHoraAsc(termoPilotoId);
                if (!doBanco.isEmpty()) {
                    return doBanco.stream().map(RegistroAuditoriaResponseDTO::fromEntity).toList();
                }
            } catch (Exception ignored) {
                // fallback to memory
            }
        }

        return memory.values().stream()
                .filter(r -> termoPilotoId.equals(r.getTermoPilotoId()))
                .sorted(Comparator.comparing(RegistroAuditoria::getDataHora))
                .map(RegistroAuditoriaResponseDTO::fromEntity)
                .toList();
    }

    public void limparMemoria() {
        memory.clear();
    }
}
