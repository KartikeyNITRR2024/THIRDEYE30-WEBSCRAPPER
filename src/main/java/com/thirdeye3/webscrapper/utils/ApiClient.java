package com.thirdeye3.webscrapper.utils;

import com.thirdeye3.webscrapper.dtos.Response;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class ApiClient {

    public <T> Response<T> getForObject(String url, ParameterizedTypeReference<Response<T>> typeRef) {
        try {
            return WebClient.create()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(typeRef)
                    .block();
        } catch (WebClientResponseException ex) {
            return new Response<>(
                    false,
                    ex.getRawStatusCode(),
                    "API error: " + ex.getStatusText(),
                    null
            );
        } catch (WebClientRequestException ex) {
            return new Response<>(
                    false,
                    500,
                    "Connection error: " + ex.getMessage(),
                    null
            );
        } catch (Exception ex) {
            return new Response<>(
                    false,
                    500,
                    "Unexpected error: " + ex.getMessage(),
                    null
            );
        }
    }

    public <B, T> Response<T> postForObject(String url, B body, ParameterizedTypeReference<Response<T>> typeRef) {
        try {
            return WebClient.create()
                    .post()
                    .uri(url)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(typeRef)
                    .block();
        } catch (WebClientResponseException ex) {
            return new Response<>(
                    false,
                    ex.getRawStatusCode(),
                    "API error: " + ex.getStatusText(),
                    null
            );
        } catch (WebClientRequestException ex) {
            return new Response<>(
                    false,
                    500,
                    "Connection error: " + ex.getMessage(),
                    null
            );
        } catch (Exception ex) {
            return new Response<>(
                    false,
                    500,
                    "Unexpected error: " + ex.getMessage(),
                    null
            );
        }
    }
}
