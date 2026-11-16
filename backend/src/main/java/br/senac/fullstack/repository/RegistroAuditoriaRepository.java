package br.senac.fullstack.repository;

import br.senac.fullstack.model.RegistroAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RegistroAuditoriaRepository extends JpaRepository<RegistroAuditoria, UUID> {

    List<RegistroAuditoria> findByPropostaPocIdOrderByDataHoraAsc(UUID propostaPocId);

    List<RegistroAuditoria> findByTermoPilotoIdOrderByDataHoraAsc(UUID termoPilotoId);
}
