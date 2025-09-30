package com.thirdeye3.webscrapper.exceptions.handler;

import com.thirdeye3.webscrapper.dtos.Response;
import com.thirdeye3.webscrapper.exceptions.ApiException;
import com.thirdeye3.webscrapper.exceptions.ConnectionException;
import com.thirdeye3.webscrapper.exceptions.InvalidMachineException;
import com.thirdeye3.webscrapper.exceptions.WebScrapperException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Response<Object>> handleApiException(ApiException ex) {
        Response<Object> response = new Response<>(
                false,
                ex.getStatusCode(),
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    @ExceptionHandler(ConnectionException.class)
    public ResponseEntity<Response<Object>> handleConnectionException(ConnectionException ex) {
        Response<Object> response = new Response<>(
                false,
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(WebScrapperException.class)
    public ResponseEntity<Response<Object>> handleWebScrapperException(WebScrapperException ex) {
        Response<Object> response = new Response<>(
                false,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    @ExceptionHandler(InvalidMachineException.class)
    public ResponseEntity<Response<Object>> handleInvalidMachineException(InvalidMachineException ex) {
        Response<Object> response = new Response<>(
                false,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Object>> handleGeneralException(Exception ex) {
        Response<Object> response = new Response<>(
                false,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Unexpected error: " + ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
