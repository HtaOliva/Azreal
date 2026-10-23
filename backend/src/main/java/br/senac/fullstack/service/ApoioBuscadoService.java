package br.senac.fullstack.service;

import br.senac.fullstack.dto.ApoioBuscadoRequestDTO;
import br.senac.fullstack.dto.ApoioBuscadoResponseDTO;
import br.senac.fullstack.model.ApoioBuscado;
import br.senac.fullstack.repository.ApoioBuscadoRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ApoioBuscadoService {

    private final ObjectProvider<ApoioBuscadoRepository> repositoryProvider;
    private final Map<UUID, ApoioBuscado> memory = new ConcurrentHashMap<>();

    public ApoioBuscadoService(ObjectProvider<ApoioBuscadoRepository> repositoryProvider) {
        this.repositoryProvider = repositoryProvider;
    }

    private ApoioBuscadoRepository repository() {
        return repositoryProvider.getIfAvailable();
    }

    public ApoioBuscadoResponseDTO registrar(ApoioBuscadoRequestDTO request) {
        ApoioBuscado a = new ApoioBuscado();
        a.setStartupId(request.startupId());
        a.setTipoApoio(request.tipoApoio());

        ApoioBuscadoRepository repo = repository();
        if (repo != null) {
            a = repo.save(a);
        } else {
            memory.put(a.getId(), a);
        }
        return new ApoioBuscadoResponseDTO(a.getId(), a.getStartupId(), a.getTipoApoio(), a.getDataRegistro());
    }

    public List<ApoioBuscadoResponseDTO> listarPorStartup(UUID startupId) {
        ApoioBuscadoRepository repo = repository();
        List<ApoioBuscado> items;
        if (repo != null) {
            items = repo.findByStartupId(startupId);
        } else {
            items = memory.values().stream()
                    .filter(a -> a.getStartupId().equals(startupId)).collect(Collectors.toList());
        }
        return items.stream()
                .map(a -> new ApoioBuscadoResponseDTO(a.getId(), a.getStartupId(), a.getTipoApoio(), a.getDataRegistro()))
                .collect(Collectors.toList());
    }
}
