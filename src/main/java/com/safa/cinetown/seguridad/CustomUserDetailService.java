package com.safa.cinetown.seguridad;

import com.safa.cinetown.modelos.Usuario;
import com.safa.cinetown.repositorios.IUsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@AllArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final IUsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String nombreUsuario)
            throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuario no encontrado"
                        )
                );

        return org.springframework.security.core.userdetails.User
                .withUsername(usuario.getNombreUsuario())
                .password(usuario.getPassword())
                .authorities(
                        "ROLE_" + usuario.getRol().name().toUpperCase(Locale.ROOT)
                ).build();
    }
}
