package br.senac.fullstack.repository;

import br.senac.fullstack.model.Mentor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MentorRepository extends JpaRepository<Mentor, UUID> {
    java.util.Optional<Mentor> findByEmail(String email);
}
