package br.senac.fullstack.repository;

import br.senac.fullstack.model.Tema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TemaRepository extends JpaRepository<Tema, UUID> {
    List<Tema> findByAtivoTrue();
}