package br.senac.fullstack.repository;

import br.senac.fullstack.model.DesafioValidacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DesafioValidacaoRepository extends JpaRepository<DesafioValidacao, UUID> {

    List<DesafioValidacao> findByAmbienteValidacaoId(UUID ambienteValidacaoId);

    List<DesafioValidacao> findByAtivoTrue();
}
