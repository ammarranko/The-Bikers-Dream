import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import NavigationBar from '../../components/navigationBar/NavigationBar';
import '../home/Home.css';
import "./Pricing.css";

const Pricing = () => {
  const navigate = useNavigate();
  const [plans, setPlans] = useState([]);
  const [loading, setLoading] = useState(true);

  // Get user info from localStorage
  const fullName = localStorage.getItem('user_full_name');
  const role = localStorage.getItem('user_role');

  const planData = [
    { planType: "STANDARD", baseFee: 1.0, costPerMinute: 0.5 },
    { planType: "E-BIKE", baseFee: 3.0, costPerMinute: 0.35 }
  ];

  const planColors = {
    STANDARD: "#6C63FF",
    "E-BIKE": "#3FC1C9"
  };

  const generateDescription = (plan) => {
    switch (plan.planType) {
      case "STANDARD": return "Standard bike with no additional per-trip surcharge.";
      case "E-BIKE": return "Electric bike with faster rides and higher per-minute cost.";
      default: return "Flexible bike plan.";
    }
  };

  const calculateExampleTrip = (plan, durationMinutes = 30) => {
    return plan.baseFee + durationMinutes * plan.costPerMinute;
  };

  useEffect(() => {
    setPlans(planData);
    setLoading(false);
  }, []);

  const handleLogout = () => {
    localStorage.clear();
    navigate('/');
  };

  const handleHomeClick = () => {
    navigate('/home');
  };

  const handleBillingClick = () => {
    navigate('/billing');
  };

  const handleViewHistory = () => {
    navigate('/history');
  };

  const handlePricingClick = () => {
    // Already on pricing page
    window.location.reload();
  };

  if (loading) return <p>Loading...</p>;

  return (
    <div className="home-container">
      <NavigationBar
        fullName={fullName}
        role={role}
        handleLogout={handleLogout}
        handleBillingClick={handleBillingClick}
        handleHomeClick={handleHomeClick}
        handleViewHistory={handleViewHistory}
        handlePricingClick={handlePricingClick}
        activePage="pricing"
      />

      <div className="pricing-page-container">
        <div className="pricing-section">
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
        </div>
      </div>
    </div>
  );
};

export default Pricing;

