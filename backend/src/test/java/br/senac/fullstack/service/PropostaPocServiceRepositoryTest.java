package br.senac.fullstack.service;

import br.senac.fullstack.dto.PropostaPocRequestDTO;
import br.senac.fullstack.dto.PropostaPocResponseDTO;
import br.senac.fullstack.model.PropostaPoc;
import br.senac.fullstack.model.StatusProposta;
import br.senac.fullstack.repository.PropostaPocRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PropostaPocServiceRepositoryTest {

    private PropostaPocRepository repository;
    private PropostaPocService service;

    @BeforeEach
    void setUp() {
        repository = mock(PropostaPocRepository.class);
        service = new PropostaPocService(new FixedObjectProvider<>(repository), null, null);
    }

    @Test
    @DisplayName("Deve criar proposta persistindo pelo repositório JPA")
    void deveCriarPropostaComRepositorio() {
        UUID startupId = UUID.randomUUID();
        UUID desafioId = UUID.randomUUID();
        PropostaPocRequestDTO request = new PropostaPocRequestDTO(
                startupId,
                desafioId,
                "Escopo teste repositorio",
                20,
                "Resp startup",
                "Resp ambiente",
                "Metrica 90%",
                true
        );

        when(repository.countByStartupIdAndStatus(startupId, StatusProposta.PENDENTE)).thenReturn(0L);
        when(repository.existsByStartupIdAndDesafioValidacaoIdAndStatusIn(eq(startupId), eq(desafioId), any())).thenReturn(false);
        when(repository.save(any(PropostaPoc.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PropostaPocResponseDTO response = service.criar(request);

        assertNotNull(response);
        assertEquals(startupId, response.startupId());
        assertEquals(desafioId, response.desafioValidacaoId());
        verify(repository).save(any(PropostaPoc.class));
    }

    @Test
    @DisplayName("Deve buscar proposta por ID via repositório")
    void deveBuscarPorIdComRepositorio() {
        UUID id = UUID.randomUUID();
        PropostaPoc proposta = new PropostaPoc();
        proposta.setId(id);
        proposta.setStartupId(UUID.randomUUID());
        proposta.setDesafioValidacaoId(UUID.randomUUID());
        proposta.setEscopoTeste("Escopo teste");
        proposta.setMetricaValidacao("Metrica");
        proposta.setDuracaoSugeridaDias(10);
        proposta.setStatus(StatusProposta.PENDENTE);

        when(repository.findById(id)).thenReturn(Optional.of(proposta));

        PropostaPocResponseDTO resultado = service.buscar(id);

        assertNotNull(resultado);
        assertEquals(id, resultado.id());
        verify(repository).findById(id);
    }

    @Test
    @DisplayName("Deve listar fila de ambiente paginada via repositório")
    void deveListarFilaAmbientePaginadaComRepositorio() {
        UUID desafioId = UUID.randomUUID();
        PropostaPoc proposta = new PropostaPoc();
        proposta.setId(UUID.randomUUID());
        proposta.setStartupId(UUID.randomUUID());
        proposta.setDesafioValidacaoId(desafioId);
        proposta.setEscopoTeste("Escopo");
        proposta.setMetricaValidacao("Metrica");
        proposta.setDuracaoSugeridaDias(15);
        proposta.setStatus(StatusProposta.PENDENTE);

        when(repository.findByStatusAndDesafioValidacaoIdIn(eq(StatusProposta.PENDENTE), any(), any()))
                .thenReturn(new PageImpl<>(List.of(proposta), PageRequest.of(0, 10), 1));

        var pagina = service.listarFilaAmbientePaginado(List.of(desafioId), StatusProposta.PENDENTE, PageRequest.of(0, 10));

        assertNotNull(pagina);
        assertEquals(1, pagina.getTotalElements());
        verify(repository).findByStatusAndDesafioValidacaoIdIn(eq(StatusProposta.PENDENTE), any(), any());
    }
}
