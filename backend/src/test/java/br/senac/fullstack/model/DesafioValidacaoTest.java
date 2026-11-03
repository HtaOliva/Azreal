package br.senac.fullstack.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DesafioValidacaoTest {

    @Test
    void instanciacaoComValoresPadrao() {
        DesafioValidacao desafio = new DesafioValidacao();

        assertNotNull(desafio.getId());
        assertNotNull(desafio.getDataCriacao());
        assertTrue(desafio.isAtivo());
        assertEquals(StatusDesafio.ABERTO, desafio.getStatus());
    }

    @Test
    void atribuicaoEConsultaDeCampos() {
        DesafioValidacao desafio = new DesafioValidacao();
        AmbienteValidacao ambiente = new AmbienteValidacao();
        UUID id = UUID.randomUUID();
        LocalDateTime agora = LocalDateTime.now();

        desafio.setId(id);
        desafio.setAmbienteValidacao(ambiente);
        desafio.setTitulo("Teste de Carga no Totem");
        desafio.setDescricaoProblema("Validar usabilidade da interface sob fila de espera");
        desafio.setPublicoAlvo("Pacientes em triagem");
        desafio.setTamanhoAmostra(100);
        desafio.setModalidade(ModalidadeTeste.PRESENCIAL);
        desafio.setDuracaoMaxima(30);
        desafio.setStatus(StatusDesafio.EM_ANDAMENTO);
        desafio.setAtivo(false);
        desafio.setDataCriacao(agora);

        assertEquals(id, desafio.getId());
        assertEquals(ambiente, desafio.getAmbienteValidacao());
        assertEquals("Teste de Carga no Totem", desafio.getTitulo());
        assertEquals("Validar usabilidade da interface sob fila de espera", desafio.getDescricaoProblema());
        assertEquals("Pacientes em triagem", desafio.getPublicoAlvo());
        assertEquals(100, desafio.getTamanhoAmostra());
        assertEquals(ModalidadeTeste.PRESENCIAL, desafio.getModalidade());
        assertEquals(30, desafio.getDuracaoMaxima());
        assertEquals(StatusDesafio.EM_ANDAMENTO, desafio.getStatus());
        assertEquals(false, desafio.isAtivo());
        assertEquals(agora, desafio.getDataCriacao());
    }
}
