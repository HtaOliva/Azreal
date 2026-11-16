package br.senac.fullstack.service;

import br.senac.fullstack.dto.AceiteTermoRequestDTO;
import br.senac.fullstack.dto.StartupRequestDTO;
import br.senac.fullstack.dto.StartupResponseDTO;
import br.senac.fullstack.dto.StartupUpdateDTO;
import br.senac.fullstack.model.Estagio;
import br.senac.fullstack.model.ModalidadeTeste;
import br.senac.fullstack.model.Startup;
import br.senac.fullstack.repository.StartupRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StartupServiceTest {

    private final AceiteTermoService aceiteService = new AceiteTermoService(new NullObjectProvider<>());

    private StartupService service() {
        return new StartupService(new NullObjectProvider<>(), aceiteService);
    }

    private UUID aceiteId() {
        return aceiteService.registrar(new AceiteTermoRequestDTO("v1", "127.0.0.1"), null, "127.0.0.1").id();
    }

    @Test
    void criarSemAceiteLancaBadRequest() {
        StartupService s = service();
        StartupRequestDTO req = new StartupRequestDTO(
                "Startup", "s@e.com", "senha123", "desc", "area",
                LocalDate.now(), Estagio.PROTOTIPO_INICIAL, null, null, null, null, null, null);
        assertThrows(Exception.class, () -> s.criar(req));
    }

    @Test
    void criarComAceiteECamposPocMatchRetornaStartup() {
        StartupService s = service();
        UUID aid = aceiteId();
        UUID temaId = UUID.randomUUID();
        StartupRequestDTO req = new StartupRequestDTO(
                "Rede Leito", "contato@redeleito.com", "senha123", "Gestao de leitos",
                "Saude", LocalDate.now(), Estagio.MVP_FUNCIONAL, List.of("Arthur"),
                "Recife", "PE", true, "Porto Digital", aid,
                "PE", "Recife", "Validar reducao de fila hospitalar",
                List.of(temaId), List.of(ModalidadeTeste.PRESENCIAL));
        StartupResponseDTO r = s.criar(req);
        assertNotNull(r);
        assertNotNull(r.id());
        assertEquals("Rede Leito", r.nome());
        assertEquals("PE", r.ufIbge());
        assertEquals("Recife", r.municipioIbge());
        assertEquals(List.of(temaId), r.temasIds());
        assertEquals(List.of(ModalidadeTeste.PRESENCIAL), r.modalidadesSuportadas());
    }

    @Test
    void buscarInexistenteLanca404() {
        StartupService s = service();
        assertThrows(Exception.class, () -> s.buscar(UUID.randomUUID()));
    }

    @Test
    void listarVazio() {
        StartupService s = service();
        assertTrue(s.listar().isEmpty());
    }

    @Test
    void atualizarInexistenteLanca404() {
        StartupService s = service();
        StartupUpdateDTO update = new StartupUpdateDTO(null, null, null, null, null, null, null, null, null);
        assertThrows(Exception.class, () -> s.atualizar(UUID.randomUUID(), update));
    }

    @Test
    void buscarPorEmailNuloLanca404() {
        StartupService s = service();
        assertThrows(ResponseStatusException.class, () -> s.buscarPorEmail(null));
    }

    @Test
    void buscarPorEmailEmMemoriaExistenteEInexistente() {
        StartupService s = service();
        UUID aid = aceiteId();
        StartupRequestDTO req = new StartupRequestDTO(
                "Health PE", "health@pe.com", "senha123", "desc", "Saude",
                LocalDate.now(), Estagio.MVP_FUNCIONAL, List.of("Lucas"),
                "Recife", "PE", false, null, aid, null, null, null, null, null);
        StartupResponseDTO criado = s.criar(req);

        StartupResponseDTO encontrado = s.buscarPorEmail("health@pe.com");
        assertEquals(criado.id(), encontrado.id());
        assertThrows(ResponseStatusException.class, () -> s.buscarPorEmail("inexistente@pe.com"));
    }

    @Test
    void buscarPorEmailComRepositorio() {
        StartupRepository repo = mock(StartupRepository.class);
        Startup entidade = new Startup();
        entidade.setNome("BioTech");
        entidade.setEmail("bio@tech.com");
        when(repo.findByEmail("bio@tech.com")).thenReturn(Optional.of(entidade));
        when(repo.findByEmail("inexistente@tech.com")).thenReturn(Optional.empty());

        StartupService s = new StartupService(new FixedObjectProvider<>(repo), aceiteService);
        StartupResponseDTO encontrado = s.buscarPorEmail("bio@tech.com");
        assertEquals("BioTech", encontrado.nome());
        assertThrows(ResponseStatusException.class, () -> s.buscarPorEmail("inexistente@tech.com"));
    }
}
