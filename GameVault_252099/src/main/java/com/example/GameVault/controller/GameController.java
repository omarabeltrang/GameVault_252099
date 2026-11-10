package com.example.GameVault.controller;

import com.example.GameVault.model.Juego;
import com.example.GameVault.model.Usuario;
import com.example.GameVault.service.JuegoService;
import com.example.GameVault.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class GameController {
    private final JuegoService juegoService;
    private final UsuarioService usuarioService;

    public GameController(JuegoService juegoService, UsuarioService usuarioService) {
        this.juegoService = juegoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/")
    public String inicio() { return "redirect:/juegos"; }

    @GetMapping("/juegos")
    public String listarJuegos(@RequestParam(value = "buscar", required = false) String buscar,
                               Model model) {
        model.addAttribute("juegos", juegoService.buscarPorTitulo(buscar));
        model.addAttribute("buscar", buscar);
        return "juegos";
    }

    @GetMapping("/mis-juegos")
    public String misJuegos(Model model) {
        Usuario usuario = usuarioService.obtenerUsuarioLogueado();
        if (usuario == null) return "redirect:/login";
        model.addAttribute("usuario", usuario);
        model.addAttribute("juegos", juegoService.obtenerJuegosPorUsuario(usuario.getId()));
        return "mis-juegos";
    }

    @GetMapping("/juegos/nuevo")
    public String nuevoJuego(Model model) {
        model.addAttribute("juego", new Juego());
        model.addAttribute("modo", "nuevo");
        return "formulario";
    }

    @PostMapping("/juegos/guardar")
    public String guardarJuego(@RequestParam String titulo,
                               @RequestParam String categoria,
                               @RequestParam Double precio,
                               @RequestParam String descripcion,
                               @RequestParam(value = "portada", required = false) MultipartFile portada) {
        Usuario usuario = usuarioService.obtenerUsuarioLogueado();
        if (usuario == null) return "redirect:/login";
        Juego nuevoJuego = new Juego();
        nuevoJuego.setTitulo(titulo);
        nuevoJuego.setCategoria(categoria);
        nuevoJuego.setPrecio(precio);
        nuevoJuego.setDescripcion(descripcion);
        nuevoJuego.setUsuario(usuario);
        juegoService.guardarJuego(nuevoJuego, portada);
        return "redirect:/mis-juegos";
    }

    @GetMapping("/juegos/editar/{id}")
    public String editarJuego(@PathVariable Long id, Model model) {
        Juego juego = juegoPropio(id);
        model.addAttribute("juego", juego);
        model.addAttribute("modo", "editar");
        return "formulario";
    }

    @PostMapping("/juegos/actualizar/{id}")
    public String actualizarJuego(@PathVariable Long id,
                                  @RequestParam String titulo,
                                  @RequestParam String categoria,
                                  @RequestParam Double precio,
                                  @RequestParam String descripcion,
                                  @RequestParam(value = "portada", required = false) MultipartFile portada) {
        Juego juego = juegoPropio(id);
        juego.setTitulo(titulo);
        juego.setCategoria(categoria);
        juego.setPrecio(precio);
        juego.setDescripcion(descripcion);
        juegoService.guardarJuego(juego, portada);
        return "redirect:/mis-juegos";
    }

    @PostMapping("/juegos/eliminar/{id}")
    public String eliminarJuego(@PathVariable Long id) {
        juegoPropio(id);
        juegoService.eliminarJuego(id);
        return "redirect:/juegos";
    }

    @GetMapping("/juegos/detalle/{id}")
    public String detalleJuego(@PathVariable Long id, Model model) {
        Juego juego = juegoService.obtenerJuegoPorId(id);
        if (juego == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        model.addAttribute("juego", juego);
        return "detalle-juego";
    }

    private Juego juegoPropio(Long id) {
        Usuario logueado = usuarioService.obtenerUsuarioLogueado();
        if (logueado == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        Juego juego = juegoService.obtenerJuegoPorId(id);
        if (juego == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (!juego.getUsuario().getId().equals(logueado.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes modificar otro juego");
        }
        return juego;
    }
}
