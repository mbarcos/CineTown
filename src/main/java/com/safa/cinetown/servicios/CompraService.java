package com.safa.cinetown.servicios;

import com.safa.cinetown.dtos.CompraResumen;
import com.safa.cinetown.excepciones.RecursoNoEncontradoException;
import com.safa.cinetown.repositorios.ICompraRepository;
import com.safa.cinetown.repositorios.IUsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CompraService {
    private final ICompraRepository compraRepository;
    private final IUsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Page<CompraResumen> consultarCompras(
            String nombreUsuario,
            boolean consultarTodas,
            Pageable pageable) {
        if (consultarTodas) {
            return compraRepository.encuentraTodasLasCompras(pageable);
        }

        Integer usuarioId = usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el usuario autenticado"))
                .getId();

        return compraRepository.encuentraComprasDeUsuario(usuarioId, pageable);
    }
}
