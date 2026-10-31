package br.senac.fullstack.repository;

import br.senac.fullstack.model.Incubadora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncubadoraRepository extends JpaRepository<Incubadora, UUID> {
    List<Incubadora> findByAtivoTrue();
}
