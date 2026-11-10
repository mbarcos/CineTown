package com.safa.cinetown.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("roles");

        authoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(

            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth
                        // =====================================
                        // AUTENTICACIÓN
                        // =====================================
                        .requestMatchers("/auth/**").permitAll()

                        // =====================================
                        // PELÍCULAS
                        // =====================================

                        // Recaudación: solo ADMIN
                        .requestMatchers(HttpMethod.GET,
                                "/peliculas/*/recaudacion")
                        .hasRole("ADMIN")

                        // Listado y detalle: público
                        .requestMatchers(HttpMethod.GET,
                                "/peliculas",
                                "/peliculas/*")
                        .permitAll()

                        // Alta de película: solo ADMIN
                        .requestMatchers(HttpMethod.POST,
                                "/peliculas")
                        .hasRole("ADMIN")

                        // Modificación de película: solo ADMIN
                        .requestMatchers(HttpMethod.PUT,
                                "/peliculas/*")
                        .hasRole("ADMIN")

                        // Modificar cartelera: solo ADMIN
                        .requestMatchers(HttpMethod.PATCH,
                                "/peliculas/*/cartelera")
                        .hasRole("ADMIN")


                        // =====================================
                        // GÉNEROS
                        // =====================================

                        // Consultar géneros: público
                        .requestMatchers(HttpMethod.GET,
                                "/generos")
                        .permitAll()


                        // =====================================
                        // SALAS
                        // =====================================

                        // Consultar salas: ADMIN y TAQUILLA
                        .requestMatchers(HttpMethod.GET,
                                "/salas")
                        .hasAnyRole("ADMIN", "TAQUILLA")


                        // =====================================
                        // SESIONES
                        // =====================================

                        // Programar sesión: solo ADMIN
                        .requestMatchers(HttpMethod.POST,
                                "/sesiones")
                        .hasRole("ADMIN")

                        // Consultar sesiones y sus detalles: público
                        .requestMatchers(HttpMethod.GET,
                                "/sesiones",
                                "/sesiones/*")
                        .permitAll()


                        // =====================================
                        // COMPRAS
                        // =====================================

                        // Realizar compra: CLIENTE y TAQUILLA
                        .requestMatchers(HttpMethod.POST,
                                "/compras")
                        .hasAnyRole("CLIENTE", "TAQUILLA")

                        // Consultar listado de compras:
                        // ADMIN, TAQUILLA y CLIENTE
                        .requestMatchers(HttpMethod.GET,
                                "/compras")
                        .hasAnyRole("ADMIN", "TAQUILLA", "CLIENTE")

                        // Consultar detalle de compra:
                        // ADMIN, TAQUILLA y CLIENTE
                        .requestMatchers(HttpMethod.GET,
                                "/compras/*")
                        .hasAnyRole("ADMIN", "TAQUILLA", "CLIENTE")

                        // Cancelar compra: TAQUILLA y CLIENTE
                        .requestMatchers(HttpMethod.PATCH,
                                "/compras/*/cancelar")
                        .hasAnyRole("TAQUILLA", "CLIENTE")


                        // =====================================
                        // INFORMES
                        // =====================================

                        // Ocupación: solo ADMIN
                        .requestMatchers(HttpMethod.GET,
                                "/informes/ocupacion")
                        .hasRole("ADMIN")

                        // Cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                );

        return http.build();
    }

}
