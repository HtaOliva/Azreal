package br.senac.fullstack.repository;

import br.senac.fullstack.model.VinculoMentoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VinculoMentoriaRepository extends JpaRepository<VinculoMentoria, UUID> {
    List<VinculoMentoria> findByStartupId(UUID startupId);
    List<VinculoMentoria> findByMentorId(UUID mentorId);
}
