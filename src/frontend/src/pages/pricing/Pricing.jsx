import React, { useState, useEffect } from "react";
import "./Pricing.css";

const Pricing = () => {
  const [plans, setPlans] = useState([]);
  const [loading, setLoading] = useState(true);

  // Hardcoded data for your two backend plans
  const planData = [
    { 
      planType: "STANDARD", 
      baseFee: 1.0, 
      costPerMinute: 0.5 
    },
    { 
      planType: "E-BIKE", 
      baseFee: 2.0, 
      costPerMinute: 0.75 
    }
  ];

  // Plan colors
  const planColors = {
    STANDARD: "#6C63FF", // purple
    "E-BIKE": "#3FC1C9"  // teal
  };

  // Generate description
  const generateDescription = (plan) => {
    switch (plan.planType) {
      case "STANDARD":
        return "Standard bike with no additional per-trip surcharge.";
      case "E-BIKE":
        return "Electric bike with faster rides and higher per-minute cost.";
      default:
        return "Flexible bike plan.";
    }
  };

  // Calculate example 30-minute trip
  const calculateExampleTrip = (plan, durationMinutes = 30) => {
    return plan.baseFee + durationMinutes * plan.costPerMinute;
  };

  useEffect(() => {
    // Simulate fetching delay
    setTimeout(() => {
      setPlans(planData);
      setLoading(false);
    }, 300);
  }, []);

  if (loading) return <p>Loading pricing...</p>;

  return (
    <div className="pricing-container">
      <h1>Pricing Plans</h1>
      <p>Compare our ride options and choose the plan that suits you best.</p>

      <div className="pricing-cards">
        {plans.map((plan) => (
          <div
            className="pricing-card"
            key={plan.planType}
            style={{
              borderTop: `5px solid ${planColors[plan.planType]}`,
              boxShadow: `0 4px 10px ${planColors[plan.planType]}55`
            }}
          >
            <h2 style={{ color: planColors[plan.planType] }}>{plan.planType}</h2>
            <p>{generateDescription(plan)}</p>
            <ul>
              <li><strong>Base Fee:</strong> ${plan.baseFee.toFixed(2)}</li>
              <li><strong>Cost per Minute:</strong> ${plan.costPerMinute.toFixed(2)}</li>
            </ul>
            <p>
              <strong>Example 30-min trip:</strong> ${calculateExampleTrip(plan, 30).toFixed(2)}
            </p>
          </div>
        ))}
      </div>

      <p className="pricing-note">
        *Trips are billed based on duration. Electric bikes include a higher per-minute cost.
      </p>
    </div>
  );
};

export default Pricing;
