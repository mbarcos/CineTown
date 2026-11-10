package com.safa.cinetown.controladores;

import com.safa.cinetown.dtos.CompraResumen;
import com.safa.cinetown.servicios.CompraService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/compras")
@AllArgsConstructor
public class CompraController {
    private final CompraService service;

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
}
