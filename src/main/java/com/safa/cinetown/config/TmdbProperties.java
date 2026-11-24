package com.safa.cinetown.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.tmdb")
public record TmdbProperties(String token, String apiKey, String apiUrl, String cartelesUrl) {
}
