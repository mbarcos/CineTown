package com.safa.cinetown.seguridad;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;

import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class JwtService {
    private final JwtEncoder jwtEncoder;

    public String generarToken(
            String nombreUsuario,
            List<String> roles) {

        Instant ahora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(nombreUsuario)
                .issuedAt(ahora)
                .expiresAt(ahora.plus(1, ChronoUnit.HOURS))
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }
}
