package br.senac.fullstack.repository;

import br.senac.fullstack.model.Estagio;
import br.senac.fullstack.model.Startup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StartupRepository extends JpaRepository<Startup, UUID> {
    Optional<Startup> findByEmail(String email);

    Page<Startup> findByEstagioAtual(Estagio estagioAtual, Pageable pageable);

    @Query("SELECT s FROM Startup s WHERE "
            + "(:estagio IS NULL OR s.estagioAtual = :estagio) "
            + "AND (:termoBusca IS NULL OR LOWER(s.nome) LIKE LOWER(CONCAT('%', :termoBusca, '%')) "
            + "OR LOWER(s.descricao) LIKE LOWER(CONCAT('%', :termoBusca, '%')) "
            + "OR LOWER(s.areaAtuacao) LIKE LOWER(CONCAT('%', :termoBusca, '%')))")
    Page<Startup> buscarComFiltros(
            @Param("estagio") Estagio estagio,
            @Param("termoBusca") String termoBusca,
            Pageable pageable);
}
