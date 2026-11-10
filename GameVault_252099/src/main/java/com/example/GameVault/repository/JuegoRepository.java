package com.example.GameVault.repository;

import com.example.GameVault.model.Juego;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JuegoRepository extends JpaRepository<Juego, Long> {
    List<Juego> findByTituloContainingIgnoreCase(String titulo);
    List<Juego> findByUsuarioId(Long usuarioId);
}
