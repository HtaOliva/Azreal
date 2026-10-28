package br.senac.fullstack.repository;

import br.senac.fullstack.model.RegraPlataforma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RegraPlataformaRepository extends JpaRepository<RegraPlataforma, UUID> {
    List<RegraPlataforma> findByAtivoTrue();
}