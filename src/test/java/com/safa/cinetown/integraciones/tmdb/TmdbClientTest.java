package com.safa.cinetown.integraciones.tmdb;

import com.safa.cinetown.config.TmdbProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TmdbClientTest {
    private static final String API_URL = "https://api.themoviedb.org/3";
    private static final String CARTELES_URL = "https://image.tmdb.org/t/p/w500";
    private static final String BUSQUEDA = API_URL
            + "/search/movie?query=Amelie%20%26%20Co%2B&primary_release_year=2001&language=es-ES";

    private MockRestServiceServer server;
    private RestClient restClient;

    @BeforeEach
    void configurarCliente() {
        RestClient.Builder builder = RestClient.builder().baseUrl(API_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        restClient = builder.build();
    }

    @Test
    void seleccionaUnaCoincidenciaPorTituloOriginalYAnio() {
        server.expect(requestTo(BUSQUEDA))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token-de-prueba"))
                .andRespond(withSuccess("""
                        {"total_pages":1,"results":[
                          {"title":"Amelie & Co+","release_date":"2020-01-01","poster_path":"/otra.jpg"},
                          {"title":"Amelie & Co+ 2","release_date":"2001-01-01","poster_path":"/secuela.jpg"},
                          {"title":"Título traducido","original_title":"Amélie & Co+",
                           "release_date":"2001-04-25","poster_path":"/cartel.jpg","id":42}
                        ]}
                        """, MediaType.APPLICATION_JSON));

        assertThat(cliente("token-de-prueba").buscarCartel("Amelie & Co+", 2001, null))
                .contains(CARTELES_URL + "/cartel.jpg");
        server.verify();
    }

    @Test
    void descartaCoincidenciasAmbiguasAunqueSoloUnaTengaCartel() {
        server.expect(requestTo(BUSQUEDA))
                .andRespond(withSuccess("""
                        {"total_pages":1,"results":[
                          {"title":"Amelie & Co+","release_date":"2001-01-01","poster_path":"/cartel.jpg"},
                          {"title":"Amelie & Co+","release_date":"2001-04-25","poster_path":null}
                        ]}
                        """, MediaType.APPLICATION_JSON));

        assertThat(cliente("token-de-prueba").buscarCartel("Amelie & Co+", 2001, null)).isEmpty();
        server.verify();
    }

    @Test
    void distinguePeliculasHomonimasPorLaSinopsisCompleta() {
        server.expect(requestTo(BUSQUEDA))
                .andRespond(withSuccess("""
                        {"total_pages":1,"results":[
                          {"title":"Amelie & Co+","release_date":"2001-01-01",
                           "overview":"Una mujer parte de viaje.","poster_path":"/otra.jpg"},
                          {"title":"Amelie & Co+","release_date":"2001-04-25",
                           "overview":"Una mujer vuelve a París.","poster_path":"/cartel.jpg"}
                        ]}
                        """, MediaType.APPLICATION_JSON));

        assertThat(cliente("token-de-prueba").buscarCartel("Amelie & Co+", 2001, "Una mujer vuelve a Paris."))
                .contains(CARTELES_URL + "/cartel.jpg");
        server.verify();
    }

    @Test
    void devuelveVacioSiTmdbNoRespondeCorrectamente() {
        server.expect(requestTo(BUSQUEDA)).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThat(cliente("token-de-prueba").buscarCartel("Amelie & Co+", 2001, null)).isEmpty();
        server.verify();
    }

    @Test
    void omiteLaConsultaSiNoHayToken() {
        assertThat(cliente("").buscarCartel("Amelie & Co+", 2001, null)).isEmpty();
        server.verify();
    }

    @Test
    void autenticaConLaClaveV3SiNoHayTokenDeLectura() {
        server.expect(requestTo(BUSQUEDA + "&api_key=clave-v3-de-prueba"))
                .andExpect(headerDoesNotExist(HttpHeaders.AUTHORIZATION))
                .andRespond(withSuccess("""
                        {"total_pages":1,"results":[{"title":"Amelie & Co+",
                          "release_date":"2001-04-25","poster_path":"/cartel.jpg"}]}
                        """, MediaType.APPLICATION_JSON));
        TmdbProperties properties = new TmdbProperties("", "clave-v3-de-prueba", API_URL, CARTELES_URL);

        assertThat(new TmdbClient(restClient, properties).buscarCartel("Amelie & Co+", 2001, null))
                .contains(CARTELES_URL + "/cartel.jpg");
        server.verify();
    }

    private TmdbClient cliente(String token) {
        return new TmdbClient(restClient, new TmdbProperties(token, "", API_URL, CARTELES_URL));
    }
}
