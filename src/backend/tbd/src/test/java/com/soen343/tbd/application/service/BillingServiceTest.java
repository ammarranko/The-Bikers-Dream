package com.soen343.tbd.application.service;

import com.soen343.tbd.application.dto.billing.AllBillingHistoryResponse;
import com.soen343.tbd.application.dto.billing.UserBillingHistoryResponse;
import com.soen343.tbd.application.dto.billing.UserPaymentRequest;
import com.soen343.tbd.domain.model.Bill;
import com.soen343.tbd.domain.model.Station;
import com.soen343.tbd.domain.model.Trip;
import com.soen343.tbd.domain.model.enums.BillStatus;
import com.soen343.tbd.domain.model.ids.BillId;
import com.soen343.tbd.domain.model.ids.TripId;
import com.soen343.tbd.domain.model.ids.UserId;
import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.domain.repository.BillRepository;
import com.soen343.tbd.domain.repository.StationRepository;
import com.soen343.tbd.domain.repository.TripRepository;
import com.soen343.tbd.domain.repository.UserRepository;
import com.soen343.tbd.infrastructure.payment.PaymentGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BillingServiceTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StationRepository stationRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private BillingService billingService;

    private User testUser;
    private Trip testTrip;
    private Bill testBill;

    @BeforeEach
    void setUp() {
        // Initialize mocks - stubbing will be done in individual tests as needed
        testUser = mock(User.class);
        testTrip = mock(Trip.class);
        testBill = mock(Bill.class);
    }

    // ========== Tests for getAllBillingHistoryForUser() ==========

    /**
     * Test BillingService.getBillingHistoryForUser when user exists and has trips and bills.
     * Should return a complete UserBillingHistoryResponse.
     */
    @Test
    void getBillingHistoryForUserTest_whenUserExists() {
        // Arrange
        String userEmail = "test@example.com";
        when(testUser.getUserId()).thenReturn(new UserId(1L));
        when(testUser.getEmail()).thenReturn("test@example.com");
        when(testUser.getFullName()).thenReturn("Test User");

        // Mock Trip with all necessary fields
        when(testTrip.getTripId()).thenReturn(new TripId(1L));
        when(testTrip.getEndTime()).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(testTrip.getStartStationId()).thenReturn(new com.soen343.tbd.domain.model.ids.StationId(1L));
        when(testTrip.getEndStationId()).thenReturn(new com.soen343.tbd.domain.model.ids.StationId(2L));
        when(testTrip.getBikeId()).thenReturn(new com.soen343.tbd.domain.model.ids.BikeId(100L));
        when(testTrip.getStartTime()).thenReturn(new Timestamp(System.currentTimeMillis() - 3600000));
        when(testTrip.calculateDurationInMinutes()).thenReturn(60.0);
        when(testTrip.getPricingStrategy()).thenReturn(null); // Use default values

        // Mock Bill with all necessary fields
        when(testBill.getBillId()).thenReturn(new BillId(1L));
        when(testBill.getTripId()).thenReturn(new TripId(1L));
        when(testBill.getStatus()).thenReturn(BillStatus.PAID);
        when(testBill.getDiscountedCost()).thenReturn(10.50);

        // Mock Station
        Station testStation = mock(Station.class);
        when(testStation.getStationName()).thenReturn("Test Station");

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(tripRepository.findAllByUserId(testUser.getUserId())).thenReturn(List.of(testTrip));
        when(billRepository.findAllByUserId(testUser.getUserId())).thenReturn(List.of(testBill));
        when(stationRepository.findById(any())).thenReturn(Optional.of(testStation));

        // Act
        UserBillingHistoryResponse response = billingService.getAllBillingHistoryForUser(userEmail);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUserEmail()).isEqualTo(userEmail);
        assertThat(response.getFullName()).isEqualTo("Test User");
        assertThat(response.getTotalTrips()).isEqualTo(1);
        assertThat(response.getTripBills()).hasSize(1);
        assertThat(response.getTripBills().get(0).getTripId()).isEqualTo(1L);
        assertThat(response.getTripBills().get(0).getBillId()).isEqualTo(1L);

        verify(userRepository).findByEmail(userEmail);
        verify(tripRepository).findAllByUserId(testUser.getUserId());
        verify(billRepository).findAllByUserId(testUser.getUserId());
    }

    /**
     * Test BillingService.getBillingHistoryForUser when user does not exist.
     * Should throw RuntimeException.
     */
    @Test
    void getAllBillingHistory_WhenUserDoesNotExist() {
        // Arrange
        String userEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> billingService.getAllBillingHistoryForUser(userEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No user found with email: " + userEmail);

        verify(userRepository).findByEmail(userEmail);
        verify(tripRepository, never()).findAllByUserId(any());
        verify(billRepository, never()).findAllByUserId(any());
    }

    /**
     * Test BillingService.getBillingHistoryForUser when user has no trips.
     * Should return an empty UserBillingHistoryResponse.
     */
    @Test
    void getAllBillingHistoryForUser_WhenUserHasNoTrips() {
        // Arrange
        String userEmail = "test@example.com";
        when(testUser.getUserId()).thenReturn(new UserId(1L));

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(tripRepository.findAllByUserId(testUser.getUserId())).thenReturn(List.of());
        when(billRepository.findAllByUserId(testUser.getUserId())).thenReturn(List.of());

        // Act
        UserBillingHistoryResponse response = billingService.getAllBillingHistoryForUser(userEmail);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTotalTrips()).isEqualTo(0);
        assertThat(response.getTripBills()).isEmpty();
    }

    // ========== Tests for getAllSystemBillingHistory() ==========

    /**
     * Test BillingService.getAllSystemBillingHistory when trips and bills exist.
     * Should return a complete AllBillingHistoryResponse.
     */
    @Test
    void getAllSystemBillingHistory_WhenTripsAndBillsExist() {
        // Arrange
        // Mock Trip with all necessary fields
        when(testTrip.getTripId()).thenReturn(new TripId(1L));
        when(testTrip.getEndTime()).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(testTrip.getUserId()).thenReturn(new UserId(1L));
        when(testTrip.getStartStationId()).thenReturn(new com.soen343.tbd.domain.model.ids.StationId(1L));
        when(testTrip.getEndStationId()).thenReturn(new com.soen343.tbd.domain.model.ids.StationId(2L));
        when(testTrip.getBikeId()).thenReturn(new com.soen343.tbd.domain.model.ids.BikeId(100L));
        when(testTrip.getStartTime()).thenReturn(new Timestamp(System.currentTimeMillis() - 3600000));
        when(testTrip.calculateDurationInMinutes()).thenReturn(60.0);
        when(testTrip.getPricingStrategy()).thenReturn(null);

        // Mock Bill with all necessary fields
        when(testBill.getBillId()).thenReturn(new BillId(1L));
        when(testBill.getTripId()).thenReturn(new TripId(1L));
        when(testBill.getStatus()).thenReturn(BillStatus.PAID);
        when(testBill.getDiscountedCost()).thenReturn(10.50);

        // Mock Station and User
        Station testStation = mock(Station.class);
        when(testStation.getStationName()).thenReturn("Test Station");

        when(testUser.getEmail()).thenReturn("test@example.com");
        when(testUser.getFullName()).thenReturn("Test User");

        when(tripRepository.findAll()).thenReturn(List.of(testTrip));
        when(billRepository.findAll()).thenReturn(List.of(testBill));
        when(userRepository.findById(any())).thenReturn(Optional.of(testUser));
        when(stationRepository.findById(any())).thenReturn(Optional.of(testStation));

        // Act
        AllBillingHistoryResponse response = billingService.getAllSystemBillingHistory();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTotalSystemTrips()).isEqualTo(1);

        verify(tripRepository).findAll();
        verify(billRepository).findAll();
    }

    /**
     * Test BillingService.getAllSystemBillingHistory when no trips exist.
     * Should return an empty AllBillingHistoryResponse.
     */
    @Test
    void getAllSystemBillingHistoryTest_WhenNoTripsExist() {
        // Arrange
        when(tripRepository.findAll()).thenReturn(List.of());
        when(billRepository.findAll()).thenReturn(List.of());

        // Act
        AllBillingHistoryResponse response = billingService.getAllSystemBillingHistory();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTotalSystemTrips()).isEqualTo(0);
        assertThat(response.getAllTripBills()).isEmpty();
    }

    // ========== Tests for processPayment() ==========

    /**
     * Test BillingService.processPayment when payment is successful.
     * Should update bill status to PAID and return true.
     */
    @Test
    void processPaymentTest_SuccessfulPayment() {
        // Arrange
        UserPaymentRequest paymentRequest = new UserPaymentRequest(
                100L,
                "1234567890123456",
                "12/25",
                "01",
                "30",
                "123"
        );
        String userEmail = "test@example.com";

        when(testBill.getStatus()).thenReturn(BillStatus.PENDING);
        when(testBill.getUserId()).thenReturn(new UserId(1L));
        when(testBill.getDiscountedCost()).thenReturn(10.50);
        when(testUser.getUserId()).thenReturn(new UserId(1L));

        when(billRepository.findById(new BillId(100L))).thenReturn(Optional.of(testBill));
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(paymentGateway.processPayment(eq(paymentRequest), eq(testUser), eq(10.50))).thenReturn(true);
        when(billRepository.save(testBill)).thenReturn(testBill);

        // Act
        boolean result = billingService.processPayment(paymentRequest, userEmail);

        // Assert
        assertThat(result).isTrue();
        verify(billRepository).findById(new BillId(100L));
        verify(userRepository).findByEmail(userEmail);
        verify(paymentGateway).processPayment(eq(paymentRequest), eq(testUser), eq(10.50));
        verify(testBill).setStatus(BillStatus.PAID);
        verify(billRepository).save(testBill);
    }

    /**
     * Test BillingService.processPayment when bill does not exist.
     * Should throw RuntimeException.
     */
    @Test
    void processPaymentTest_NoBillFound() {
        // Arrange
        UserPaymentRequest paymentRequest = new UserPaymentRequest(
                999L,
                "1234567890123456",
                "12/25",
                "01",
                "30",
                "123"
        );
        String userEmail = "test@example.com";

        when(billRepository.findById(new BillId(999L))).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> billingService.processPayment(paymentRequest, userEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Bill not found with ID: 999");

        verify(billRepository).findById(new BillId(999L));
        verify(paymentGateway, never()).processPayment(any(), any(), any());
        verify(billRepository, never()).save(any());
    }

    /**
     * Test BillingService.processPayment when bill is already paid.
     * Should throw RuntimeException.
     */
    @Test
    void processPaymentTest_WhenBillAlreadyPaid() {
        // Arrange
        UserPaymentRequest paymentRequest = new UserPaymentRequest(
                100L,
                "1234567890123456",
                "12/25",
                "01",
                "30",
                "123"
        );
        String userEmail = "test@example.com";

        when(testBill.getStatus()).thenReturn(BillStatus.PAID);
        when(billRepository.findById(new BillId(100L))).thenReturn(Optional.of(testBill));

        // Act & Assert
        assertThatThrownBy(() -> billingService.processPayment(paymentRequest, userEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Bill has already been paid");

        verify(billRepository).findById(new BillId(100L));
        verify(userRepository, never()).findByEmail(any());
        verify(paymentGateway, never()).processPayment(any(), any(), any());
    }

    /**
     * Test BillingService.processPayment when user does not exist.
     * Should throw RuntimeException.
     */
    @Test
    void processPaymentTest_WhenUserNotFound() {
        // Arrange
        UserPaymentRequest paymentRequest = new UserPaymentRequest(
                100L,
                "1234567890123456",
                "12/25",
                "01",
                "30",
                "123"
        );
        String userEmail = "nonexistent@example.com";

        when(testBill.getStatus()).thenReturn(BillStatus.PENDING);
        when(billRepository.findById(new BillId(100L))).thenReturn(Optional.of(testBill));
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> billingService.processPayment(paymentRequest, userEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found with email: " + userEmail);

        verify(paymentGateway, never()).processPayment(any(), any(), any());
        verify(billRepository, never()).save(any());
    }

    /**
     * Test BillingService.processPayment when user does not have permission to pay the bill.
     * Should throw RuntimeException.
     */
    @Test
    void processPaymentTest_WhenUserHasNoPermission() {
        // Arrange
        UserPaymentRequest paymentRequest = new UserPaymentRequest(
                100L,
                "1234567890123456",
                "12/25",
                "01",
                "30",
                "123"
        );
        String userEmail = "test@example.com";

        User differentUser = mock(User.class);
        when(differentUser.getUserId()).thenReturn(new UserId(999L));

        when(testBill.getStatus()).thenReturn(BillStatus.PENDING);
        when(testBill.getUserId()).thenReturn(new UserId(1L));
        when(billRepository.findById(new BillId(100L))).thenReturn(Optional.of(testBill));
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(differentUser));

        // Act & Assert
        assertThatThrownBy(() -> billingService.processPayment(paymentRequest, userEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User does not have permission to pay this bill");

        verify(paymentGateway, never()).processPayment(any(), any(), any());
        verify(billRepository, never()).save(any());
    }

    /**
     * Test BillingService.processPayment when payment gateway fails.
     * Should throw RuntimeException.
     */
    @Test
    void processPaymentTest_WhenPaymentGatewayFails() {
        // Arrange
        UserPaymentRequest paymentRequest = new UserPaymentRequest(
                100L,
                "1234567890123456",
                "12/25",
                "01",
                "30",
                "123"
        );
        String userEmail = "test@example.com";

        when(testBill.getStatus()).thenReturn(BillStatus.PENDING);
        when(testBill.getUserId()).thenReturn(new UserId(1L));
        when(testBill.getDiscountedCost()).thenReturn(10.50);
        when(testUser.getUserId()).thenReturn(new UserId(1L));

        when(billRepository.findById(new BillId(100L))).thenReturn(Optional.of(testBill));
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(paymentGateway.processPayment(eq(paymentRequest), eq(testUser), eq(10.50))).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> billingService.processPayment(paymentRequest, userEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment processing failed");

        verify(paymentGateway).processPayment(eq(paymentRequest), eq(testUser), eq(10.50));
        verify(billRepository, never()).save(any());
    }
}
