package br.senac.fullstack.controller;

import br.senac.fullstack.dto.DesafioAnonimoResponseDTO;
import br.senac.fullstack.dto.PropostaPocRequestDTO;
import br.senac.fullstack.dto.PropostaPocResponseDTO;
import br.senac.fullstack.dto.StartupResponseDTO;
import br.senac.fullstack.model.Estagio;
import br.senac.fullstack.model.ModalidadeTeste;
import br.senac.fullstack.model.StatusProposta;
import br.senac.fullstack.service.DesafioValidacaoService;
import br.senac.fullstack.service.PropostaPocService;
import br.senac.fullstack.service.StartupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaginationAndFilteringTest {

    @Test
    @DisplayName("DesafioValidacaoController deve expor endpoint paginado com filtros")
    void testDesafiosPaginados() {
        DesafioValidacaoService service = mock(DesafioValidacaoService.class);
        DesafioValidacaoController controller = new DesafioValidacaoController(service);

        Pageable pageable = PageRequest.of(0, 5, Sort.by("dataCriacao").descending());
        when(service.listarAtivosAnonimosPaginado(eq("saude"), eq(ModalidadeTeste.REMOTO), any(Pageable.class)))
                .thenReturn(Page.empty(pageable));

        ResponseEntity<Page<DesafioAnonimoResponseDTO>> response =
                controller.listarAtivosAnonimosPaginado("saude", ModalidadeTeste.REMOTO, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getTotalElements());
    }

    @Test
    @DisplayName("StartupController deve expor endpoint paginado com filtros")
    void testStartupsPaginadas() {
        StartupService service = mock(StartupService.class);
        StartupController controller = new StartupController(service);

        Pageable pageable = PageRequest.of(0, 10, Sort.by("nome").ascending());
        when(service.listarPaginado(eq(Estagio.MVP_FUNCIONAL), eq("tech"), any(Pageable.class)))
                .thenReturn(Page.empty(pageable));

        ResponseEntity<Page<StartupResponseDTO>> response =
                controller.listarPaginado(Estagio.MVP_FUNCIONAL, "tech", pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getTotalElements());
    }

    @Test
    @DisplayName("PropostaPocService deve paginar e filtrar fila e propostas da startup em memoria")
    void testPropostaPocServicePaginacao() {
        PropostaPocService service = new PropostaPocService();
        UUID startupId = UUID.randomUUID();
        UUID desafioId1 = UUID.randomUUID();
        UUID desafioId2 = UUID.randomUUID();

        PropostaPocRequestDTO req1 = new PropostaPocRequestDTO(
                startupId, desafioId1, "Escopo 1", "Metrica 1", 30, ModalidadeTeste.REMOTO, true, true);
        PropostaPocRequestDTO req2 = new PropostaPocRequestDTO(
                startupId, desafioId2, "Escopo 2", "Metrica 2", 15, ModalidadeTeste.PRESENCIAL, true, true);

        service.criar(req1);
        service.criar(req2);

        Pageable pageable = PageRequest.of(0, 10);

        Page<PropostaPocResponseDTO> pageStartup = service.listarPorStartupPaginado(startupId, null, pageable);
        assertEquals(2, pageStartup.getTotalElements());

        Page<PropostaPocResponseDTO> pageFila = service.listarFilaAmbientePaginado(
                List.of(desafioId1, desafioId2), StatusProposta.PENDENTE, pageable);
        assertEquals(2, pageFila.getTotalElements());
        assertEquals(2, pageFila.getContent().size());

        Page<PropostaPocResponseDTO> pageFilaVazia = service.listarFilaAmbientePaginado(
                Collections.emptyList(), StatusProposta.PENDENTE, pageable);
        assertEquals(0, pageFilaVazia.getTotalElements());
    }

    @Test
    @DisplayName("DesafioValidacaoService deve filtrar e paginar em memoria quando repo e nulo")
    void testDesafioValidacaoServicePaginacaoMemoria() {
        DesafioValidacaoService service = new DesafioValidacaoService(null, null, null);
        Pageable pageable = PageRequest.of(0, 5);

        Page<DesafioAnonimoResponseDTO> page = service.listarAtivosAnonimosPaginado("teste", ModalidadeTeste.REMOTO, pageable);
        assertNotNull(page);
        assertTrue(page.isEmpty());
    }

    @Test
    @DisplayName("StartupService deve filtrar e paginar em memoria quando repo e nulo")
    void testStartupServicePaginacaoMemoria() {
        StartupService service = new StartupService(null, null);
        Pageable pageable = PageRequest.of(0, 5);

        Page<StartupResponseDTO> page = service.listarPaginado(Estagio.PROTOTIPO_INICIAL, "bio", pageable);
        assertNotNull(page);
        assertTrue(page.isEmpty());
    }

    @Test
    @DisplayName("PropostaPocController deve validar autenticacao em minhas paginado")
    void testPropostaPocControllerMinhasPaginadoSemAuth() {
        PropostaPocService service = new PropostaPocService();
        PropostaPocController controller = new PropostaPocController(service);

        ResponseEntity<Page<PropostaPocResponseDTO>> respSemAuth =
                controller.listarMinhasPaginado(UUID.randomUUID(), null, PageRequest.of(0, 10), null);
        assertEquals(HttpStatus.UNAUTHORIZED, respSemAuth.getStatusCode());

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "admin@senac.br", "123", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        ResponseEntity<Page<PropostaPocResponseDTO>> respComAuth =
                controller.listarMinhasPaginado(UUID.randomUUID(), null, PageRequest.of(0, 10), auth);
        assertEquals(HttpStatus.OK, respComAuth.getStatusCode());
    }
}
