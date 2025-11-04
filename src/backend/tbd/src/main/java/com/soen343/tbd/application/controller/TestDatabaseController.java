package com.soen343.tbd.application.controller;

import com.soen343.tbd.application.service.TestDatabaseService;
import com.soen343.tbd.domain.model.Bike;
import com.soen343.tbd.domain.model.Dock;
import com.soen343.tbd.domain.model.Station;
import com.soen343.tbd.domain.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestDatabaseController {
    private final TestDatabaseService testDatabaseService;

    private static final Logger logger = LoggerFactory.getLogger(TestDatabaseController.class);

    public TestDatabaseController(TestDatabaseService testDatabaseService) {
        this.testDatabaseService = testDatabaseService;
    }

    @GetMapping("/bikes/{id}")
    public ResponseEntity<Bike> getBike(@PathVariable Long id) {
        logger.info("REQUEST RECEIVED | Get Bike with ID: {}", id);
        Bike bike = testDatabaseService.getBikeById(id);
        if (bike != null) {
            logger.info("Successfully retrieved bike for id: {}", id);
            return ResponseEntity.ok(bike);
        }
        logger.warn("Bike not found for id: {}", id);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/docks/{id}")
    public ResponseEntity<Dock> getDock(@PathVariable Long id) {
        logger.info("REQUEST RECEIVED | Get Dock with ID: {}", id);
        Dock dock = testDatabaseService.getDockById(id);
        if (dock != null) {
            logger.info("Successfully retrieved dock for id: {}", id);
            return ResponseEntity.ok(dock);
        }
        logger.warn("Dock not found for id: {}", id);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/stations/{id}")
    public ResponseEntity<Station> getStation(@PathVariable Long id) {
        logger.info("REQUEST RECEIVED | Get Station with ID: {}", id);
        Station station = testDatabaseService.getStationById(id);
        if (station != null) {
            logger.info("Successfully retrieved station for id: {}", id);
            return ResponseEntity.ok(station);
        }
        logger.warn("Station not found for id: {}", id);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        logger.info("REQUEST RECEIVED | Get User with ID: {}", id);
        User user = testDatabaseService.getUserById(id);
        if (user != null) {
            logger.info("Successfully retrieved user for id: {}", id);
            return ResponseEntity.ok(user);
        }
        logger.warn("User not found for id: {}", id);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/users/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        logger.info("REQUEST RECEIVED | Get User with email: {}", email);
        User user = testDatabaseService.getUserByEmail(email);
        if (user != null) {
            logger.info("Successfully retrieved user for email: {}", email);
            return ResponseEntity.ok(user);
        }
        logger.warn("User not found for email: {}", email);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/users/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        logger.info("REQUEST RECEIVED | Get User with username: {}", username);
        User user = testDatabaseService.getUserByUsername(username);
        if (user != null) {
            logger.info("Successfully retrieved user for username: {}", username);
            return ResponseEntity.ok(user);
        }
        logger.warn("User not found for username: {}", username);
        return ResponseEntity.notFound().build();
    }
}

