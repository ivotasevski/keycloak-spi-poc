package com.ivotasevski.externalapi.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class RolesController {

    @GetMapping("/api/users/{id}/roles")
    public List<String> getRoles(@PathVariable String id) {
        log.info("Returning roles for userId: {}", id);
        return List.of("ExternalRole1", "ExternalRole2", "ExternalRole3");
    }
}
