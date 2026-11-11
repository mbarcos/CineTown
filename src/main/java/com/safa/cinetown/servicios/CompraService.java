package com.safa.cinetown.servicios;

import com.safa.cinetown.conversores.CompraMapper;
import com.safa.cinetown.dtos.CompraResumen;
import com.safa.cinetown.dtos.CompraResponse;
import com.safa.cinetown.dtos.DetalleCompra;
import com.safa.cinetown.dtos.request.CompraRequest;
import com.safa.cinetown.excepciones.AsientosInsuficientesException;
import com.safa.cinetown.excepciones.RecursoNoEncontradoException;
import com.safa.cinetown.excepciones.ReglaNegocioException;
import com.safa.cinetown.modelos.Compra;
import com.safa.cinetown.modelos.Sesion;
import com.safa.cinetown.modelos.Usuario;
import com.safa.cinetown.repositorios.ICompraRepository;
import com.safa.cinetown.repositorios.ISesionRepository;
import com.safa.cinetown.repositorios.IUsuarioRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@AllArgsConstructor
@Validated
public class CompraService {
    private static final String SESION_NO_ENCONTRADA = "No se encontró la sesión";
    private static final String COMPRA_NO_ENCONTRADA = "No se encontró la compra";
    private static final String USUARIO_NO_ENCONTRADO =
            "No se encontró el usuario autenticado";
    private static final int LONGITUD_CODIGO_COMPRA = 8;
    private static final int HORAS_LIMITE_CANCELACION = 1;
    private static final String CARACTERES_CODIGO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final BigDecimal MULTIPLICADOR_PRECIO_MIERCOLES =
            new BigDecimal("0.70");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ICompraRepository compraRepository;
    private final IUsuarioRepository usuarioRepository;
    private final ISesionRepository sesionRepository;
    private final CompraMapper mapper;

    @Transactional
    public CompraResponse comprarEntradas(
            @NotNull(message = "La compra es obligatoria") @Valid CompraRequest request,
            String nombreUsuario,
            boolean asociarUsuario) {
        Sesion sesion = obtenerSesion(request.sesionId());
        validarSesionAptaParaCompra(sesion);
        validarAsientosDisponibles(sesion, request.numEntradas());

        Compra compra = crearCompra(request, sesion, nombreUsuario, asociarUsuario);
        return mapper.aRespuesta(compraRepository.save(compra));
    }

    private Sesion obtenerSesion(Integer sesionId) {
        return sesionRepository.encuentraPorIdParaActualizar(sesionId)
                .orElseThrow(() -> new RecursoNoEncontradoException(SESION_NO_ENCONTRADA));
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
        compra.setPrecioTotal(calcularPrecioTotal(sesion, request.numEntradas()));
        compra.setCodCompra(generarCodigoUnico());
        if (asociarUsuario) {
            compra.setUsuario(buscarUsuarioAutenticado(nombreUsuario));
        }
        return compra;
    }

    private BigDecimal calcularPrecioTotal(Sesion sesion, int numEntradas) {
        BigDecimal precioUnitario = sesion.getPrecioBase();
        if (sesion.getFechaHoraInicio().getDayOfWeek() == DayOfWeek.WEDNESDAY) {
            precioUnitario = precioUnitario
                    .multiply(MULTIPLICADOR_PRECIO_MIERCOLES)
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return precioUnitario.multiply(BigDecimal.valueOf(numEntradas));
    }

    private Usuario buscarUsuarioAutenticado(String nombreUsuario) {
        return usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException(USUARIO_NO_ENCONTRADO));
    }

    private String generarCodigoUnico() {
        String codigo;
        do {
            StringBuilder resultado = new StringBuilder(LONGITUD_CODIGO_COMPRA);
            for (int i = 0; i < LONGITUD_CODIGO_COMPRA; i++) {
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

        Integer usuarioId = buscarUsuarioAutenticado(nombreUsuario).getId();

        return compraRepository.encuentraComprasDeUsuario(usuarioId, pageable);
    }

    @Transactional(readOnly = true)
    public DetalleCompra consultarCompra(
            String codigo,
            String nombreUsuario,
            boolean consultarTodas) {
        if (!consultarTodas) {
            Integer usuarioId = buscarUsuarioAutenticado(nombreUsuario).getId();

            if (!compraRepository.existsByCodCompraAndUsuario_Id(codigo, usuarioId)) {
                throw new RecursoNoEncontradoException(COMPRA_NO_ENCONTRADA);
            }
        }

        return compraRepository.encuentraDetalleCompraPorCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException(COMPRA_NO_ENCONTRADA));
    }

    @Transactional
    public void cancelarCompra(String codigo, String nombreUsuario, boolean consultarTodas) {
        Compra compra = compraRepository.encuentraPorCodigoParaActualizar(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException(COMPRA_NO_ENCONTRADA));

        if (!consultarTodas) {
            Integer usuarioId = buscarUsuarioAutenticado(nombreUsuario).getId();
            if (compra.getUsuario() == null
                    || !Objects.equals(compra.getUsuario().getId(), usuarioId)) {
                throw new RecursoNoEncontradoException(COMPRA_NO_ENCONTRADA);
            }
        }

        validarCancelacionPermitida(compra);
        compra.setEstado(false);
        compraRepository.save(compra);
    }

    private void validarCancelacionPermitida(Compra compra) {
        if (Boolean.FALSE.equals(compra.getEstado())) {
            throw new ReglaNegocioException("La compra ya está cancelada");
        }
        if (Boolean.TRUE.equals(compra.getParkingUsado())) {
            throw new ReglaNegocioException(
                    "No se puede cancelar una compra cuyo parking ya se ha canjeado");
        }

        LocalDateTime limiteCancelacion = compra.getSesion().getFechaHoraInicio()
                .minusHours(HORAS_LIMITE_CANCELACION);
        if (LocalDateTime.now().isAfter(limiteCancelacion)) {
            throw new ReglaNegocioException(
                    "La compra solo se puede cancelar hasta una hora antes de la sesión");
        }
    }
}
