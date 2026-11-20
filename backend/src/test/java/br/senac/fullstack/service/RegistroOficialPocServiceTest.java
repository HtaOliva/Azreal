package br.senac.fullstack.service;

import br.senac.fullstack.dto.AmbienteValidacaoResponseDTO;
import br.senac.fullstack.dto.RegistroOficialPocResponseDTO;
import br.senac.fullstack.model.AvaliacaoMetrica;
import br.senac.fullstack.model.ModoPrivacidadePoc;
import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.model.RegistroOficialPoc;
import br.senac.fullstack.model.StatusProposta;
import br.senac.fullstack.model.TermoPiloto;
import br.senac.fullstack.repository.RegistroOficialPocRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegistroOficialPocServiceTest {

    private RegistroOficialPocRepository repository;
    private ObjectProvider<RegistroOficialPocRepository> repositoryProvider;
    private AmbienteValidacaoService ambienteService;
    private ObjectProvider<AmbienteValidacaoService> ambienteServiceProvider;
    private RegistroOficialPocService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        repository = mock(RegistroOficialPocRepository.class);
        repositoryProvider = mock(ObjectProvider.class);
        ambienteService = mock(AmbienteValidacaoService.class);
        ambienteServiceProvider = mock(ObjectProvider.class);

        when(repositoryProvider.getIfAvailable()).thenReturn(repository);
        when(ambienteServiceProvider.getIfAvailable()).thenReturn(ambienteService);

        service = new RegistroOficialPocService(repositoryProvider, ambienteServiceProvider);
    }

    @Test
    @DisplayName("RF20 - Emitir Registro Oficial de POC com sucesso e enriquecer dados do ambiente")
    void deveEmitirRegistroComSucesso() {
        UUID termoId = UUID.randomUUID();
        UUID startupId = UUID.randomUUID();
        UUID ambienteId = UUID.randomUUID();

        TermoPiloto termo = new TermoPiloto();
        termo.setId(termoId);
        termo.setPropostaPocId(UUID.randomUUID());
        termo.setStartupId(startupId);
        termo.setAmbienteValidacaoId(ambienteId);
        termo.setEscopoTeste("Modulo de telemetria");
        termo.setDuracaoDias(30);
        termo.setMetricaValidacao("99.9% uptime");
        termo.setStatus(StatusProposta.EM_TESTE);

        termo.confirmarExecucao(
                true,
                AvaliacaoMetrica.ATENDEU_TOTALMENTE,
                "Excelente resultado",
                ModoPrivacidadePoc.EXIBICAO_NOMINAL,
                "Interesse comercial",
                null
        );

        AmbienteValidacaoResponseDTO ambienteDTO = new AmbienteValidacaoResponseDTO(
                ambienteId, "Rede Dor S.A.", "Hospital Esperanca", "Saude", PorteOrganizacao.GRANDE,
                "2611606", "Recife", "contato@rededor.com", "81999998888", null, true, LocalDateTime.now()
        );
        when(ambienteService.buscar(ambienteId)).thenReturn(ambienteDTO);
        when(repository.save(any(RegistroOficialPoc.class))).thenAnswer(i -> i.getArgument(0));

        RegistroOficialPocResponseDTO emitido = service.emitir(termo);

        assertNotNull(emitido);
        assertTrue(emitido.codigoVerificador().startsWith("POC-"));
        assertEquals("Modulo de telemetria", emitido.escopoTestado());
        assertEquals(30, emitido.duracaoDias());
        assertEquals(AvaliacaoMetrica.ATENDEU_TOTALMENTE, emitido.avaliacaoMetrica());
        assertEquals("Rede Dor S.A.", emitido.nomeOrganizacao());
        assertEquals("Saude", emitido.setorOrganizacao());
        assertEquals(PorteOrganizacao.GRANDE, emitido.porteOrganizacao());
        assertTrue(termo.getCertificadoEmitido());
    }

    @Test
    @DisplayName("RF20 - Emitir registro com termo duplicado retorna registro ja existente")
    void emitirRegistroDuplicadoRetornaExistente() {
        UUID termoId = UUID.randomUUID();
        TermoPiloto termo = new TermoPiloto();
        termo.setId(termoId);
        termo.setStatus(StatusProposta.POC_CONCLUIDA);
        termo.setAvaliacaoMetrica(AvaliacaoMetrica.ATENDEU_TOTALMENTE);
        termo.setParecer("Parecer");

        RegistroOficialPoc existente = new RegistroOficialPoc();
        existente.setTermoPilotoId(termoId);
        existente.setCodigoVerificador("POC-2026-JAEXISTE");
        when(repository.findByTermoPilotoId(termoId)).thenReturn(Optional.of(existente));

        RegistroOficialPocResponseDTO dto = service.emitir(termo);

        assertEquals("POC-2026-JAEXISTE", dto.codigoVerificador());
    }

    @Test
    @DisplayName("RF20 - Validacoes ao tentar emitir com termo nulo ou status diferente de POC_CONCLUIDA")
    void emitirComStatusInvalidoDeveFalhar() {
        assertThrows(ResponseStatusException.class, () -> service.emitir(null));

        TermoPiloto termoEmTeste = new TermoPiloto();
        termoEmTeste.setStatus(StatusProposta.EM_TESTE);
        assertThrows(ResponseStatusException.class, () -> service.emitir(termoEmTeste));

        TermoPiloto termoNaoConfirmado = new TermoPiloto();
        termoNaoConfirmado.setStatus(StatusProposta.POC_NAO_CONFIRMADA);
        assertThrows(ResponseStatusException.class, () -> service.emitir(termoNaoConfirmado));
    }

    @Test
    @DisplayName("RF20 - Listar registros do portfolio da startup por startupId")
    void deveListarRegistrosPorStartup() {
        UUID startupId = UUID.randomUUID();
        RegistroOficialPoc r1 = new RegistroOficialPoc();
        r1.setStartupId(startupId);
        r1.setEscopoTestado("POC 1");

        when(repository.findByStartupIdOrderByDataEmissaoDesc(startupId)).thenReturn(List.of(r1));

        List<RegistroOficialPocResponseDTO> lista = service.listarPorStartup(startupId);

        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertEquals("POC 1", lista.get(0).escopoTestado());

        assertThrows(ResponseStatusException.class, () -> service.listarPorStartup(null));
    }

    @Test
    @DisplayName("RF20 - Listar em memoria quando repositorio lanca excecao")
    void listarEmMemoriaQuandoRepoFalha() {
        UUID startupId = UUID.randomUUID();
        RegistroOficialPoc r1 = new RegistroOficialPoc();
        r1.setStartupId(startupId);
        r1.setEscopoTestado("POC em memoria");
        service.salvar(r1);

        when(repository.findByStartupIdOrderByDataEmissaoDesc(startupId)).thenThrow(new RuntimeException("DB down"));

        List<RegistroOficialPocResponseDTO> lista = service.listarPorStartup(startupId);

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
        assertEquals("POC em memoria", lista.get(0).escopoTestado());
    }

    @Test
    @DisplayName("RF20 - Buscar registro por ID e por Codigo Verificador")
    void buscarPorIdECodigoVerificador() {
        UUID id = UUID.randomUUID();
        String codigo = "POC-2026-VAL12345";
        RegistroOficialPoc r = new RegistroOficialPoc();
        r.setId(id);
        r.setCodigoVerificador(codigo);

        when(repository.findById(id)).thenReturn(Optional.of(r));
        when(repository.findByCodigoVerificador(codigo)).thenReturn(Optional.of(r));

        RegistroOficialPocResponseDTO porId = service.buscarPorId(id);
        assertEquals(id, porId.id());

        RegistroOficialPocResponseDTO porCodigo = service.buscarPorCodigoVerificador(codigo);
        assertEquals(codigo, porCodigo.codigoVerificador());

        assertThrows(ResponseStatusException.class, () -> service.buscarPorId(null));
        assertThrows(ResponseStatusException.class, () -> service.buscarPorId(UUID.randomUUID()));

        assertThrows(ResponseStatusException.class, () -> service.buscarPorCodigoVerificador(null));
        assertThrows(ResponseStatusException.class, () -> service.buscarPorCodigoVerificador("  "));
        assertThrows(ResponseStatusException.class, () -> service.buscarPorCodigoVerificador("INEXISTENTE"));
    }

    @Test
    @DisplayName("buscarPorTermoPilotoId trata nulo e busca em memoria")
    void buscarPorTermoPilotoIdTrataNulo() {
        assertTrue(service.buscarPorTermoPilotoId(null).isEmpty());

        UUID termoId = UUID.randomUUID();
        RegistroOficialPoc r = new RegistroOficialPoc();
        r.setTermoPilotoId(termoId);
        service.salvar(r);

        when(repository.findByTermoPilotoId(termoId)).thenThrow(new RuntimeException());
        assertTrue(service.buscarPorTermoPilotoId(termoId).isPresent());
    }
}
