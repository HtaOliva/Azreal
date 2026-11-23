package br.senac.fullstack.repository;

import br.senac.fullstack.model.DesafioValidacao;
import br.senac.fullstack.model.ModalidadeTeste;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DesafioValidacaoRepository extends JpaRepository<DesafioValidacao, UUID> {

    List<DesafioValidacao> findByAmbienteValidacaoId(UUID ambienteValidacaoId);

    List<DesafioValidacao> findByAtivoTrue();

    Page<DesafioValidacao> findByAtivoTrue(Pageable pageable);

    Page<DesafioValidacao> findByAmbienteValidacaoId(UUID ambienteValidacaoId, Pageable pageable);

    @Query("SELECT d FROM DesafioValidacao d WHERE d.ativo = true "
            + "AND (:modalidade IS NULL OR d.modalidade = :modalidade) "
            + "AND (:termoBusca IS NULL OR LOWER(d.titulo) LIKE LOWER(CONCAT('%', :termoBusca, '%')) "
            + "OR LOWER(d.descricaoProblema) LIKE LOWER(CONCAT('%', :termoBusca, '%')))")
    Page<DesafioValidacao> findByAtivoTrueComFiltros(
            @Param("modalidade") ModalidadeTeste modalidade,
            @Param("termoBusca") String termoBusca,
            Pageable pageable);
}
