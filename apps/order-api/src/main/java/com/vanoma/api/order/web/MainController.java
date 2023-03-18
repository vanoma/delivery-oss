package com.vanoma.api.order.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("")
public class MainController {

    @GetMapping(value = "/")
    public ResponseEntity<Map<String, String>> index() {
        Map<String, String> body = new HashMap<>();
        body.put("name", "Vanoma Order API");
        return new ResponseEntity<>(body, HttpStatus.OK);
    }
}
