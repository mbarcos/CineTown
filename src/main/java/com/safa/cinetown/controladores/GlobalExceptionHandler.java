package com.safa.cinetown.controladores;

import com.safa.cinetown.excepciones.ConflictoException;
import com.safa.cinetown.excepciones.AsientosInsuficientesException;
import com.safa.cinetown.excepciones.ReglaNegocioException;
import com.safa.cinetown.excepciones.RecursoNoEncontradoException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Pattern CAMPO_JSON = Pattern.compile("\\[\\\"([^\\\"]+)\\\"\\]");

    @ExceptionHandler(AsientosInsuficientesException.class)
    public ResponseEntity<?> handleAsientosInsuficientes(
            AsientosInsuficientesException ex) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<?> handleRecursoNoEncontrado(
            RecursoNoEncontradoException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<?> handleConflicto(ConflictoException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<?> handleReglaNegocio(ReglaNegocioException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(
            BadCredentialsException ex) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "error",
                        "Nombre de usuario o contraseña incorrectos"
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errores.put(
                        error.getField(),
                        "El campo '" + error.getField() + "' es incorrecto: "
                                + error.getDefaultMessage()));

        ProblemDetail problema = construirProblema(
                HttpStatus.BAD_REQUEST,
                "Datos no válidos",
                "Revisa los campos indicados");
        problema.setProperty("errores", errores);
        return ResponseEntity.badRequest().body(problema);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(
            ConstraintViolationException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violacion -> {
            String ruta = violacion.getPropertyPath().toString();
            int ultimoSeparador = ruta.lastIndexOf('.');
            String campo = ruta;
            if (ultimoSeparador >= 0) {
                campo = ruta.substring(ultimoSeparador + 1);
            }
            errores.put(campo, "El campo '" + campo + "' es incorrecto: "
                    + violacion.getMessage());
        });

        ProblemDetail problema = construirProblema(
                HttpStatus.BAD_REQUEST,
                "Datos no válidos",
                "Revisa los campos indicados");
        problema.setProperty("errores", errores);
        return ResponseEntity.badRequest().body(problema);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleJsonNoLegible(
            HttpMessageNotReadableException ex) {
        String campo = encontrarCampoConError(ex);
        String mensaje = campo.equals("body")
                ? "El JSON no tiene un formato válido o contiene valores no permitidos"
                : "El campo '" + campo + "' es incorrecto";

        ProblemDetail problema = construirProblema(
                HttpStatus.BAD_REQUEST,
                "Datos no válidos",
                "Revisa los campos indicados");
        problema.setProperty("errores", Map.of(campo, mensaje));
        return ResponseEntity.badRequest().body(problema);
    }

    private String encontrarCampoConError(HttpMessageNotReadableException ex) {
        Throwable causa = ex.getMostSpecificCause();
        String mensaje = causa.getMessage();
        if (mensaje == null) {
            return "body";
        }

        Matcher matcher = CAMPO_JSON.matcher(mensaje);
        String campoEncontrado = null;
        while (matcher.find()) {
            campoEncontrado = matcher.group(1);
        }
        if (campoEncontrado == null) {
            return "body";
        }
        return campoEncontrado;
    }

    private ProblemDetail construirProblema(
            HttpStatus estado,
            String titulo,
            String detalle) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, detalle);
        problema.setTitle(titulo);
        problema.setProperty("timestamp", LocalDateTime.now());
        return problema;
    }
}
