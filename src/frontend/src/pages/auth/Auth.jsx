import React, { useState, useEffect } from "react";
import "./Auth.css";
import logo from "../../assets/logo.png";
import axios from "axios";
import { useNavigate, useLocation } from "react-router-dom";
import TierChangePopup from "../../components/tierChangePopup/TierChangePopup";

const initialForm = {
  fullName: "",
  email: "",
  password: "",
  confirmPassword: "",
  username: "",
  street: "",
  city: "",
  postalCode: "",
  cardHolderName: "",
  cardNumber: "",
  expiryMonth: "",
  expiryYear: "",
  cvc: "",
};

const Auth = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [isLogin, setIsLogin] = useState(true);
  const [formData, setFormData] = useState(initialForm);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const [showTierPopup, setShowTierPopup] = useState(false);
  const [tierChangeData, setTierChangeData] = useState({ oldTier: "", newTier: "" });

  const handleLogout = () => {
    // Clear stored auth info and axios header
    try {
      localStorage.removeItem("jwt_token");
      localStorage.removeItem("user_email");
      localStorage.removeItem("user_full_name");
      localStorage.removeItem("tier"); // Clear tier on logout
      delete axios.defaults.headers.common["Authorization"];
    } finally {
      // Reset UI state
      setIsLogin(true);
      setFormData(initialForm);
      setError("");
    }
  };

  useEffect(() => {
    // Check if user is already logged in and redirect to home
    const token = localStorage.getItem("jwt_token");
    if (token) {
      // Set the default authorization header
      axios.defaults.headers.common["Authorization"] = `Bearer ${token}`;
      // Redirect to home page
      navigate("/home");
    }
  }, [navigate]);

  useEffect(() => {
    // Check for session expiration message
    const params = new URLSearchParams(location.search);
    if (params.get('expired')) {
      setError("Your session has expired. Please login again.");
    }
  }, [location]);

  const handleInputChange = (e) =>
    setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError("");

    try {
      // For login
      if (isLogin) {
        const response = await axios.post("http://localhost:8080/api/login", {
          email: formData.email,
          password: formData.password,
        });

        const { token, email, fullName, role, username, tier, flexMoney } =
          response.data || {};

        // Ensure we actually received a valid token from the server
        if (!token || token === "undefined" || token === "null") {
          setError("Login failed: invalid email or password");
          return;
        }

        // Store auth data
        localStorage.setItem("jwt_token", token);
        localStorage.setItem("user_email", email);
        localStorage.setItem("user_full_name", fullName);
        localStorage.setItem("user_role", role);
        localStorage.setItem("username", username);
        localStorage.setItem("actual_user_role", role); // to keep track of actual role if switched
        localStorage.setItem("tier", tier || "NONE"); // Store user tier
        localStorage.setItem("flexMoney", flexMoney || 0); // Store flex money

        const previousTierKey = `previousTier_${email}`; // storing w email to differentiate users
        const storedTier = localStorage.getItem(previousTierKey);
        const newTier = tier || "NONE";

        // Set default header for future requests
        axios.defaults.headers.common["Authorization"] = `Bearer ${token}`;

        // Only show popup if we have a record of a previous tier AND it's different
        if (storedTier && storedTier !== newTier) {
          setTierChangeData({ oldTier: storedTier, newTier: newTier });
          setShowTierPopup(true);
          localStorage.setItem(previousTierKey, newTier);
        } else {
          // First time on this device OR no change
          localStorage.setItem(previousTierKey, newTier);
          navigate("/home");
        }
      }
      // For registration
      else {
        // Check if passwords match
        if (formData.password !== formData.confirmPassword) {
          setError("Passwords do not match");
          return;
        }
        const address = `${formData.street}, ${formData.city}, ${formData.postalCode}`;
        // Call registration endpoint (expects fullName, email, password, address)
        await axios.post("http://localhost:8080/api/register", {
          fullName: formData.fullName,
          email: formData.email,
          password: formData.password,
          address: address,
          username: formData.username,
          cardHolderName: formData.cardHolderName,
          cardNumber: formData.cardNumber,
          expiryMonth: formData.expiryMonth,
          expiryYear: formData.expiryYear,
          cvc: formData.cvc,
        });

        // On successful registration, switch to login mode
        setIsLogin(true);
        setFormData({ ...initialForm, email: formData.email });
        alert("Registration successful! Please log in.");
      }
    } catch (error) {
      setError(error.response?.data?.message || "Authentication failed");
    } finally {
      setIsLoading(false);
    }
  };

  const toggleMode = () => {
    setIsLogin(!isLogin);
    setFormData(initialForm);
    setError("");
  };

  const handlePopupClose = () => {
    setShowTierPopup(false);
    navigate("/home");
  };

  return (
    <div>
      {showTierPopup && (
        <TierChangePopup
          oldTier={tierChangeData.oldTier}
          newTier={tierChangeData.newTier}
          onClose={handlePopupClose}
        />
      )}
      <div className="header-bar">
        <img src={logo} className="corner-logo" alt="logo" />
      </div>
      <div className="auth-container">
        <div className="form-wrapper">
          <div className="card">
            <div className="card-header">
              <h4>{isLogin ? "Login" : "Sign Up"}</h4>
            </div>
            <div className="card-body">
              {/* Back to Landing Page Button */}
              <div style={{ textAlign: "center", marginBottom: "15px" }}>
                <button
                  type="button"
                  onClick={() => navigate("/landing")}
                  style={{
                    padding: "10px 20px",
                    background: "#3f7fc9",
                    color: "#fff",
                    border: "none",
                    borderRadius: "8px",
                    cursor: "pointer",
                    fontWeight: "600",
                  }}
                >
                  Back to Landing Page
                </button>
              </div>
              {error && <div className="error-message">{error}</div>}
              <form onSubmit={handleSubmit} className="auth-form">
                {!isLogin && (
                  <div className="name-row">
                    <input
                      type="text"
                      name="fullName"
                      value={formData.fullName}
                      onChange={handleInputChange}
                      placeholder="Full Name"
                      className="form-input"
                      required={!isLogin}
                    />
                  </div>
                )}
                {!isLogin && (
                  <input
                    type="text"
                    name="username"
                    value={formData.username}
                    onChange={handleInputChange}
                    placeholder="username"
                    className="form-input"
                    required={!isLogin}
                  />
                )}
                {/* Address fields for signup */}

                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleInputChange}
                  placeholder="Email"
                  className="form-input"
                  required
                />
                <input
                  type="password"
                  name="password"
                  value={formData.password}
                  onChange={handleInputChange}
                  placeholder="Password"
                  className="form-input"
                  required
                />
                {!isLogin && (
                  <input
                    type="password"
                    name="confirmPassword"
                    value={formData.confirmPassword}
                    onChange={handleInputChange}
                    placeholder="Confirm Password"
                    className="form-input"
                    required={!isLogin}
                  />
                )}

                {!isLogin && (
                  <div className="address-row">
                    <input
                      type="text"
                      name="street"
                      value={formData.street}
                      onChange={handleInputChange}
                      placeholder="Street"
                      className="form-input"
                      required={!isLogin}
                    />
                    <input
                      type="text"
                      name="city"
                      value={formData.city}
                      onChange={handleInputChange}
                      placeholder="City"
                      className="form-input"
                      required={!isLogin}
                    />
                    <input
                      type="text"
                      name="postalCode"
                      value={formData.postalCode}
                      onChange={handleInputChange}
                      placeholder="Postal Code"
                      className="form-input"
                      required={!isLogin}
                    />
                  </div>
                )}
                {!isLogin && (
                  <div className="payment-row">
                    <input
                      type="text"
                      name="cardHolderName"
                      value={formData.cardHolderName}
                      onChange={handleInputChange}
                      placeholder="Card Holder Name"
                      className="form-input"
                      required={!isLogin}
                    />
                    <input
                      type="text"
                      name="cardNumber"
                      value={formData.cardNumber}
                      onChange={handleInputChange}
                      placeholder="Card Number"
                      className="form-input"
                      required={!isLogin}
                    />
                    <div className="expiry-cvc-row">
                      <input
                        type="text"
                        name="expiryMonth"
                        value={formData.expiryMonth}
                        onChange={handleInputChange}
                        placeholder="MM"
                        className="form-input expiry-input"
                        required={!isLogin}
                      />
                      <input
                        type="text"
                        name="expiryYear"
                        value={formData.expiryYear}
                        onChange={handleInputChange}
                        placeholder="YY"
                        className="form-input expiry-input"
                        required={!isLogin}
                      />
                      <input
                        type="text"
                        name="cvc"
                        value={formData.cvc}
                        onChange={handleInputChange}
                        placeholder="CVC"
                        className="form-input"
                        required={!isLogin}
                      />
                    </div>
                  </div>
                )}
                <button
                  type="submit"
                  className="submit-btn"
                  disabled={isLoading}
                >
                  {isLoading ? "Processing..." : isLogin ? "Login" : "Sign Up"}
                </button>
              </form>
              <div className="toggle-section">
                <p>
                  {isLogin
                    ? "Don't have an account? "
                    : "Already have an account? "}
                  <button
                    type="button"
                    onClick={toggleMode}
                    className="toggle-btn"
                  >
                    {isLogin ? "Sign Up" : "Login"}
                  </button>
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
      {showTierPopup && (
        <TierChangePopup
          oldTier={tierChangeData.oldTier}
          newTier={tierChangeData.newTier}
          onClose={handlePopupClose}
        />
      )}
    </div>
  );
};

export default Auth;
