package br.senac.fullstack.service;

import br.senac.fullstack.dto.AmbienteValidacaoResponseDTO;
import br.senac.fullstack.dto.DesafioAnonimoResponseDTO;
import br.senac.fullstack.dto.DesafioValidacaoRequestDTO;
import br.senac.fullstack.dto.DesafioValidacaoResponseDTO;
import br.senac.fullstack.dto.DesafioValidacaoUpdateDTO;
import br.senac.fullstack.model.AmbienteValidacao;
import br.senac.fullstack.model.DesafioValidacao;
import br.senac.fullstack.model.ModalidadeTeste;
import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.model.StatusDesafio;
import br.senac.fullstack.repository.AmbienteValidacaoRepository;
import br.senac.fullstack.repository.DesafioValidacaoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DesafioValidacaoServiceTest {

    private static final String DESCRICAO_PROBLEMA_PADRAO = "Descrição detalhada do problema de validação operacional";
    private static final String DESAFIO_REPO_TITULO = "Desafio Repo";

    private DesafioValidacaoService service() {
        return new DesafioValidacaoService(
                new NullObjectProvider<>(),
                new NullObjectProvider<>(),
                new NullObjectProvider<>()
        );
    }

    private DesafioValidacaoRequestDTO novoRequest(UUID ambienteId, String titulo) {
        return new DesafioValidacaoRequestDTO(
                ambienteId,
                titulo,
                DESCRICAO_PROBLEMA_PADRAO,
                "Usuários do sistema interno",
                100,
                ModalidadeTeste.PRESENCIAL,
                30
        );
    }

    @Test
    void criarEBuscarDesafioEmMemoria() {
        DesafioValidacaoService s = service();
        UUID ambienteId = UUID.randomUUID();

        DesafioValidacaoResponseDTO criado = s.criar(novoRequest(ambienteId, "Desafio 1"));

        assertNotNull(criado);
        assertNotNull(criado.id());
        assertEquals("Desafio 1", criado.titulo());
        assertEquals(ambienteId, criado.ambienteValidacaoId());
        assertTrue(criado.ativo());
        assertEquals(StatusDesafio.ABERTO, criado.status());

        DesafioValidacaoResponseDTO buscado = s.buscar(criado.id());
        assertEquals(criado.id(), buscado.id());
        assertEquals("Desafio 1", buscado.titulo());
    }

    @Test
    void buscarInexistenteLanca404() {
        DesafioValidacaoService s = service();
        UUID idInexistente = UUID.randomUUID();

        assertThrows(ResponseStatusException.class, () -> s.buscar(idInexistente));
    }

    @Test
    void atualizarCamposDesafioEmMemoria() {
        DesafioValidacaoService s = service();
        UUID ambienteId = UUID.randomUUID();

        DesafioValidacaoResponseDTO criado = s.criar(novoRequest(ambienteId, "Titulo Antigo"));

        DesafioValidacaoUpdateDTO update = new DesafioValidacaoUpdateDTO(
                "Titulo Atualizado",
                "Nova descricao do problema",
                "Novo publico alvo",
                200,
                ModalidadeTeste.REMOTO,
                45,
                StatusDesafio.EM_ANDAMENTO
        );

        DesafioValidacaoResponseDTO atualizado = s.atualizar(criado.id(), update);

        assertEquals("Titulo Atualizado", atualizado.titulo());
        assertEquals("Nova descricao do problema", atualizado.descricaoProblema());
        assertEquals("Novo publico alvo", atualizado.publicoAlvo());
        assertEquals(200, atualizado.tamanhoAmostra());
        assertEquals(ModalidadeTeste.REMOTO, atualizado.modalidade());
        assertEquals(45, atualizado.duracaoMaxima());
        assertEquals(StatusDesafio.EM_ANDAMENTO, atualizado.status());
    }

    @Test
    void atualizarComRequestDTOCarregaSobrecarga() {
        DesafioValidacaoService s = service();
        UUID ambienteId = UUID.randomUUID();

        DesafioValidacaoResponseDTO criado = s.criar(novoRequest(ambienteId, "Desafio Base"));
        DesafioValidacaoRequestDTO reqUpdate = new DesafioValidacaoRequestDTO(
                ambienteId,
                "Titulo Reescrito",
                "Problema Reescrito",
                "Publico Reescrito",
                180,
                ModalidadeTeste.HIBRIDO,
                60
        );

        DesafioValidacaoResponseDTO atualizado = s.atualizar(criado.id(), reqUpdate);
        assertEquals("Titulo Reescrito", atualizado.titulo());
        assertEquals("Problema Reescrito", atualizado.descricaoProblema());
        assertEquals(180, atualizado.tamanhoAmostra());
        assertEquals(ModalidadeTeste.HIBRIDO, atualizado.modalidade());
    }

    @Test
    void inativarSoftDelete() {
        DesafioValidacaoService s = service();
        UUID ambienteId = UUID.randomUUID();

        DesafioValidacaoResponseDTO criado = s.criar(novoRequest(ambienteId, "Desafio Ativo"));
        assertTrue(criado.ativo());

        DesafioValidacaoResponseDTO inativado = s.inativar(criado.id());
        assertFalse(inativado.ativo());

        DesafioValidacaoResponseDTO buscado = s.buscar(criado.id());
        assertFalse(buscado.ativo());
    }

    @Test
    void atualizarInexistenteLanca404() {
        DesafioValidacaoService s = service();
        UUID inexistente = UUID.randomUUID();
        DesafioValidacaoUpdateDTO update = new DesafioValidacaoUpdateDTO(
                "Teste", null, null, null, null, null, null);

        assertThrows(ResponseStatusException.class, () -> s.atualizar(inexistente, update));
    }

    @Test
    void inativarInexistenteLanca404() {
        DesafioValidacaoService s = service();
        UUID inexistente = UUID.randomUUID();
        assertThrows(ResponseStatusException.class, () -> s.inativar(inexistente));
    }

    @Test
    void listarPorAmbienteFiltraApenasDoAmbienteSolicitado() {
        DesafioValidacaoService s = service();
        UUID ambienteA = UUID.randomUUID();
        UUID ambienteB = UUID.randomUUID();

        s.criar(novoRequest(ambienteA, "Desafio A1"));
        s.criar(novoRequest(ambienteA, "Desafio A2"));
        s.criar(novoRequest(ambienteB, "Desafio B1"));

        List<DesafioValidacaoResponseDTO> listaA = s.listarPorAmbiente(ambienteA);
        assertEquals(2, listaA.size());
        assertTrue(listaA.stream().allMatch(d -> ambienteA.equals(d.ambienteValidacaoId())));

        List<DesafioValidacaoResponseDTO> listaB = s.listarPorAmbiente(ambienteB);
        assertEquals(1, listaB.size());
        assertEquals("Desafio B1", listaB.get(0).titulo());

        assertTrue(s.listarPorAmbiente(null).isEmpty());
    }

    @Test
    void listarAtivosAnonimosIsolaPrivacidadeConformeRnf04() {
        DesafioValidacaoService s = service();
        UUID ambienteId = UUID.randomUUID();

        DesafioValidacaoResponseDTO d1 = s.criar(novoRequest(ambienteId, "Desafio Ativo 1"));
        DesafioValidacaoResponseDTO d2 = s.criar(novoRequest(ambienteId, "Desafio que sera inativado"));
        s.inativar(d2.id());

        List<DesafioAnonimoResponseDTO> anonimos = s.listarAtivosAnonimos();

        assertEquals(1, anonimos.size());
        DesafioAnonimoResponseDTO anonimo = anonimos.get(0);
        assertEquals(d1.id(), anonimo.id());
        assertEquals("Desafio Ativo 1", anonimo.titulo());
        assertEquals(DESCRICAO_PROBLEMA_PADRAO, anonimo.descricaoProblema());
        assertNotNull(anonimo.setor());
        assertNotNull(anonimo.porte());
        assertNotNull(anonimo.ufIbge());
        assertNotNull(anonimo.municipioIbge());
        assertEquals(ModalidadeTeste.PRESENCIAL, anonimo.modalidade());
        assertEquals(100, anonimo.tamanhoAmostra());
        assertEquals(30, anonimo.duracaoMaxima());

        String anonimoPayload = anonimo.toString();
        assertFalse(anonimoPayload.contains("emailContato"));
        assertFalse(anonimoPayload.contains("telefoneContato"));
        assertFalse(anonimoPayload.contains("razaoSocial"));
        assertFalse(anonimoPayload.contains("nomeFantasia"));
    }

    @Test
    void criarComAmbienteIdNuloLanca400() {
        DesafioValidacaoService s = service();
        DesafioValidacaoRequestDTO req = new DesafioValidacaoRequestDTO(
                null, "Titulo", "Problema", null, null, ModalidadeTeste.REMOTO, null);
        assertThrows(ResponseStatusException.class, () -> s.criar(req));
    }

    @Test
    void integrarComAmbienteValidacaoRepositoryQuandoDisponivel() {
        AmbienteValidacaoRepository ambRepo = mock(AmbienteValidacaoRepository.class);
        UUID ambId = UUID.randomUUID();
        AmbienteValidacao amb = new AmbienteValidacao();
        amb.setId(ambId);
        amb.setSetor("Logistica");
        amb.setPorte(PorteOrganizacao.PEQUENO);
        when(ambRepo.findById(ambId)).thenReturn(Optional.of(amb));

        DesafioValidacaoRepository desafioRepo = mock(DesafioValidacaoRepository.class);
        when(desafioRepo.save(any(DesafioValidacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DesafioValidacaoService s = new DesafioValidacaoService(
                new FixedObjectProvider<>(desafioRepo),
                new FixedObjectProvider<>(ambRepo),
                new NullObjectProvider<>()
        );

        DesafioValidacaoResponseDTO dto = s.criar(novoRequest(ambId, DESAFIO_REPO_TITULO));
        assertNotNull(dto);
        assertEquals("Logistica", dto.setor());
        verify(ambRepo).findById(ambId);
        verify(desafioRepo).save(any());
    }

    @Test
    void integrarComAmbienteValidacaoRepositoryNaoEncontradoLanca404() {
        AmbienteValidacaoRepository ambRepo = mock(AmbienteValidacaoRepository.class);
        UUID ambId = UUID.randomUUID();
        when(ambRepo.findById(ambId)).thenReturn(Optional.empty());

        DesafioValidacaoService s = new DesafioValidacaoService(
                new NullObjectProvider<>(),
                new FixedObjectProvider<>(ambRepo),
                new NullObjectProvider<>()
        );

        DesafioValidacaoRequestDTO req = novoRequest(ambId, DESAFIO_REPO_TITULO);
        assertThrows(ResponseStatusException.class, () -> s.criar(req));
    }

    @Test
    void integrarComAmbienteValidacaoServiceQuandoDisponivel() {
        AmbienteValidacaoService ambService = mock(AmbienteValidacaoService.class);
        UUID ambId = UUID.randomUUID();
        AmbienteValidacaoResponseDTO ambDTO = new AmbienteValidacaoResponseDTO(
                ambId, "Org S.A.", "Org Fantasia", "Agro", PorteOrganizacao.GRANDE,
                "PE", "Petrolina", "agro@org.com", "87999990000", "https://linkedin.com",
                true, LocalDateTime.now());
        when(ambService.buscar(ambId)).thenReturn(ambDTO);

        DesafioValidacaoService s = new DesafioValidacaoService(
                new NullObjectProvider<>(),
                new NullObjectProvider<>(),
                new FixedObjectProvider<>(ambService)
        );

        DesafioValidacaoResponseDTO dto = s.criar(novoRequest(ambId, "Desafio com Service"));
        assertNotNull(dto);
        assertEquals("Agro", dto.setor());
        assertEquals("Org Fantasia", dto.ambienteValidacaoNome());
    }

    @Test
    void operacoesDelegamParaDesafioValidacaoRepositoryQuandoPresente() {
        DesafioValidacaoRepository desafioRepo = mock(DesafioValidacaoRepository.class);
        UUID desafioId = UUID.randomUUID();
        UUID ambId = UUID.randomUUID();

        AmbienteValidacao amb = new AmbienteValidacao();
        amb.setId(ambId);
        amb.setSetor("Fintech");
        amb.setPorte(PorteOrganizacao.MEDIO);

        DesafioValidacao d = new DesafioValidacao();
        d.setId(desafioId);
        d.setAmbienteValidacao(amb);
        d.setTitulo("Desafio Banco");
        d.setAtivo(true);
        d.setStatus(StatusDesafio.ABERTO);

        when(desafioRepo.findById(desafioId)).thenReturn(Optional.of(d));
        when(desafioRepo.save(any())).thenReturn(d);
        when(desafioRepo.findByAmbienteValidacaoId(ambId)).thenReturn(List.of(d));
        when(desafioRepo.findByAtivoTrue()).thenReturn(List.of(d));

        DesafioValidacaoService s = new DesafioValidacaoService(
                new FixedObjectProvider<>(desafioRepo),
                new NullObjectProvider<>(),
                new NullObjectProvider<>()
        );

        DesafioValidacaoResponseDTO buscado = s.buscar(desafioId);
        assertEquals("Desafio Banco", buscado.titulo());

        UUID inexistente = UUID.randomUUID();
        when(desafioRepo.findById(inexistente)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> s.buscar(inexistente));

        DesafioValidacaoUpdateDTO update = new DesafioValidacaoUpdateDTO(
                "Desafio Banco Atualizado", null, null, null, null, null, null);
        s.atualizar(desafioId, update);
        verify(desafioRepo).save(d);

        s.inativar(desafioId);
        assertFalse(d.isAtivo());

        List<DesafioValidacaoResponseDTO> porAmbiente = s.listarPorAmbiente(ambId);
        assertEquals(1, porAmbiente.size());

        List<DesafioAnonimoResponseDTO> ativos = s.listarAtivosAnonimos();
        assertEquals(1, ativos.size());
    }
}
