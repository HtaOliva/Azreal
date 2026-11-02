package br.senac.fullstack.repository;

import br.senac.fullstack.model.AmbienteValidacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AmbienteValidacaoRepository extends JpaRepository<AmbienteValidacao, UUID> {
    Optional<AmbienteValidacao> findByEmailContato(String emailContato);
}
