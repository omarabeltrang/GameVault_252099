package com.example.GameVault.service;

import com.example.GameVault.model.Usuario;
import com.example.GameVault.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final HttpSession session;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          HttpSession session) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.session = session;
    }

    public Usuario registrarUsuario(Usuario usuario) {
        String email = usuario.getEmail().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Este correo ya está registrado");
        }
        usuario.setEmail(email);
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        return usuarioRepository.save(usuario);
    }

    /** Devuelve el usuario si las credenciales son correctas; de lo contrario, null. */
    public Usuario autenticarUsuario(String email, String password) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email.trim().toLowerCase());
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (passwordEncoder.matches(password, usuario.getContrasena())) {
                return usuario;
            }
        }
        return null;
    }

    public Usuario obtenerUsuarioLogueado() {
        Object usuarioId = session.getAttribute("usuario_id");
        if (usuarioId instanceof Long id) {
            return usuarioRepository.findById(id).orElse(null);
        }
        return null;
    }
}
