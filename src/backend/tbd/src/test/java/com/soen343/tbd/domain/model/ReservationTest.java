package com.soen343.tbd.domain.model;

import com.soen343.tbd.domain.model.enums.ReservationStatus;
import com.soen343.tbd.domain.model.ids.BikeId;
import com.soen343.tbd.domain.model.ids.StationId;
import com.soen343.tbd.domain.model.ids.UserId;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.*;

public class ReservationTest {
    // ========== Tests for cancel method ==========

    /**
     * Test cancelling an active reservation sets its status to CANCELLED.
     */
    @Test
    void cancelTest_CancelActiveReservation() {
        Reservation reservation = new Reservation(
                new BikeId(1L), new StationId(2L), new UserId(3L),
                new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 10000)
        );
        reservation.cancel();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    /**
     * Test cancelling a non-active reservation throws an exception.
     */
    @Test
    void cancelTest_CancelNonActiveReservationThrowsException() {
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.COMPLETED);
        assertThatThrownBy(reservation::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Reservation cannot be cancelled because it is not active.");
    }

    // ========== Tests for complete method ==========

    /**
     * Test completing an active reservation sets its status to COMPLETED.
     */
    @Test
    void completeTest_SetsActiveReservationToCompleted() {
        Reservation reservation = new Reservation(
                new BikeId(1L), new StationId(2L), new UserId(3L),
                new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 10000)
        );
        reservation.complete();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.COMPLETED);
    }

    /**
     * Test completing a non-active reservation throws an exception.
     */
    @Test
    void completeTest_CompleteNonActiveReservationThrowsException() {
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.CANCELLED);
        assertThatThrownBy(reservation::complete)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Reservation cannot be completed because it is not active.");
    }

    // ========== Tests for isExpired and expire methods ==========
    /**
     * Test isExpired method returns true if the reservation has expired.
     * Test expire method sets the reservation status to EXPIRED.
     */
    @Test
    void isExpiredTest_ReturnsTrueIfExpired() {
        Reservation reservation = new Reservation();
        reservation.setExpiresAt(new Timestamp(System.currentTimeMillis() - 10000));
        assertThat(reservation.isExpired()).isTrue();
    }

    /**
     * Test isExpired method returns false if the reservation has not expired.
     */
    @Test
    void isExpiredTest_ReturnsFalseIfNotExpired() {
        Reservation reservation = new Reservation();
        reservation.setExpiresAt(new Timestamp(System.currentTimeMillis() + 10000));
        assertThat(reservation.isExpired()).isFalse();
    }

    /**
     * Test expire method sets the reservation status to EXPIRED.
     */
    @Test
    void expireTest_SetsStatusExpired() {
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.ACTIVE);
        reservation.expire();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.EXPIRED);
    }
}

