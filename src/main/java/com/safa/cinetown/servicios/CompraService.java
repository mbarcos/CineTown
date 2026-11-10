package com.safa.cinetown.servicios;

import com.safa.cinetown.dtos.CompraResumen;
import com.safa.cinetown.dtos.CompraResponse;
import com.safa.cinetown.dtos.DetalleCompra;
import com.safa.cinetown.dtos.request.CompraRequest;
import com.safa.cinetown.conversores.CompraMapper;
import com.safa.cinetown.excepciones.AsientosInsuficientesException;
import com.safa.cinetown.excepciones.RecursoNoEncontradoException;
import com.safa.cinetown.excepciones.ReglaNegocioException;
import com.safa.cinetown.modelos.Compra;
import com.safa.cinetown.modelos.Sesion;
import com.safa.cinetown.modelos.Usuario;
import com.safa.cinetown.repositorios.ICompraRepository;
import com.safa.cinetown.repositorios.ISesionRepository;
import com.safa.cinetown.repositorios.IUsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.security.SecureRandom;

@Service
@AllArgsConstructor
public class CompraService {
    private final ICompraRepository compraRepository;
    private final IUsuarioRepository usuarioRepository;
    private final ISesionRepository sesionRepository;
    private final CompraMapper mapper;

    private static final String CARACTERES_CODIGO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public CompraResponse comprarEntradas(
            CompraRequest request, String nombreUsuario, boolean asociarUsuario) {
        Sesion sesion = obtenerSesion(request.sesionId());
        validarSesionAptaParaCompra(sesion);
        validarAsientosDisponibles(sesion, request.numEntradas());

        Compra compra = crearCompra(request, sesion, nombreUsuario, asociarUsuario);
        return mapper.aRespuesta(compraRepository.save(compra));
    }

    private Sesion obtenerSesion(Integer sesionId) {
        return sesionRepository.encuentraPorIdParaActualizar(sesionId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la sesión"));
    }

    private void validarSesionAptaParaCompra(Sesion sesion) {
        if (!sesion.getFechaHoraInicio().isAfter(LocalDateTime.now())) {
            throw new ReglaNegocioException(
                    "No se pueden comprar entradas para una sesión que ya ha comenzado");
        }
    }

    private void validarAsientosDisponibles(Sesion sesion, Integer entradasSolicitadas) {
        long entradasVendidas = compraRepository
                .cuentaEntradasActivasPorSesion(sesion.getId());
        long entradasDisponibles = sesion.getSala().getCapacidad() - entradasVendidas;

        if (entradasSolicitadas > entradasDisponibles) {
            throw new AsientosInsuficientesException(
                    "No hay suficientes asientos disponibles para la sesión");
        }
    }

    private Compra crearCompra(
            CompraRequest request,
            Sesion sesion,
            String nombreUsuario,
            boolean asociarUsuario) {
        Compra compra = mapper.aEntidad(request);
        compra.setSesion(sesion);
        compra.setPrecioTotal(sesion.getPrecioBase()
                .multiply(BigDecimal.valueOf(request.numEntradas())));
        compra.setCodCompra(generarCodigoUnico());
        if (asociarUsuario) {
            compra.setUsuario(obtenerUsuario(nombreUsuario));
        }
        return compra;
    }

    private Usuario obtenerUsuario(String nombreUsuario) {
        return usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el usuario autenticado"));
    }

    private String generarCodigoUnico() {
        String codigo;
        do {
            StringBuilder resultado = new StringBuilder(8);
            for (int i = 0; i < 8; i++) {
                resultado.append(CARACTERES_CODIGO.charAt(
                        RANDOM.nextInt(CARACTERES_CODIGO.length())));
            }
            codigo = resultado.toString();
        } while (compraRepository.existsByCodCompra(codigo));
        return codigo;
    }

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

    @Transactional(readOnly = true)
    public DetalleCompra consultarCompra(
            String codigo,
            String nombreUsuario,
            boolean consultarTodas) {
        if (!consultarTodas) {
            Integer usuarioId = usuarioRepository.findByNombreUsuario(nombreUsuario)
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "No se encontró el usuario autenticado"))
                    .getId();

            if (!compraRepository.existsByCodCompraAndUsuario_Id(codigo, usuarioId)) {
                throw new RecursoNoEncontradoException("No se encontró la compra");
            }
        }

        return compraRepository.encuentraDetalleCompraPorCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la compra"));
    }
}
