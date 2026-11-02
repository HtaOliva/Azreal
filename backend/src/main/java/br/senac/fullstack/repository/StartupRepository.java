package br.senac.fullstack.repository;

import br.senac.fullstack.model.Startup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StartupRepository extends JpaRepository<Startup, UUID> { /* empty */ }
