import React from "react";
import "./TierChangePopup.css";

const TierChangePopup = ({ oldTier, newTier, onClose }) => {
  const tiers = ["NONE", "BRONZE", "SILVER", "GOLD", "PLATINUM"];
  
  const getTierIndex = (tier) => tiers.indexOf(tier || "NONE");
  
  const isUpgrade = getTierIndex(newTier) > getTierIndex(oldTier);
  const typeClass = isUpgrade ? "upgrade" : "downgrade";

  const getTierClass = (tier) => {
    if (!tier) return "none";
    return tier.toLowerCase();
  };

  return (
    <div className="tier-popup-overlay">
      <div className={`tier-popup-box ${typeClass}`}>
        <div className={`tier-popup-header ${typeClass}`}>
          {isUpgrade ? "🎉 Tier Upgrade!" : "⚠️ Tier Downgrade"}
        </div>
        <div className="tier-popup-content">
          <p>
            {isUpgrade 
              ? "Congratulations! You've reached a new loyalty status." 
              : "Your loyalty status has changed."}
          </p>
          
          <div className="tier-transition">
            <span className={`tier-badge ${getTierClass(oldTier)}`}>
              {oldTier || "NONE"}
            </span>
            <span className="tier-arrow">➜</span>
            <span className={`tier-badge ${getTierClass(newTier)}`}>
              {newTier || "NONE"}
            </span>
          </div>
          
          <p>
            {isUpgrade 
              ? "Keep riding to unlock more benefits!" 
              : "Ride more to regain your status!"}
          </p>
        </div>
        
        <button className={`tier-popup-button ${typeClass}`} onClick={onClose}>
          {isUpgrade ? "Awesome!" : "Got it"}
        </button>
      </div>
    </div>
  );
};

export default TierChangePopup;
