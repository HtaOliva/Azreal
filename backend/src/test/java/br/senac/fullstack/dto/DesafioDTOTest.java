package br.senac.fullstack.dto;

import br.senac.fullstack.model.AmbienteValidacao;
import br.senac.fullstack.model.DesafioValidacao;
import br.senac.fullstack.model.ModalidadeTeste;
import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.model.StatusDesafio;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DesafioDTOTest {

    private AmbienteValidacao criarAmbienteValido() {
        AmbienteValidacao ambiente = new AmbienteValidacao();
        ambiente.setId(UUID.randomUUID());
        ambiente.setRazaoSocial("Hospital Regional S.A.");
        ambiente.setNomeFantasia("Hospital Regional");
        ambiente.setSetor("Saude");
        ambiente.setPorte(PorteOrganizacao.GRANDE);
        ambiente.setUfIbge("PE");
        ambiente.setMunicipioIbge("Recife");
        ambiente.setEmailContato("contato@hospitalregional.com.br");
        ambiente.setTelefoneContato("81999998888");
        ambiente.setLinkedinUrl("https://linkedin.com/company/hospitalregional");
        return ambiente;
    }

    private DesafioValidacao criarDesafio(AmbienteValidacao ambiente) {
        DesafioValidacao desafio = new DesafioValidacao();
        desafio.setId(UUID.randomUUID());
        desafio.setAmbienteValidacao(ambiente);
        desafio.setTitulo("Otimização de Triagem de Emergência");
        desafio.setDescricaoProblema("Filas excessivas na triagem com tempo de espera superior a 40 minutos");
        desafio.setPublicoAlvo("Pacientes classificados como amarelo/verde");
        desafio.setTamanhoAmostra(150);
        desafio.setModalidade(ModalidadeTeste.PRESENCIAL);
        desafio.setDuracaoMaxima(30);
        desafio.setStatus(StatusDesafio.ABERTO);
        desafio.setAtivo(true);
        desafio.setDataCriacao(LocalDateTime.now());
        return desafio;
    }

    @Test
    void requestDTODeveArmazenarDadosInformados() {
        UUID ambienteId = UUID.randomUUID();
        DesafioValidacaoRequestDTO req = new DesafioValidacaoRequestDTO(
                ambienteId,
                "Integrar Totem",
                "Problema no totem",
                "Usuarios",
                50,
                ModalidadeTeste.REMOTO,
                15
        );

        assertEquals(ambienteId, req.ambienteValidacaoId());
        assertEquals("Integrar Totem", req.titulo());
        assertEquals("Problema no totem", req.descricaoProblema());
        assertEquals("Usuarios", req.publicoAlvo());
        assertEquals(50, req.tamanhoAmostra());
        assertEquals(ModalidadeTeste.REMOTO, req.modalidade());
        assertEquals(15, req.duracaoMaxima());
    }

    @Test
    void responseDTODeveConterVisibilidadeCompletaParaAutorEAdmin() {
        AmbienteValidacao ambiente = criarAmbienteValido();
        DesafioValidacao desafio = criarDesafio(ambiente);

        DesafioValidacaoResponseDTO dto = DesafioValidacaoResponseDTO.fromEntity(desafio);

        assertEquals(desafio.getId(), dto.id());
        assertEquals(ambiente.getId(), dto.ambienteValidacaoId());
        assertEquals("Hospital Regional", dto.ambienteValidacaoNome());
        assertEquals("Hospital Regional S.A.", dto.razaoSocial());
        assertEquals("contato@hospitalregional.com.br", dto.emailContato());
        assertEquals("81999998888", dto.telefoneContato());
        assertEquals("Saude", dto.setor());
        assertEquals(PorteOrganizacao.GRANDE, dto.porte());
        assertEquals("PE", dto.ufIbge());
        assertEquals("Recife", dto.municipioIbge());
        assertEquals(desafio.getTitulo(), dto.titulo());
        assertEquals(desafio.getDescricaoProblema(), dto.descricaoProblema());
        assertEquals(desafio.getPublicoAlvo(), dto.publicoAlvo());
        assertEquals(150, dto.tamanhoAmostra());
        assertEquals(ModalidadeTeste.PRESENCIAL, dto.modalidade());
        assertEquals(30, dto.duracaoMaxima());
        assertEquals(StatusDesafio.ABERTO, dto.status());
        assertTrue(dto.ativo());
        assertNotNull(dto.dataCriacao());
    }

    @Test
    void desafioAnonimoResponseDTODeveIsolarPrivacidadeNaoExpondoIdentidade() {
        AmbienteValidacao ambiente = criarAmbienteValido();
        DesafioValidacao desafio = criarDesafio(ambiente);

        DesafioAnonimoResponseDTO anonimo = DesafioAnonimoResponseDTO.fromEntity(desafio);

        // Campos permitidos
        assertEquals(desafio.getId(), anonimo.id());
        assertEquals(desafio.getTitulo(), anonimo.titulo());
        assertEquals(desafio.getDescricaoProblema(), anonimo.descricaoProblema());
        assertEquals("Saude", anonimo.setor());
        assertEquals(PorteOrganizacao.GRANDE, anonimo.porte());
        assertEquals("PE", anonimo.ufIbge());
        assertEquals("Recife", anonimo.municipioIbge());
        assertEquals(ModalidadeTeste.PRESENCIAL, anonimo.modalidade());
        assertEquals("Pacientes classificados como amarelo/verde", anonimo.publicoAlvo());
        assertEquals(150, anonimo.tamanhoAmostra());
        assertEquals(30, anonimo.duracaoMaxima());
        assertEquals(StatusDesafio.ABERTO, anonimo.status());
        assertNotNull(anonimo.dataCriacao());

        // Verificação de isolamento estrito de privacidade (RNF04):
        // Nenhuma propriedade ou campo do DTO anônimo expõe razão social, nome fantasia, contato ou LinkedIn
        String dtoAsString = anonimo.toString();
        org.junit.jupiter.api.Assertions.assertFalse(dtoAsString.contains("Hospital Regional"));
        org.junit.jupiter.api.Assertions.assertFalse(dtoAsString.contains("Hospital Regional S.A."));
        org.junit.jupiter.api.Assertions.assertFalse(dtoAsString.contains("contato@hospitalregional.com.br"));
        org.junit.jupiter.api.Assertions.assertFalse(dtoAsString.contains("81999998888"));
    }
}
