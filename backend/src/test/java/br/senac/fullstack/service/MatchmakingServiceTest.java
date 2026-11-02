package br.senac.fullstack.service;

import br.senac.fullstack.dto.DesafioAnonimoResponseDTO;
import br.senac.fullstack.model.AmbienteValidacao;
import br.senac.fullstack.model.DesafioValidacao;
import br.senac.fullstack.model.ModalidadeTeste;
import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.model.Startup;
import br.senac.fullstack.model.StatusDesafio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchmakingServiceTest {

    private MatchmakingService matchmakingService;

    @BeforeEach
    void setUp() {
        matchmakingService = new MatchmakingService(
                new NullObjectProvider<>(),
                new NullObjectProvider<>(),
                new NullObjectProvider<>(),
                new NullObjectProvider<>()
        );
    }

    private AmbienteValidacao criarAmbiente(String uf, String municipio) {
        AmbienteValidacao amb = new AmbienteValidacao();
        amb.setId(UUID.randomUUID());
        amb.setRazaoSocial("Hospital Teste S.A.");
        amb.setNomeFantasia("Hospital Teste");
        amb.setSetor("Saude");
        amb.setPorte(PorteOrganizacao.MEDIO);
        amb.setUfIbge(uf);
        amb.setMunicipioIbge(municipio);
        amb.setAtivo(true);
        return amb;
    }

    private DesafioValidacao criarDesafio(AmbienteValidacao amb, ModalidadeTeste modalidade) {
        DesafioValidacao desafio = new DesafioValidacao();
        desafio.setId(UUID.randomUUID());
        desafio.setAmbienteValidacao(amb);
        desafio.setTitulo("Piloto Teste");
        desafio.setDescricaoProblema("Problema operacional real");
        desafio.setModalidade(modalidade);
        desafio.setStatus(StatusDesafio.ABERTO);
        desafio.setAtivo(true);
        return desafio;
    }

    private Startup criarStartup(String uf, String municipio, List<ModalidadeTeste> modalidades) {
        Startup s = new Startup();
        s.setId(UUID.randomUUID());
        s.setNome("Startup AI Health");
        s.setEmail("contato@aihealth.com");
        s.setUfIbge(uf);
        s.setMunicipioIbge(municipio);
        s.setModalidadesSuportadas(modalidades);
        return s;
    }

    @Test
    void desafioInativoOuComStatusDiferenteDeAbertoDeveSerRejeitado() {
        AmbienteValidacao amb = criarAmbiente("PE", "Recife");
        Startup startup = criarStartup("PE", "Recife", List.of(ModalidadeTeste.PRESENCIAL));

        DesafioValidacao desafioInativo = criarDesafio(amb, ModalidadeTeste.PRESENCIAL);
        desafioInativo.setAtivo(false);
        assertFalse(matchmakingService.atendeCriteriosObjetivos(startup, desafioInativo));

        DesafioValidacao desafioConcluido = criarDesafio(amb, ModalidadeTeste.PRESENCIAL);
        desafioConcluido.setStatus(StatusDesafio.CONCLUIDO);
        assertFalse(matchmakingService.atendeCriteriosObjetivos(startup, desafioConcluido));

        AmbienteValidacao ambInativo = criarAmbiente("PE", "Recife");
        ambInativo.setAtivo(false);
        DesafioValidacao desafioAmbInativo = criarDesafio(ambInativo, ModalidadeTeste.PRESENCIAL);
        assertFalse(matchmakingService.atendeCriteriosObjetivos(startup, desafioAmbInativo));
    }

    @Test
    void filtroModalidadeStartupApenasRemoto() {
        AmbienteValidacao amb = criarAmbiente("PE", "Recife");
        Startup startupRemota = criarStartup("SP", "São Paulo", List.of(ModalidadeTeste.REMOTO));

        DesafioValidacao desafioRemoto = criarDesafio(amb, ModalidadeTeste.REMOTO);
        DesafioValidacao desafioPresencial = criarDesafio(amb, ModalidadeTeste.PRESENCIAL);
        DesafioValidacao desafioHibrido = criarDesafio(amb, ModalidadeTeste.HIBRIDO);

        assertTrue(matchmakingService.atendeCriteriosObjetivos(startupRemota, desafioRemoto));
        assertFalse(matchmakingService.atendeCriteriosObjetivos(startupRemota, desafioPresencial));
        assertFalse(matchmakingService.atendeCriteriosObjetivos(startupRemota, desafioHibrido));
    }

    @Test
    void filtroModalidadeStartupHibridaAtendeTodasModalidadesComLocalidadeAdequada() {
        AmbienteValidacao ambRecife = criarAmbiente("PE", "Recife");
        Startup startupHibrida = criarStartup("PE", "Recife", List.of(ModalidadeTeste.HIBRIDO));

        DesafioValidacao dRemoto = criarDesafio(ambRecife, ModalidadeTeste.REMOTO);
        DesafioValidacao dPresencial = criarDesafio(ambRecife, ModalidadeTeste.PRESENCIAL);
        DesafioValidacao dHibrido = criarDesafio(ambRecife, ModalidadeTeste.HIBRIDO);

        assertTrue(matchmakingService.atendeCriteriosObjetivos(startupHibrida, dRemoto));
        assertTrue(matchmakingService.atendeCriteriosObjetivos(startupHibrida, dPresencial));
        assertTrue(matchmakingService.atendeCriteriosObjetivos(startupHibrida, dHibrido));
    }

    @Test
    void filtroLocalidadeDesafioRemotoNaoRestringePorUFouMunicipio() {
        AmbienteValidacao ambRecife = criarAmbiente("PE", "Recife");
        Startup startupManaus = criarStartup("AM", "Manaus", List.of(ModalidadeTeste.REMOTO));

        DesafioValidacao dRemoto = criarDesafio(ambRecife, ModalidadeTeste.REMOTO);
        assertTrue(matchmakingService.atendeCriteriosObjetivos(startupManaus, dRemoto));
    }

    @Test
    void filtroLocalidadeDesafioPresencialExigeMesmaUFeMunicipio() {
        AmbienteValidacao ambRecife = criarAmbiente("PE", "Recife");
        DesafioValidacao dPresencial = criarDesafio(ambRecife, ModalidadeTeste.PRESENCIAL);

        Startup startupMesmaCidade = criarStartup("PE", "Recife", List.of(ModalidadeTeste.PRESENCIAL));
        Startup startupOutroMunicipio = criarStartup("PE", "Caruaru", List.of(ModalidadeTeste.PRESENCIAL));
        Startup startupOutroEstado = criarStartup("PB", "João Pessoa", List.of(ModalidadeTeste.PRESENCIAL));

        assertTrue(matchmakingService.atendeCriteriosObjetivos(startupMesmaCidade, dPresencial));
        assertFalse(matchmakingService.atendeCriteriosObjetivos(startupOutroMunicipio, dPresencial));
        assertFalse(matchmakingService.atendeCriteriosObjetivos(startupOutroEstado, dPresencial));
    }

    @Test
    void filtroLocalidadeDesafioHibridoExigeMesmoEstado() {
        AmbienteValidacao ambRecife = criarAmbiente("PE", "Recife");
        DesafioValidacao dHibrido = criarDesafio(ambRecife, ModalidadeTeste.HIBRIDO);

        Startup startupPE = criarStartup("PE", "Olinda", List.of(ModalidadeTeste.HIBRIDO));
        Startup startupSP = criarStartup("SP", "Campinas", List.of(ModalidadeTeste.HIBRIDO));

        assertTrue(matchmakingService.atendeCriteriosObjetivos(startupPE, dHibrido));
        assertFalse(matchmakingService.atendeCriteriosObjetivos(startupSP, dHibrido));
    }

    @Test
    void filtrarColecaoEDeferirDTOAnonimo() {
        AmbienteValidacao ambPE = criarAmbiente("PE", "Recife");
        AmbienteValidacao ambSP = criarAmbiente("SP", "São Paulo");

        DesafioValidacao d1 = criarDesafio(ambPE, ModalidadeTeste.PRESENCIAL);
        DesafioValidacao d2 = criarDesafio(ambSP, ModalidadeTeste.PRESENCIAL);
        DesafioValidacao d3 = criarDesafio(ambSP, ModalidadeTeste.REMOTO);

        Startup startup = criarStartup("PE", "Recife", List.of(ModalidadeTeste.PRESENCIAL, ModalidadeTeste.REMOTO));

        List<DesafioValidacao> compativeis = matchmakingService.filtrar(startup, List.of(d1, d2, d3));
        assertEquals(2, compativeis.size());
        assertTrue(compativeis.contains(d1));
        assertTrue(compativeis.contains(d3));
        assertFalse(compativeis.contains(d2));

        List<DesafioAnonimoResponseDTO> anonimos = matchmakingService.filtrarDesafiosAnonimos(startup, List.of(d1, d2, d3));
        assertEquals(2, anonimos.size());
        assertEquals("Piloto Teste", anonimos.get(0).titulo());
    }

    @Test
    void entradasNulasTratadasComSeguranca() {
        assertFalse(matchmakingService.atendeCriteriosObjetivos(null, null));
        assertFalse(matchmakingService.atendeCriteriosObjetivos(new Startup(), null));
        assertFalse(matchmakingService.atendeCriteriosObjetivos(null, new DesafioValidacao()));
        assertTrue(matchmakingService.filtrar(null, null).isEmpty());
        assertThrows(ResponseStatusException.class, () -> matchmakingService.obterStartup(null));
    }
}
