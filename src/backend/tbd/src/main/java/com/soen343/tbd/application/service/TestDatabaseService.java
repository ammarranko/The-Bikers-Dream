package com.soen343.tbd.application.service;

import com.soen343.tbd.domain.model.Bike;
import com.soen343.tbd.domain.model.Dock;
import com.soen343.tbd.domain.model.Station;
import com.soen343.tbd.domain.model.ids.BikeId;
import com.soen343.tbd.domain.model.ids.DockId;
import com.soen343.tbd.domain.model.ids.StationId;
import com.soen343.tbd.domain.model.ids.UserId;
import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.domain.repository.BikeRepository;
import com.soen343.tbd.domain.repository.DockRepository;
import com.soen343.tbd.domain.repository.StationRepository;
import com.soen343.tbd.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TestDatabaseService {
    private final BikeRepository bikeRepository;
    private final DockRepository dockRepository;
    private final StationRepository stationRepository;
    private final UserRepository userRepository;

    public TestDatabaseService(BikeRepository bikeRepository,
                               DockRepository dockRepository,
                               StationRepository stationRepository,
                               UserRepository userRepository) {
        this.bikeRepository = bikeRepository;
        this.dockRepository = dockRepository;
        this.stationRepository = stationRepository;
        this.userRepository = userRepository;
    }

    // Test fetching a bike by ID
    public Bike getBikeById(Long id) {
        Optional<Bike> bike = bikeRepository.findById(new BikeId(id));
        if (bike.isPresent()) {
            System.out.println("Found bike with ID: " + id);
            System.out.println("Bike Status: " + bike.get().getStatus());
            System.out.println("Bike Type: " + bike.get().getBikeType());
            if (bike.get().getDockId() != null) {
                System.out.println("Docked at Dock ID: " + bike.get().getDockId().value());
            }
            return bike.get();
        } else {
            System.out.println("Bike with ID " + id + " not found");
            return null;
        }
    }

    // Test fetching a dock by ID
    public Dock getDockById(Long id) {
        Optional<Dock> dock = dockRepository.findById(new DockId(id));
        if (dock.isPresent()) {
            System.out.println("Found dock with ID: " + id);
            System.out.println("Dock Status: " + dock.get().getStatus());
            if (dock.get().getStationId() != null) {
                System.out.println("Part of Station ID: " + dock.get().getStationId().value());
            }
            return dock.get();
        } else {
            System.out.println("Dock with ID " + id + " not found");
            return null;
        }
    }

    // Test fetching a station by ID (includes all its docks!)
    public Station getStationById(Long id) {
        Optional<Station> station = stationRepository.findById(new StationId(id));
        if (station.isPresent()) {
            System.out.println("Found station with ID: " + id);
            System.out.println("Station Name: " + station.get().getStationName());
            System.out.println("Station Address: " + station.get().getAddress());
            System.out.println("Capacity: " + station.get().getCapacity());
            System.out.println("Bikes Docked: " + station.get().getNumberOfBikesDocked());
            System.out.println("Number of Docks: " +
                (station.get().getDocks() != null ? station.get().getDocks().size() : 0));
            return station.get();
        } else {
            System.out.println("Station with ID " + id + " not found");
            return null;
        }
    }

    // Test fetching a user by ID
    public User getUserById(Long id) {
        Optional<User> user = userRepository.findById(new UserId(id));
        if (user.isPresent()) {
            System.out.println("Found user with ID: " + id);
            System.out.println("Username: " + user.get().getUsername());
            System.out.println("Email: " + user.get().getEmail());
            System.out.println("Role: " + user.get().getRole());
            return user.get();
        } else {
            System.out.println("User with ID " + id + " not found");
            return null;
        }
    }

    // Test fetching a user by email
    public User getUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            System.out.println("Found user with email: " + email);
            System.out.println("Username: " + user.get().getUsername());
            System.out.println("Role: " + user.get().getRole());
            return user.get();
        } else {
            System.out.println("User with email " + email + " not found");
            return null;
        }
    }

    // Test fetching a user by username
    public User getUserByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            System.out.println("Found user with username: " + username);
            System.out.println("Email: " + user.get().getEmail());
            System.out.println("Role: " + user.get().getRole());
            return user.get();
        } else {
            System.out.println("User with username " + username + " not found");
            return null;
        }
    }

    // Test saving a bike
    public void saveBike(Bike bike) {
        System.out.println("Saving bike with ID: " + bike.getBikeId().value());
        bikeRepository.save(bike);
        System.out.println("Bike saved successfully!");
    }

    // Test saving a dock
    public void saveDock(Dock dock) {
        System.out.println("Saving dock with ID: " + dock.getDockId().value());
        dockRepository.save(dock);
        System.out.println("Dock saved successfully!");
    }

    // Test saving a station
    public void saveStation(Station station) {
        System.out.println("Saving station: " + station.getStationName());
        stationRepository.save(station);
        System.out.println("Station saved successfully!");
    }

    // Test saving a user
    public void saveUser(User user) {
        System.out.println("Saving user: " + user.getUsername());
        userRepository.save(user);
        System.out.println("User saved successfully!");
    }
}

