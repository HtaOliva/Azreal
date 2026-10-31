package br.senac.fullstack.service;

import br.senac.fullstack.dto.AceiteTermoRequestDTO;
import br.senac.fullstack.dto.AmbienteValidacaoRequestDTO;
import br.senac.fullstack.dto.AmbienteValidacaoResponseDTO;
import br.senac.fullstack.dto.AmbienteValidacaoUpdateDTO;
import br.senac.fullstack.model.AmbienteValidacao;
import br.senac.fullstack.model.PorteOrganizacao;
import br.senac.fullstack.repository.AmbienteValidacaoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AmbienteValidacaoServiceRepoTest {

    private final AceiteTermoService aceiteService = new AceiteTermoService(new NullObjectProvider<>());

    private UUID aceiteId() {
        return aceiteService.registrar(new AceiteTermoRequestDTO("v1", "127.0.0.1"), null, "127.0.0.1").id();
    }

    @Test
    void operacoesComRepositorioFuncionam() {
        AmbienteValidacaoRepository repo = mock(AmbienteValidacaoRepository.class);
        AmbienteValidacao entidade = new AmbienteValidacao();
        entidade.setRazaoSocial("Secretaria Municipal");
        entidade.setNomeFantasia("Sec. Inovacao");
        entidade.setSetor("Governo");
        entidade.setPorte(PorteOrganizacao.ORGAO_PUBLICO);
        entidade.setUfIbge("PE");
        entidade.setMunicipioIbge("Recife");
        entidade.setEmailContato("inovacao@recife.pe.gov.br");
        UUID id = entidade.getId();
        UUID inexistente = UUID.randomUUID();

        when(repo.save(any(AmbienteValidacao.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repo.findById(id)).thenReturn(Optional.of(entidade));
        when(repo.findById(inexistente)).thenReturn(Optional.empty());
        when(repo.findAll()).thenReturn(List.of(entidade));

        AmbienteValidacaoService svc = new AmbienteValidacaoService(new FixedObjectProvider<>(repo), aceiteService);

        AmbienteValidacaoRequestDTO req = new AmbienteValidacaoRequestDTO(
                "Secretaria Municipal", "Sec. Inovacao", "Governo", PorteOrganizacao.ORGAO_PUBLICO,
                "PE", "Recife", "inovacao@recife.pe.gov.br", null, null, "senha123", aceiteId());
        AmbienteValidacaoResponseDTO criado = svc.criar(req);
        assertNotNull(criado);

        AmbienteValidacaoResponseDTO buscado = svc.buscar(id);
        assertEquals("Sec. Inovacao", buscado.nomeFantasia());
        assertFalse(svc.listar().isEmpty());

        AmbienteValidacaoUpdateDTO update = new AmbienteValidacaoUpdateDTO(
                null, "Sec. Transformacao Digital", null, null, null, null, null, null, null);
        AmbienteValidacaoResponseDTO atualizado = svc.atualizar(id, update);
        assertEquals("Sec. Transformacao Digital", atualizado.nomeFantasia());

        assertThrows(ResponseStatusException.class, () -> svc.buscar(inexistente));
        assertThrows(ResponseStatusException.class, () -> svc.atualizar(inexistente, update));
    }
}
