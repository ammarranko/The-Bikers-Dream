import React, { useState } from "react";
import "./auth.css";
import logo from "../assets/logo.png";
import axios from "axios";

const Auth = () => {
  const [isLogin, setIsLogin] = useState(true);
  const [formData, setFormData] = useState({
    email: "",
    password: "",
  });

  const handleInputChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };
  const handleSubmit = async (e) => {
    e.preventDefault();

    // Concatenation address, city and postal code into a single address line, which will be address' value
    const fullAddress = [
      formData.address,
      formData.city,
      formData.postalCode,
    ].join(", ");

    console.log(fullAddress);

    // Handle form submission logic here
    const data = {
      firstName: formData.firstName,
      lastName: formData.lastName,
      username: formData.username,
      email: formData.email,
      password: formData.password,
      confirmPassword: formData.confirmPassword,
      address: fullAddress,
    };

    try {
      //sending request to the backend
      const response = await axios.post(
        "http://localhost:8080/api/login",
        data
      );
      if (response.data === false) {
        alert("Invalid Credentials");
      } else {
        alert("Login Successful");
      }
    } catch (error) {
      console.error("There was an error!", error);
    }
  };

  const toggleMode = () => {
    setIsLogin(!isLogin);
    setFormData({
      firstName: "",
      lastName: "",
      username: "",
      email: "",
      password: "",
      confirmPassword: "",
      address: "",
      city: "",
      postalCode: "",
    });
  };

  return (
    <div>
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
              <form onSubmit={handleSubmit} className="auth-form">
                {!isLogin && (
                  <div className="name-row">
                    <input
                      type="text"
                      name="firstName"
                      value={formData.firstName}
                      onChange={handleInputChange}
                      placeholder="First Name"
                      className="form-input"
                      required
                    />
                    <input
                      type="text"
                      name="lastName"
                      value={formData.lastName}
                      onChange={handleInputChange}
                      placeholder="Last Name"
                      className="form-input"
                      required
                    />
                  </div>
                )}
                {!isLogin && (
                  <input
                    type="text"
                    name="username"
                    value={formData.username}
                    onChange={handleInputChange}
                    placeholder="Username"
                    className="form-input"
                    required
                  />
                )}

                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleInputChange}
                  placeholder="Email"
                  className="form-input"
                  required
                />
                <div className="name-row">
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
                      required
                    />
                  )}
                </div>

                {!isLogin && (
                  <input
                    type="text"
                    name="address"
                    value={formData.address}
                    onChange={handleInputChange}
                    placeholder="Address"
                    className="form-input"
                    required
                  />
                )}
                {!isLogin && (
                  <div className="name-row">
                    <input
                      type="text"
                      name="city"
                      value={formData.city}
                      onChange={handleInputChange}
                      placeholder="City"
                      className="form-input"
                      required
                    />

                    <input
                      type="text"
                      name="postalCode"
                      value={formData.postalCode}
                      onChange={handleInputChange}
                      placeholder="Postal Code"
                      className="form-input"
                      required
                    />
                  </div>
                )}

                <button type="submit" className="submit-btn">
                  {isLogin ? "Login" : "Sign Up"}
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
    </div>
  );
};

export default Auth;
