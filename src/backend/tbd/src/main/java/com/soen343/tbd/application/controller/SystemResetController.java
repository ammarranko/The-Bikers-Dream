package com.soen343.tbd.application.controller;

import com.soen343.tbd.application.service.SystemResetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class SystemResetController {

    private final SystemResetService systemResetService;

    public SystemResetController(SystemResetService systemResetService) {
        this.systemResetService = systemResetService;
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetSystem() {
        try {
            systemResetService.resetSystem();
            return ResponseEntity.ok("System reset successfully.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to reset system: " + e.getMessage());
        }
    }
}
