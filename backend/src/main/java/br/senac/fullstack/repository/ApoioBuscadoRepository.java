package br.senac.fullstack.repository;

import br.senac.fullstack.model.ApoioBuscado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApoioBuscadoRepository extends JpaRepository<ApoioBuscado, UUID> {
    List<ApoioBuscado> findByStartupId(UUID startupId);
}
