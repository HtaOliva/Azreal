package br.senac.fullstack.service;

import br.senac.fullstack.dto.CertificadoResponseDTO;
import br.senac.fullstack.model.Certificado;
import br.senac.fullstack.repository.CertificadoRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CertificadoService {

    private final ObjectProvider<CertificadoRepository> repositoryProvider;
    private final Map<UUID, Certificado> memory = new ConcurrentHashMap<>();

    public CertificadoService(ObjectProvider<CertificadoRepository> repositoryProvider) {
        this.repositoryProvider = repositoryProvider;
    }

    private CertificadoRepository repository() {
        return repositoryProvider.getIfAvailable();
    }

    public CertificadoResponseDTO registrar(UUID startupId, String nome, String tipo, Long tamanho, String arquivoUrl) {
        Certificado c = new Certificado();
        c.setStartupId(startupId);
        c.setNome(nome);
        c.setTipo(tipo);
        c.setTamanho(tamanho);
        c.setArquivoUrl(arquivoUrl);

        CertificadoRepository repo = repository();
        if (repo != null) {
            c = repo.save(c);
        } else {
            memory.put(c.getId(), c);
        }
        return toResponse(c);
    }

    public List<CertificadoResponseDTO> listarPorStartup(UUID startupId) {
        CertificadoRepository repo = repository();
        List<Certificado> items;
        if (repo != null) {
            items = repo.findByStartupId(startupId);
        } else {
            items = memory.values().stream()
                    .filter(c -> c.getStartupId().equals(startupId)).toList();
        }
        return items.stream().map(this::toResponse).toList();
    }

    private CertificadoResponseDTO toResponse(Certificado c) {
        return new CertificadoResponseDTO(c.getId(), c.getStartupId(), c.getNome(),
                c.getTipo(), c.getTamanho(), c.getArquivoUrl(), c.getDataUpload());
    }
}
