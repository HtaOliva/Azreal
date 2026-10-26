package br.senac.fullstack.repository;

import br.senac.fullstack.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CursoRepository extends JpaRepository<Curso, UUID> {
    List<Curso> findByAtivoTrue();
}
