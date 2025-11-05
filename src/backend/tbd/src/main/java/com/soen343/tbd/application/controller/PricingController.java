package com.soen343.tbd.application.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.ArrayList;
import com.soen343.tbd.application.dto.PricingPlanDTO;

@RestController
@RequestMapping("api/pricing")
@CrossOrigin(origins = "http://localhost:3000")
public class PricingController {

    @GetMapping("/plan")
    public List<PricingPlanDTO> getPricingPlans() {
           return List.of(
            new PricingPlanDTO("STANDARD", 1.0, 0.15, 0.25),
            new PricingPlanDTO("PREMIUM", 2.0, 0.12, 0.20),
            new PricingPlanDTO("E-BIKE", 1.5, 0.20, 0.30)
        );
    }
}
