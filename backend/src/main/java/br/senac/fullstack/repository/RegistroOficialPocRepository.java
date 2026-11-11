package br.senac.fullstack.repository;

import br.senac.fullstack.model.RegistroOficialPoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegistroOficialPocRepository extends JpaRepository<RegistroOficialPoc, UUID> {

    List<RegistroOficialPoc> findByStartupIdOrderByDataEmissaoDesc(UUID startupId);

    Optional<RegistroOficialPoc> findByCodigoVerificador(String codigoVerificador);

    Optional<RegistroOficialPoc> findByTermoPilotoId(UUID termoPilotoId);
}
