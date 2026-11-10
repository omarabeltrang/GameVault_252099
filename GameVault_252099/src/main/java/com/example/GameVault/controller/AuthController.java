package com.example.GameVault.controller;

import com.example.GameVault.model.Usuario;
import com.example.GameVault.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String mostrarLogin(@RequestParam(value = "registrado", required = false) String registrado,
                               Model model) {
        if (registrado != null) model.addAttribute("mensaje", "Registro exitoso. Inicia sesión.");
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam("email") String email,
                                @RequestParam("password") String password,
                                HttpServletRequest request,
                                Model model) {
        Usuario usuario = usuarioService.autenticarUsuario(email, password);
        if (usuario != null) {
            HttpSession session = request.getSession(true);
            request.changeSessionId(); // Protege contra fijación de sesión.
            session.setAttribute("usuario_id", usuario.getId());
            session.setAttribute("mensaje", "Bienvenido, " + usuario.getNombre());
            return "redirect:/mis-juegos";
        }
        model.addAttribute("error", "Credenciales inválidas. Inténtalo de nuevo.");
        return "login";
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@Valid Usuario usuario,
                                   BindingResult result, Model model) {
        if (result.hasErrors()) return "registro";
        try {
            usuarioService.registrarUsuario(usuario);
            return "redirect:/login?registrado=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "registro";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
