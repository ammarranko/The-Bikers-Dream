package com.soen343.tbd.application.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(StationFullException.class)
    public ResponseEntity<Map<String, Object>> handleStationFull(StationFullException ex) {
        logger.warn("Attempt to return bike to a full station: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Station is full");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body); // 409
    }

    // Fallback handler for other runtime exceptions can be added later
}

