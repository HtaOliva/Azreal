package br.senac.fullstack.service;

import br.senac.fullstack.dto.DesafioAnonimoResponseDTO;
import br.senac.fullstack.dto.DesafioValidacaoRequestDTO;
import br.senac.fullstack.dto.DesafioValidacaoResponseDTO;
import br.senac.fullstack.dto.DesafioValidacaoUpdateDTO;
import br.senac.fullstack.model.ModalidadeTeste;
import br.senac.fullstack.model.StatusDesafio;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DesafioValidacaoServiceTest {

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
                "Descrição detalhada do problema de validação operacional",
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
    void atualizarOuInativarInexistenteLanca404() {
        DesafioValidacaoService s = service();
        UUID inexistente = UUID.randomUUID();
        DesafioValidacaoUpdateDTO update = new DesafioValidacaoUpdateDTO(
                "Teste", null, null, null, null, null, null);

        assertThrows(ResponseStatusException.class, () -> s.atualizar(inexistente, update));
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

        // Retorna apenas os ativos
        assertEquals(1, anonimos.size());
        DesafioAnonimoResponseDTO anonimo = anonimos.get(0);
        assertEquals(d1.id(), anonimo.id());
        assertEquals("Desafio Ativo 1", anonimo.titulo());
        assertEquals("Descrição detalhada do problema de validação operacional", anonimo.descricaoProblema());
        assertNotNull(anonimo.setor());
        assertNotNull(anonimo.porte());
        assertNotNull(anonimo.ufIbge());
        assertNotNull(anonimo.municipioIbge());
        assertEquals(ModalidadeTeste.PRESENCIAL, anonimo.modalidade());
        assertEquals(100, anonimo.tamanhoAmostra());
        assertEquals(30, anonimo.duracaoMaxima());

        // Assertivas de isolamento estrito de privacidade (RNF04)
        String anonimoPayload = anonimo.toString();
        assertFalse(anonimoPayload.contains("emailContato"));
        assertFalse(anonimoPayload.contains("telefoneContato"));
        assertFalse(anonimoPayload.contains("razaoSocial"));
        assertFalse(anonimoPayload.contains("nomeFantasia"));
    }
}
