package br.senac.fullstack.service;

import br.senac.fullstack.dto.DesafioAnonimoResponseDTO;
import br.senac.fullstack.dto.DesafioRecomendadoDTO;
import br.senac.fullstack.dto.RankedDesafioAiResult;
import br.senac.fullstack.exception.AiMatchmakingException;
import br.senac.fullstack.model.DesafioValidacao;
import br.senac.fullstack.model.ModalidadeTeste;
import br.senac.fullstack.model.Startup;
import br.senac.fullstack.model.StatusDesafio;
import br.senac.fullstack.repository.DesafioValidacaoRepository;
import br.senac.fullstack.repository.StartupRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MatchmakingService {

    private final ObjectProvider<StartupService> startupServiceProvider;
    private final ObjectProvider<DesafioValidacaoService> desafioServiceProvider;
    private final ObjectProvider<StartupRepository> startupRepositoryProvider;
    private final ObjectProvider<DesafioValidacaoRepository> desafioRepositoryProvider;
    private final ObjectProvider<MatchmakingAiClient> aiClientProvider;

    public MatchmakingService(
            ObjectProvider<StartupService> startupServiceProvider,
            ObjectProvider<DesafioValidacaoService> desafioServiceProvider,
            ObjectProvider<StartupRepository> startupRepositoryProvider,
            ObjectProvider<DesafioValidacaoRepository> desafioRepositoryProvider,
            ObjectProvider<MatchmakingAiClient> aiClientProvider) {
        this.startupServiceProvider = startupServiceProvider;
        this.desafioServiceProvider = desafioServiceProvider;
        this.startupRepositoryProvider = startupRepositoryProvider;
        this.desafioRepositoryProvider = desafioRepositoryProvider;
        this.aiClientProvider = aiClientProvider;
    }

    public boolean atendeCriteriosObjetivos(Startup startup, DesafioValidacao desafio) {
        if (startup == null || desafio == null) {
            return false;
        }

        // 1. Restrição Rígida: Status Ativo do Desafio e da Organização
        if (!desafio.isAtivo()) {
            return false;
        }
        if (desafio.getStatus() != null && desafio.getStatus() != StatusDesafio.ABERTO) {
            return false;
        }
        if (desafio.getAmbienteValidacao() != null && !desafio.getAmbienteValidacao().isAtivo()) {
            return false;
        }

        // 2. Restrição Rígida: Modalidade de Execução do Piloto
        if (!compativelModalidade(startup, desafio)) {
            return false;
        }

        // 3. Restrição Rígida: Localidade Alvo (UF / Município IBGE)
        return compativelLocalidade(startup, desafio);
    }

    private boolean compativelModalidade(Startup startup, DesafioValidacao desafio) {
        ModalidadeTeste modalidadeDesafio = desafio.getModalidade();
        if (modalidadeDesafio == null) {
            return true;
        }

        List<ModalidadeTeste> modalidadesStartup = startup.getModalidadesSuportadas();
        if (modalidadesStartup == null || modalidadesStartup.isEmpty()) {
            return false;
        }

        if (modalidadeDesafio == ModalidadeTeste.REMOTO) {
            return modalidadesStartup.contains(ModalidadeTeste.REMOTO)
                    || modalidadesStartup.contains(ModalidadeTeste.HIBRIDO);
        }
        if (modalidadeDesafio == ModalidadeTeste.PRESENCIAL) {
            return modalidadesStartup.contains(ModalidadeTeste.PRESENCIAL)
                    || modalidadesStartup.contains(ModalidadeTeste.HIBRIDO);
        }
        if (modalidadeDesafio == ModalidadeTeste.HIBRIDO) {
            return modalidadesStartup.contains(ModalidadeTeste.HIBRIDO)
                    || (modalidadesStartup.contains(ModalidadeTeste.PRESENCIAL)
                    && modalidadesStartup.contains(ModalidadeTeste.REMOTO));
        }
        return false;
    }

    private boolean compativelLocalidade(Startup startup, DesafioValidacao desafio) {
        ModalidadeTeste modalidadeDesafio = desafio.getModalidade();
        // Em modalidade 100% remota, a localidade geográfica não é restritiva
        if (modalidadeDesafio == ModalidadeTeste.REMOTO) {
            return true;
        }

        String ufDesafio = desafio.getUfIbge();
        String municipioDesafio = desafio.getMunicipioIbge();

        String ufStartup = obterUfStartup(startup);
        String municipioStartup = obterMunicipioStartup(startup);

        // Se o desafio presencial ou híbrido possui UF alvo definida
        if (ufDesafio != null && !ufDesafio.isBlank()) {
            if (ufStartup == null || !ufDesafio.trim().equalsIgnoreCase(ufStartup.trim())) {
                return false;
            }
        }

        // Se o desafio presencial possui município definido
        if (modalidadeDesafio == ModalidadeTeste.PRESENCIAL
                && municipioDesafio != null && !municipioDesafio.isBlank()) {
            if (municipioStartup != null && !municipioStartup.isBlank()) {
                if (!municipioDesafio.trim().equalsIgnoreCase(municipioStartup.trim())) {
                    return false;
                }
            }
        }

        return true;
    }

    private String obterUfStartup(Startup startup) {
        if (startup.getUfIbge() != null && !startup.getUfIbge().isBlank()) {
            return startup.getUfIbge();
        }
        return startup.getLocalidadeAtuacao();
    }

    private String obterMunicipioStartup(Startup startup) {
        if (startup.getMunicipioIbge() != null && !startup.getMunicipioIbge().isBlank()) {
            return startup.getMunicipioIbge();
        }
        return startup.getLocalidadeSede();
    }

    public List<DesafioValidacao> filtrar(Startup startup, List<DesafioValidacao> desafios) {
        if (startup == null || desafios == null) {
            return List.of();
        }
        return desafios.stream()
                .filter(d -> atendeCriteriosObjetivos(startup, d))
                .toList();
    }

    public List<DesafioAnonimoResponseDTO> filtrarDesafiosAnonimos(Startup startup, List<DesafioValidacao> desafios) {
        return filtrar(startup, desafios).stream()
                .map(DesafioAnonimoResponseDTO::fromEntity)
                .toList();
    }

    public List<DesafioRecomendadoDTO> rankearPorRegras(Startup startup, List<DesafioValidacao> desafios) {
        if (desafios == null || desafios.isEmpty()) {
            return Collections.emptyList();
        }

        String areaStartup = startup.getAreaAtuacao() != null ? startup.getAreaAtuacao().toLowerCase() : "";

        List<DesafioRecomendadoDTO> recomendados = new ArrayList<>();
        for (DesafioValidacao d : desafios) {
            String setor = d.getAmbienteValidacao() != null ? d.getAmbienteValidacao().getSetor() : "Geral";
            String setorLower = setor.toLowerCase();

            String motivo;
            if (!areaStartup.isBlank() && setorLower.contains(areaStartup)) {
                motivo = "Recomendado por compatibilidade direta no setor " + setor + " e sinergia de atuacao da startup.";
            } else if (!areaStartup.isBlank() && areaStartup.contains(setorLower)) {
                motivo = "Recomendado por alinhamento tematico na area de " + startup.getAreaAtuacao()
                        + " e aderencia a modalidade " + d.getModalidade() + ".";
            } else {
                motivo = "Recomendado por compatibilidade de perfil operacional, setor e modalidade disponivel para validacao.";
            }

            recomendados.add(DesafioRecomendadoDTO.de(d, motivo, "REGRAS"));
        }

        recomendados.sort((a, b) -> {
            int scoreA = calcularScoreRegras(areaStartup, a.setor());
            int scoreB = calcularScoreRegras(areaStartup, b.setor());
            return Integer.compare(scoreB, scoreA);
        });

        return recomendados;
    }

    private int calcularScoreRegras(String areaStartup, String setor) {
        if (areaStartup == null || areaStartup.isBlank() || setor == null) {
            return 1;
        }
        String setorLower = setor.toLowerCase();
        if (setorLower.contains(areaStartup)) {
            return 3;
        }
        if (areaStartup.contains(setorLower)) {
            return 2;
        }
        return 1;
    }

    public List<DesafioRecomendadoDTO> recomendarDesafios(Startup startup, List<DesafioValidacao> desafios) {
        List<DesafioValidacao> preFiltrados = filtrar(startup, desafios);
        if (preFiltrados.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            MatchmakingAiClient client = aiClientProvider.getIfAvailable();
            if (client == null) {
                throw new AiMatchmakingException("Cliente de IA nao configurado no contexto");
            }
            List<RankedDesafioAiResult> aiResults = client.rankearComIa(startup, preFiltrados);
            if (aiResults == null || aiResults.isEmpty()) {
                throw new AiMatchmakingException("Resposta vazia da API de IA");
            }

            Map<UUID, DesafioValidacao> map = preFiltrados.stream()
                    .collect(Collectors.toMap(DesafioValidacao::getId, d -> d));

            List<DesafioRecomendadoDTO> recomendados = new ArrayList<>();
            for (RankedDesafioAiResult res : aiResults) {
                DesafioValidacao d = map.get(res.desafioId());
                if (d != null) {
                    recomendados.add(DesafioRecomendadoDTO.de(d, res.motivo(), "IA"));
                }
            }
            return recomendados;
        } catch (Exception ex) {
            // Contingência / Fallback por regras simples (RF11, RNF03, RNF10)
            return rankearPorRegras(startup, preFiltrados);
        }
    }

    public List<DesafioValidacao> obterDesafiosAtivos() {
        DesafioValidacaoRepository repo = desafioRepositoryProvider.getIfAvailable();
        if (repo != null) {
            return repo.findByAtivoTrue();
        }
        return Collections.emptyList();
    }

    public Startup obterStartup(UUID startupId) {
        if (startupId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID da startup e obrigatorio");
        }

        StartupRepository repo = startupRepositoryProvider.getIfAvailable();
        if (repo != null) {
            return repo.findById(startupId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Startup nao encontrada"));
        }

        StartupService service = startupServiceProvider.getIfAvailable();
        if (service != null) {
            var dto = service.buscar(startupId);
            Startup startup = new Startup();
            startup.setId(dto.id());
            startup.setNome(dto.nome());
            startup.setEmail(dto.email());
            startup.setAreaAtuacao(dto.areaAtuacao());
            startup.setDescricao(dto.descricao());
            startup.setOQuePrecisaValidar(dto.oQuePrecisaValidar());
            startup.setUfIbge(dto.ufIbge());
            startup.setMunicipioIbge(dto.municipioIbge());
            startup.setLocalidadeSede(dto.localidadeSede());
            startup.setLocalidadeAtuacao(dto.localidadeAtuacao());
            startup.setModalidadesSuportadas(dto.modalidadesSuportadas());
            startup.setTemasIds(dto.temasIds());
            return startup;
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Startup nao encontrada");
    }

    public Startup obterStartupPorEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email da startup e obrigatorio");
        }

        StartupRepository repo = startupRepositoryProvider.getIfAvailable();
        if (repo != null) {
            return repo.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Startup nao encontrada"));
        }

        StartupService service = startupServiceProvider.getIfAvailable();
        if (service != null) {
            var dto = service.buscarPorEmail(email);
            return obterStartup(dto.id());
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Startup nao encontrada");
    }

    public List<DesafioRecomendadoDTO> recomendarDesafiosParaStartup(UUID startupId, List<DesafioValidacao> desafios) {
        Startup startup = obterStartup(startupId);
        List<DesafioValidacao> pool = (desafios != null && !desafios.isEmpty()) ? desafios : obterDesafiosAtivos();
        return recomendarDesafios(startup, pool);
    }
}
