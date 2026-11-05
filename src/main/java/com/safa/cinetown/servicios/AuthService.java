package com.safa.cinetown.servicios;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import com.safa.cinetown.dtos.AuthResponse;
import com.safa.cinetown.dtos.request.LoginRequest;
import com.safa.cinetown.dtos.request.RegistroRequest;

import com.safa.cinetown.modelos.Usuario;
import com.safa.cinetown.modelos.RolUsuario;

import com.safa.cinetown.repositorios.IUsuarioRepository;

import com.safa.cinetown.seguridad.JwtService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class AuthService {
    private final IUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse registrar(RegistroRequest request) {

        String nombreUsuario = request.nombreUsuario().trim();

        if (usuarioRepository.existsByNombreUsuario(nombreUsuario)) {
            throw new IllegalArgumentException(
                    "El nombre de usuario ya está registrado"
            );
        }

        // Crear usuario
        Usuario usuario = new Usuario();

        usuario.setNombreUsuario(nombreUsuario);

        // Cifrar contraseña
        usuario.setPassword(
                passwordEncoder.encode(request.password())
        );

        // Todos los registros nuevos son clientes
        usuario.setRol(RolUsuario.cliente);

        usuarioRepository.save(usuario);

        // Generar JWT
        List<String> roles = List.of("ROLE_CLIENTE");

        String token = jwtService.generarToken(
                usuario.getNombreUsuario(),
                roles
        );

        return new AuthResponse(
                token,
                "Bearer",
                usuario.getNombreUsuario(),
                roles
        );
    }

    public AuthResponse login(LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.nombreUsuario(),
                                request.password()
                        )
                );

        // Obtener los roles del usuario autenticado
        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .toList();

        // Generar JWT
        String token = jwtService.generarToken(
                authentication.getName(),
                roles
        );

        return new AuthResponse(
                token,
                "Bearer",
                authentication.getName(),
                roles
        );
    }


}
