package br.senac.fullstack.repository;

import br.senac.fullstack.model.AnotacaoMentoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnotacaoMentoriaRepository extends JpaRepository<AnotacaoMentoria, UUID> {
    List<AnotacaoMentoria> findByStartupId(UUID startupId);
    List<AnotacaoMentoria> findByMentorId(UUID mentorId);
}
