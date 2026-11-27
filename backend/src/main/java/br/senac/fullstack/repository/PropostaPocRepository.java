package br.senac.fullstack.repository;

import br.senac.fullstack.model.PropostaPoc;
import br.senac.fullstack.model.StatusProposta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface PropostaPocRepository extends JpaRepository<PropostaPoc, UUID> {

    List<PropostaPoc> findByStartupId(UUID startupId);

    Page<PropostaPoc> findByStartupId(UUID startupId, Pageable pageable);

    Page<PropostaPoc> findByStartupIdAndStatus(UUID startupId, StatusProposta status, Pageable pageable);

    List<PropostaPoc> findByDesafioValidacaoId(UUID desafioValidacaoId);

    List<PropostaPoc> findByStatusAndDesafioValidacaoIdIn(StatusProposta status, Collection<UUID> desafioValidacaoIds);

    Page<PropostaPoc> findByStatusAndDesafioValidacaoIdIn(StatusProposta status, Collection<UUID> desafioValidacaoIds, Pageable pageable);

    long countByStartupIdAndStatus(UUID startupId, StatusProposta status);

    boolean existsByStartupIdAndDesafioValidacaoIdAndStatusIn(UUID startupId, UUID desafioValidacaoId, Collection<StatusProposta> statuses);
}
