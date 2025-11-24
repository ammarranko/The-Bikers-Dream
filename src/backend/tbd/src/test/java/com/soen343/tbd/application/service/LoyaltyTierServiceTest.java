package com.soen343.tbd.application.service;

import com.soen343.tbd.domain.model.enums.TierType;
import com.soen343.tbd.domain.model.ids.UserId;
import com.soen343.tbd.domain.model.user.Rider;
import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.domain.repository.ReservationRepository;
import com.soen343.tbd.domain.repository.TripRepository;
import com.soen343.tbd.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class LoyaltyTierServiceTest {

    @Mock
    private TripRepository tripRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LoyaltyTierService loyaltyTierService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new Rider(
                new UserId(1L),
                "John Doe",
                "john@example.com",
                "password",
                "123 Street",
                "johndoe",
                Timestamp.valueOf("2023-01-01 00:00:00"),
                "visa-1234",
                null
        );
    }

    @Test
    void testUpdateUserTier_Bronze() {
        when(reservationRepository.missedReservationscount(eq(testUser.getUserId()), any()))
                .thenReturn(0);

        when(tripRepository.countUnreturnedBikesByUser(eq(testUser.getUserId())))
                .thenReturn(0);

        when(tripRepository.countTripsForUserByIdSince(eq(testUser.getUserId()), any()))
                .thenReturn(12); // Bronze passes

        // Fail Silver + Gold
        when(tripRepository.countTripsForUserBetween(eq(testUser.getUserId()), any(), any()))
                .thenReturn(3); // <5 → Silver fails → Gold fails

        loyaltyTierService.updateUserTier(testUser);

        assertEquals(TierType.BRONZE, testUser.getTierType());
        verify(userRepository).save(testUser);
    }

    @Test
    void testUpdateUserTier_Silver() {
        when(reservationRepository.missedReservationscount(eq(testUser.getUserId()), any()))
                .thenReturn(0);

        when(tripRepository.countUnreturnedBikesByUser(eq(testUser.getUserId())))
                .thenReturn(0);

        when(tripRepository.countTripsForUserByIdSince(eq(testUser.getUserId()), any()))
                .thenReturn(12); // Bronze passes

        when(tripRepository.countTripsForUserBetween(eq(testUser.getUserId()), any(), any()))
                .thenAnswer(inv -> {
                    LocalDateTime start = inv.getArgument(1);
                    LocalDateTime end = inv.getArgument(2);

                    long days = java.time.Duration.between(start, end).toDays();

                    // ---- Silver checks (approx 30 days) ----
                    if (days >= 28 && days <= 32) {
                        return 5; // Silver passes
                    }

                    // ---- Gold checks (approx 7 days) ----
                    if (days >= 6 && days <= 8) {
                        return 0; // Gold fails
                    }

                    return 0;
                });

        loyaltyTierService.updateUserTier(testUser);

        assertEquals(TierType.SILVER, testUser.getTierType());
        verify(userRepository).save(testUser);
    }

    @Test
    void testUpdateUserTier_Gold() {

        when(reservationRepository.missedReservationscount(eq(testUser.getUserId()), any()))
                .thenReturn(0);
        when(tripRepository.countUnreturnedBikesByUser(eq(testUser.getUserId())))
                .thenReturn(0);
        when(tripRepository.countTripsForUserByIdSince(eq(testUser.getUserId()), any()))
                .thenReturn(12); // Bronze

        // Silver + Gold pass
        when(tripRepository.countTripsForUserBetween(eq(testUser.getUserId()), any(), any()))
                .thenReturn(6); // >=5

        loyaltyTierService.updateUserTier(testUser);

        assertEquals(TierType.GOLD, testUser.getTierType());
        verify(userRepository).save(testUser);
    }

}
