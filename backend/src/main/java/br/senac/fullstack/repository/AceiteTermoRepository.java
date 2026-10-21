package br.senac.fullstack.repository;

import br.senac.fullstack.model.AceiteTermo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AceiteTermoRepository extends JpaRepository<AceiteTermo, UUID> {
}
