package br.senac.fullstack.service;

import br.senac.fullstack.dto.AceiteTermoRequestDTO;
import br.senac.fullstack.model.AceiteTermo;
import br.senac.fullstack.repository.AceiteTermoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AceiteTermoServiceTest {

    private AceiteTermoService service() {
        return new AceiteTermoService(new NullObjectProvider<>());
    }

    @Test
    void registrarRetornaAceite() {
        AceiteTermoService s = service();
        var r = s.registrar(new AceiteTermoRequestDTO("v1", "127.0.0.1"), null, "127.0.0.1");
        assertNotNull(r);
        assertNotNull(r.id());
        assertTrue(r.consentimentoIaAnonimizado());
    }

    @Test
    void registrarSemConsentimentoIaLancaBadRequest() {
        AceiteTermoService s = service();
        AceiteTermoRequestDTO request = new AceiteTermoRequestDTO("v1", "127.0.0.1", Boolean.FALSE);
        assertThrows(ResponseStatusException.class, () -> s.registrar(request, null, "127.0.0.1"));
    }

    @Test
    void registrarComIpParametroNuloUsaIpDoRequest() {
        AceiteTermoService s = service();
        var r = s.registrar(new AceiteTermoRequestDTO("v1", "10.0.0.1"), UUID.randomUUID(), null);
        assertNotNull(r);
        AceiteTermo encontrado = s.buscar(r.id());
        assertEquals("10.0.0.1", encontrado.getIp());
        assertNotNull(encontrado.getUsuarioId());
    }

    @Test
    void existeSemRepoRetornaFalseParaNull() {
        AceiteTermoService s = service();
        assertFalse(s.existe(null));
    }

    @Test
    void existeAposRegistrarRetornaTrue() {
        AceiteTermoService s = service();
        UUID id = s.registrar(new AceiteTermoRequestDTO("v1", "127.0.0.1"), null, "127.0.0.1").id();
        assertTrue(s.existe(id));
    }

    @Test
    void buscarInexistenteLanca404() {
        AceiteTermoService s = service();
        assertThrows(Exception.class, () -> s.buscar(UUID.randomUUID()));
    }

    @Test
    void operacoesComRepositorioFuncionam() {
        AceiteTermoRepository repo = mock(AceiteTermoRepository.class);
        AceiteTermo salvo = new AceiteTermo();
        salvo.setVersaoTermo("v2");
        salvo.setIp("127.0.0.1");
        UUID id = salvo.getId();

        when(repo.save(any(AceiteTermo.class))).thenReturn(salvo);
        when(repo.existsById(id)).thenReturn(true);
        when(repo.findById(id)).thenReturn(Optional.of(salvo));
        when(repo.findById(UUID.fromString("00000000-0000-0000-0000-000000000001"))).thenReturn(Optional.empty());

        AceiteTermoService s = new AceiteTermoService(new FixedObjectProvider<>(repo));
        var resp = s.registrar(new AceiteTermoRequestDTO("v2", "127.0.0.1"), null, "127.0.0.1");
        assertEquals("v2", resp.versaoTermo());
        assertTrue(s.existe(id));
        assertNotNull(s.buscar(id));
        assertThrows(Exception.class, () -> s.buscar(UUID.fromString("00000000-0000-0000-0000-000000000001")));
    }
}
