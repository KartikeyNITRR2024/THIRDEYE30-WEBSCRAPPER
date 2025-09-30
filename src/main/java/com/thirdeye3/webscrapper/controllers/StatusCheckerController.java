package com.thirdeye3.webscrapper.controllers;
import com.thirdeye3.webscrapper.dtos.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statuschecker")
public class StatusCheckerController {

    private static final Logger logger = LoggerFactory.getLogger(StatusCheckerController.class);

    @Value("${webscrapper.uniqueId}")
    private Integer uniqueId;

    @Value("${webscrapper.uniqueCode}")
    private String uniqueCode;

    @GetMapping("/{id}/{code}")
    public ResponseEntity<Response<String>> getStatus(@PathVariable("id") Integer id, @PathVariable("code") String code) {
        if (id.equals(uniqueId) && code.equals(uniqueCode)) {
            Response<String> response = new Response<>(true,0,null,"Valid credentials");
            return ResponseEntity.ok(response);
        } else {
            logger.warn("Status check failed for id: {} and code: {}", id, code);
            Response<String> response = new Response<>(false,404,"Invalid credentials",null);
            return ResponseEntity.status(404).body(response);
        }
    }
}
