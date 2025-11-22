package com.soen343.tbd.application.service;

import com.soen343.tbd.application.dto.billing.FlexMoneyDTO;
import com.soen343.tbd.domain.model.Station;
import com.soen343.tbd.domain.model.ids.UserId;
import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.domain.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*  1 flex money = 1 cent, so 100 flex money = 1$
    *NOTE: this service is being called in trip service/return 
    - checks if user is eligible for flex money obtention
    - can add flex money into user's balance
    - can automatically reduce bill of next trip using existing balance
 */

@Service
public class FlexMoneyService {

    private static final Logger logger = LoggerFactory.getLogger(FlexMoneyService.class);

    private final UserRepository userRepository;

    public FlexMoneyService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public Integer addFlexMoneyIfEligible(UserId userId, Station station, double durationMinutes) {
        // bikes is count minus one since checking if it was
        // at <25% full before the docking of the bike return
        int bikes = station.getNumberOfBikesDocked() - 1;
        int capacity = station.getCapacity();
        double fullness = (double) bikes / capacity;

        if (fullness < 0.25) {
            // FlexMoney is now a function of time: 10 points per minute
            int amount = (int) Math.ceil(durationMinutes * 10);
            // Minimum reward of 10 points
            if (amount < 10) amount = 10;
            
            updateFlexMoneyBalance(userId, amount);
            return amount;
        }
        return 0;
    }

    @Transactional
    public FlexMoneyDTO updateFlexMoneyBalance(UserId userId, Integer amountToAddRemove) {

        logger.info("UserID: {}, change to flex money: {}", userId.value(), amountToAddRemove);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId.value()));
        
        Integer currentBalance = user.getFlexMoney();
        Integer updatedBalance = currentBalance + amountToAddRemove;
        user.setFlexMoney(updatedBalance);
        userRepository.save(user);

        logger.info("UserID: {} flex money updated: {}", userId.value(), updatedBalance);

        return new FlexMoneyDTO(updatedBalance, amountToAddRemove);
    }

    @Transactional
    public double reduceBillWithFlexMoney(UserId userId, double bill) {
        logger.info("UserID: {}, total bill: {}", userId.value(), bill);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId.value()));

        Integer userFlexMoney = user.getFlexMoney();
        double billProcessing = (bill*100);
        Integer billInFlexMoney = (int) billProcessing;
        double billAfterReduction;

        int flexMoneyBillAfterReduction = billInFlexMoney - userFlexMoney;
        // more flex money than bill
        if (flexMoneyBillAfterReduction < 0) {
            billAfterReduction = 0.0;
            updateFlexMoneyBalance(userId, -billInFlexMoney);
        } else { // use up all flex money
            billAfterReduction = flexMoneyBillAfterReduction/100.0;
            updateFlexMoneyBalance(userId, -userFlexMoney);
        }

        return billAfterReduction;

    }
}