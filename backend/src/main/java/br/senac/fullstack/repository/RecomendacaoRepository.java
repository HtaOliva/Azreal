package br.senac.fullstack.repository;

import br.senac.fullstack.model.Recomendacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecomendacaoRepository extends JpaRepository<Recomendacao, UUID> {
    List<Recomendacao> findByStartupId(UUID startupId);
    List<Recomendacao> findByMentorId(UUID mentorId);
}
