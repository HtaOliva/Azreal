package br.senac.fullstack.repository;

import br.senac.fullstack.model.Certificado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CertificadoRepository extends JpaRepository<Certificado, UUID> {
    List<Certificado> findByStartupId(UUID startupId);
}
