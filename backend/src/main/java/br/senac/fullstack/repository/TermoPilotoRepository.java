package br.senac.fullstack.repository;

import br.senac.fullstack.model.TermoPiloto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TermoPilotoRepository extends JpaRepository<TermoPiloto, UUID> {

    Optional<TermoPiloto> findByPropostaPocId(UUID propostaPocId);
}
