package com.safa.cinetown.integraciones.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.safa.cinetown.config.TmdbProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
public class TmdbClient {
    private static final Pattern MARCAS_DIACRITICAS = Pattern.compile("\\p{M}+");
    private static final Pattern ESPACIOS = Pattern.compile("\\s+");
    private static final Pattern RUTA_CARTEL = Pattern.compile("/[a-zA-Z0-9_-]+\\.(jpg|jpeg|png|webp)");

    private final RestClient restClient;
    private final TmdbProperties properties;

    public Optional<String> buscarCartel(String titulo, int anio, String sinopsis) {
        String token = StringUtils.hasText(properties.token()) ? properties.token().strip() : null;
        Optional<String> apiKey = Optional.ofNullable(properties.apiKey())
                .filter(StringUtils::hasText)
                .map(String::strip)
                .filter(clave -> token == null);
        if (token == null && apiKey.isEmpty()) {
            return Optional.empty();
        }

        try {
            ResultadoBusqueda busqueda = restClient.get()
                    .uri(uri -> uri.path("/search/movie")
                            .queryParam("query", "{titulo}")
                            .queryParam("primary_release_year", anio)
                            .queryParam("language", "es-ES")
                            .queryParamIfPresent("api_key", apiKey.map(clave -> "{apiKey}"))
                            .build(Map.of("titulo", titulo, "apiKey", apiKey.orElse(""))))
                    .headers(headers -> {
                        if (token != null) {
                            headers.setBearerAuth(token);
                        }
                    })
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(ResultadoBusqueda.class);
            return seleccionarCartel(busqueda, titulo, anio, sinopsis);
        } catch (RestClientResponseException ex) {
            log.warn("TMDB rechazó la búsqueda de cartel, estado HTTP {}", ex.getStatusCode().value());
        } catch (RestClientException ex) {
            log.warn("No se pudo consultar el cartel en TMDB ({})", ex.getClass().getSimpleName());
        }
        return Optional.empty();
    }

    private Optional<String> seleccionarCartel(ResultadoBusqueda busqueda, String titulo, int anio, String sinopsis) {
        if (busqueda == null || busqueda.resultados() == null || busqueda.totalPaginas() > 1) {
            return Optional.empty();
        }

        String tituloNormalizado = normalizarTexto(titulo);
        List<PeliculaTmdb> coincidencias = busqueda.resultados().stream()
                .filter(pelicula -> pelicula.fechaEstreno() != null
                        && pelicula.fechaEstreno().startsWith(anio + "-"))
                .filter(pelicula -> tituloNormalizado.equals(normalizarTexto(pelicula.titulo()))
                        || tituloNormalizado.equals(normalizarTexto(pelicula.tituloOriginal())))
                .toList();

        if (coincidencias.size() > 1 && StringUtils.hasText(sinopsis)) {
            String sinopsisNormalizada = normalizarTexto(sinopsis);
            coincidencias = coincidencias.stream()
                    .filter(pelicula -> sinopsisNormalizada.equals(normalizarTexto(pelicula.sinopsis())))
                    .toList();
        }

        if (coincidencias.size() != 1) {
            return Optional.empty();
        }

        return Optional.ofNullable(coincidencias.getFirst().rutaCartel())
                .filter(ruta -> RUTA_CARTEL.matcher(ruta).matches())
                .map(ruta -> properties.cartelesUrl() + ruta);
    }

    private static String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        String descompuesto = Normalizer.normalize(texto, Normalizer.Form.NFD);
        String sinAcentos = MARCAS_DIACRITICAS.matcher(descompuesto).replaceAll("");
        return ESPACIOS.matcher(sinAcentos.strip()).replaceAll(" ").toLowerCase(Locale.ROOT);
    }

    private record ResultadoBusqueda(
            @JsonProperty("results") List<PeliculaTmdb> resultados,
            @JsonProperty("total_pages") int totalPaginas
    ) {
    }

    private record PeliculaTmdb(
            @JsonProperty("title") String titulo,
            @JsonProperty("original_title") String tituloOriginal,
            @JsonProperty("release_date") String fechaEstreno,
            @JsonProperty("overview") String sinopsis,
            @JsonProperty("poster_path") String rutaCartel
    ) {
    }
}
