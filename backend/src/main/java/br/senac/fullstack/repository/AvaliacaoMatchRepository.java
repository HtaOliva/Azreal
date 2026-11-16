package br.senac.fullstack.repository;

import br.senac.fullstack.model.AvaliacaoMatch;
import br.senac.fullstack.model.TipoAvaliadorMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AvaliacaoMatchRepository extends JpaRepository<AvaliacaoMatch, UUID> {

    List<AvaliacaoMatch> findByPropostaPocIdOrderByDataCriacaoAsc(UUID propostaPocId);

    List<AvaliacaoMatch> findByTermoPilotoIdOrderByDataCriacaoAsc(UUID termoPilotoId);

    Optional<AvaliacaoMatch> findByPropostaPocIdAndTipoAvaliador(UUID propostaPocId, TipoAvaliadorMatch tipoAvaliador);
}
