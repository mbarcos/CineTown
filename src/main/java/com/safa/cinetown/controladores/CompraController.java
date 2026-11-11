package com.safa.cinetown.controladores;

import com.safa.cinetown.dtos.CompraResumen;
import com.safa.cinetown.dtos.CompraResponse;
import com.safa.cinetown.dtos.request.CompraRequest;
import com.safa.cinetown.servicios.CompraService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;

@RestController
@RequestMapping("/compras")
@AllArgsConstructor
public class CompraController {
    private final CompraService service;

    @PostMapping
    public CompraResponse comprarEntradas(
            @Valid @RequestBody CompraRequest request,
            Authentication authentication) {
        boolean esCliente = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_CLIENTE"));
        String nombreUsuario = authentication == null ? null : authentication.getName();
        return service.comprarEntradas(request, nombreUsuario, esCliente);
    }

    @GetMapping
    public Page<CompraResumen> consultarCompras(
            Authentication authentication,
            @PageableDefault(size = 10) Pageable pageable) {
        boolean consultarTodas = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ROLE_TAQUILLA"));

        return service.consultarCompras(
                authentication.getName(), consultarTodas, pageable);
    }

    @GetMapping("/{codigo}")
    public com.safa.cinetown.dtos.DetalleCompra consultarCompra(
            @PathVariable String codigo,
            Authentication authentication) {
        boolean consultarTodas = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ROLE_TAQUILLA"));

        return service.consultarCompra(
                codigo, authentication.getName(), consultarTodas);
    }

    @PatchMapping("/{codigo}/cancelar")
    public void cancelarCompra(
            @PathVariable String codigo,
            Authentication authentication) {
        boolean esTaquilla = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_TAQUILLA"));

        service.cancelarCompra(codigo, authentication.getName(), esTaquilla);
    }
}
