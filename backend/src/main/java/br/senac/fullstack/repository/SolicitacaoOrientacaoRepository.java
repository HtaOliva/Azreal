package br.senac.fullstack.repository;

import br.senac.fullstack.model.SolicitacaoOrientacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SolicitacaoOrientacaoRepository extends JpaRepository<SolicitacaoOrientacao, UUID> {
    List<SolicitacaoOrientacao> findByMentorIdAndStatus(UUID mentorId, br.senac.fullstack.model.StatusSolicitacao status);
    long countByStartupIdAndStatusIn(UUID startupId, List<br.senac.fullstack.model.StatusSolicitacao> statuses);
    List<SolicitacaoOrientacao> findByStartupId(UUID startupId);
}
