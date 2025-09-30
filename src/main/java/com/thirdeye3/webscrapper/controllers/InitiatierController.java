package com.thirdeye3.webscrapper.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.thirdeye3.webscrapper.dtos.Response;
import com.thirdeye3.webscrapper.utils.Initiatier;

@RestController
@RequestMapping("/api/updateinitiatier")
public class InitiatierController {

    private static final Logger logger = LoggerFactory.getLogger(InitiatierController.class);

    @Autowired
    private Initiatier initiatier;

    @GetMapping()
    public Response<String> updateInitiatier() {
            try {
				initiatier.init();
	            return new Response<>(true, 0, null, "Initiatier updated");
			} catch (Exception e) {
				return new Response<>(false, 0, "Failed to update initiatier", null);
			}
    }
}