package com.example.GameVault.service;

import com.example.GameVault.model.Juego;
import com.example.GameVault.repository.JuegoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class JuegoService {
    private static final Set<String> IMAGENES_PERMITIDAS =
            Set.of("image/png", "image/jpeg", "image/webp");

    private final JuegoRepository juegoRepository;
    private final Path uploadDir;

    public JuegoService(JuegoRepository juegoRepository,
                        @Value("${app.upload-dir:uploads}") String uploadDir) {
        this.juegoRepository = juegoRepository;
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public List<Juego> listarTodos() {
        return juegoRepository.findAll();
    }

    public Juego obtenerJuegoPorId(Long id) {
        return juegoRepository.findById(id).orElse(null);
    }

    public void eliminarJuego(Long id) {
        juegoRepository.deleteById(id);
    }

    public List<Juego> buscarPorTitulo(String palabra) {
        if (palabra == null || palabra.isBlank()) {
            return listarTodos();
        }
        return juegoRepository.findByTituloContainingIgnoreCase(palabra);
    }

    public List<Juego> obtenerJuegosPorUsuario(Long usuarioId) {
        return juegoRepository.findByUsuarioId(usuarioId);
    }

    public void guardarJuego(Juego juego, MultipartFile portada) {
        if (portada != null && !portada.isEmpty()) {
            if (!IMAGENES_PERMITIDAS.contains(portada.getContentType())) {
                throw new IllegalArgumentException("La portada debe ser PNG, JPG o WEBP");
            }
            if (portada.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("La imagen supera el límite de 5 MB");
            }
            try {
                Files.createDirectories(uploadDir);
                String extension = switch (portada.getContentType()) {
                    case "image/png" -> ".png";
                    case "image/webp" -> ".webp";
                    default -> ".jpg";
                };
                String nombreArchivo = UUID.randomUUID() + extension;
                Path destino = uploadDir.resolve(nombreArchivo).normalize();
                if (!destino.startsWith(uploadDir)) {
                    throw new IllegalArgumentException("Ruta de archivo no válida");
                }
                try (InputStream entrada = portada.getInputStream()) {
                    Files.copy(entrada, destino, StandardCopyOption.REPLACE_EXISTING);
                }
                juego.setPortadaUrl("/uploads/" + nombreArchivo);
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo guardar la portada", e);
            }
        }
        juegoRepository.save(juego);
    }
}
