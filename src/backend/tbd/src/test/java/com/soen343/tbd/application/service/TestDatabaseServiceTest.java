package com.soen343.tbd.application.service;

import com.soen343.tbd.domain.model.Bike;
import com.soen343.tbd.domain.model.Dock;
import com.soen343.tbd.domain.model.Station;
import com.soen343.tbd.domain.model.enums.BikeStatus;
import com.soen343.tbd.domain.model.enums.BikeType;
import com.soen343.tbd.domain.model.enums.DockStatus;
import com.soen343.tbd.domain.model.enums.StationAvailability;
import com.soen343.tbd.domain.model.enums.StationStatus;
import com.soen343.tbd.domain.model.ids.BikeId;
import com.soen343.tbd.domain.model.ids.DockId;
import com.soen343.tbd.domain.model.ids.StationId;
import com.soen343.tbd.domain.model.ids.UserId;
import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.domain.repository.BikeRepository;
import com.soen343.tbd.domain.repository.DockRepository;
import com.soen343.tbd.domain.repository.StationRepository;
import com.soen343.tbd.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.sql.Timestamp;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestDatabaseServiceTest {

    @Mock
    private BikeRepository bikeRepository;
    @Mock
    private DockRepository dockRepository;
    @Mock
    private StationRepository stationRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TestDatabaseService testDatabaseService;

    private Bike bike;
    private Dock dock;
    private Station station;
    private User user;

    @BeforeEach
    void setUp() {

        bike = new Bike(
                new BikeId(1L),
                new DockId(1L),
                BikeStatus.AVAILABLE,
                BikeType.E_BIKE,
                null
        );


        // Dock with proper status and associated station
        dock = new Dock(
                new DockId(1L),
                new StationId(1L),
                DockStatus.OCCUPIED
        );

        // Station with proper availability and status, contains the dock
        station = new Station(
                new StationId(1L),
                "Station A",
                StationAvailability.FULL,
                StationStatus.ACTIVE,
                "Position A",
                "123 Street",
                10,
                1,
                List.of(dock)
        );

        // User with realistic role and creation timestamp
        user = new User(
                new UserId(1L),
                "John Doe",
                "john@example.com",
                "password",
                "123 Street",
                "johndoe",
                "RIDER",
                new Timestamp(System.currentTimeMillis()),
                null
        ) {
        };
    }

    @Test
    void testGetBikeById() {
        when(bikeRepository.findById(new BikeId(1L))).thenReturn(Optional.of(bike));

        Bike result = testDatabaseService.getBikeById(1L);
        assertNotNull(result);
        assertEquals(bike, result);
        verify(bikeRepository).findById(new BikeId(1L));
    }

    @Test
    void testGetDockById() {
        when(dockRepository.findById(new DockId(1L))).thenReturn(Optional.of(dock));

        Dock result = testDatabaseService.getDockById(1L);
        assertNotNull(result);
        assertEquals(dock, result);
        verify(dockRepository).findById(new DockId(1L));
    }

    @Test
    void testGetStationById() {
        when(stationRepository.findById(new StationId(1L))).thenReturn(Optional.of(station));

        Station result = testDatabaseService.getStationById(1L);
        assertNotNull(result);
        assertEquals(station, result);
        verify(stationRepository).findById(new StationId(1L));
    }

    @Test
    void testGetUserById() {
        when(userRepository.findById(new UserId(1L))).thenReturn(Optional.of(user));

        User result = testDatabaseService.getUserById(1L);
        assertNotNull(result);
        assertEquals(user, result);
        verify(userRepository).findById(new UserId(1L));
    }

    @Test
    void testGetUserByEmail() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        User result = testDatabaseService.getUserByEmail("john@example.com");
        assertNotNull(result);
        assertEquals(user, result);
        verify(userRepository).findByEmail("john@example.com");
    }

    @Test
    void testGetUserByUsername() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(user));

        User result = testDatabaseService.getUserByUsername("johndoe");
        assertNotNull(result);
        assertEquals(user, result);
        verify(userRepository).findByUsername("johndoe");
    }

    @Test
    void testSaveBike() {
        testDatabaseService.saveBike(bike);
        verify(bikeRepository).save(bike);
    }

    @Test
    void testSaveDock() {
        testDatabaseService.saveDock(dock);
        verify(dockRepository).save(dock);
    }

    @Test
    void testSaveStation() {
        testDatabaseService.saveStation(station);
        verify(stationRepository).save(station);
    }

    @Test
    void testSaveUser() {
        testDatabaseService.saveUser(user);
        verify(userRepository).save(user);
    }
}
