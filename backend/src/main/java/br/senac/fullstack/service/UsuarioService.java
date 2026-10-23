package br.senac.fullstack.service;

import br.senac.fullstack.dto.UsuarioRequestDTO;
import br.senac.fullstack.dto.UsuarioResponseDTO;
import br.senac.fullstack.dto.UsuarioUpdateDTO;
import br.senac.fullstack.model.Usuario;
import br.senac.fullstack.repository.UsuarioRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    private final ObjectProvider<UsuarioRepository> usuarioRepositoryProvider;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(ObjectProvider<UsuarioRepository> usuarioRepositoryProvider, PasswordEncoder passwordEncoder) {
        this.usuarioRepositoryProvider = usuarioRepositoryProvider;
        this.passwordEncoder = passwordEncoder;
    }

    private UsuarioRepository usuarioRepository() {
        return usuarioRepositoryProvider.getIfAvailable();
    }

    public List<UsuarioResponseDTO> listar() {
        UsuarioRepository repository = usuarioRepository();
        if (repository == null) {
            return List.of();
        }
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public UsuarioResponseDTO buscarPorId(Long id) {
        UsuarioRepository repository = usuarioRepository();
        if (repository == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Repositorio de usuarios indisponivel");
        }
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado"));
        return toResponse(usuario);
    }

    public UsuarioResponseDTO salvar(UsuarioRequestDTO request) {
        UsuarioRepository repository = usuarioRepository();
        if (repository == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Repositorio de usuarios indisponivel");
        }
        if (repository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ja cadastrado");
        }
        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        Usuario salvo = repository.save(usuario);
        return toResponse(salvo);
    }

    public UsuarioResponseDTO atualizar(Long id, UsuarioUpdateDTO request) {
        UsuarioRepository repository = usuarioRepository();
        if (repository == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Repositorio de usuarios indisponivel");
        }
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado"));

        if (request.email() != null && !request.email().equals(usuario.getEmail())
                && repository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ja cadastrado");
        }

        if (request.nome() != null) {
            usuario.setNome(request.nome());
        }
        if (request.email() != null) {
            usuario.setEmail(request.email());
        }
        if (request.senha() != null && !request.senha().isBlank()) {
            usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        }
        Usuario salvo = repository.save(usuario);
        return toResponse(salvo);
    }

    public UsuarioResponseDTO definirAtivo(Long id, boolean ativo) {
        UsuarioRepository repository = usuarioRepository();
        if (repository == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Repositorio de usuarios indisponivel");
        }
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado"));
        usuario.setAtivo(ativo);
        Usuario salvo = repository.save(usuario);
        return toResponse(salvo);
    }

    public void deletar(Long id) {
        UsuarioRepository repository = usuarioRepository();
        if (repository == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Repositorio de usuarios indisponivel");
        }
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado");
        }
        repository.deleteById(id);
    }

    private UsuarioResponseDTO toResponse(Usuario usuario) {
        return new UsuarioResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}
