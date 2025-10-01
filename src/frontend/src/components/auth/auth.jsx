import React, { useState } from "react";
import './auth.css';
import logo from '../assets/logo.png';

const Auth = () => {
    const [isLogin, setIsLogin] = useState(true);
    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        email: '',
        password: '',
        confirmPassword: ''
    });

    const handleInputChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const toggleMode = () => {
        setIsLogin(!isLogin);
        setFormData({
            firstName: '',
            lastName: '',
            email: '',
            password: '',
            confirmPassword: ''
        });
    };

    return (
        <div>
            <div className="header-bar">
                <img src={logo} className="corner-logo" alt="logo"/>
            </div>

            <div className="auth-container">
                <div className="form-wrapper">
                    <div className="card">
                        <div className="card-header">
                            <h4>{isLogin ? 'Login' : 'Sign Up'}</h4>
                        </div>
                        <div className="card-body">
                            <form>
                                {!isLogin && (
                                    <div className="name-row">
                                        <input
                                            type="text"
                                            name="firstName"
                                            value={formData.firstName}
                                            onChange={handleInputChange}
                                            placeholder="First Name"
                                            className="form-input"
                                        />
                                        <input
                                            type="text"
                                            name="lastName"
                                            value={formData.lastName}
                                            onChange={handleInputChange}
                                            placeholder="Last Name"
                                            className="form-input"
                                        />
                                    </div>
                                )}

                                <input
                                    type="email"
                                    name="email"
                                    value={formData.email}
                                    onChange={handleInputChange}
                                    placeholder="Email"
                                    className="form-input"
                                />

                                <input
                                    type="password"
                                    name="password"
                                    value={formData.password}
                                    onChange={handleInputChange}
                                    placeholder="Password"
                                    className="form-input"
                                />

                                {!isLogin && (
                                    <input
                                        type="password"
                                        name="confirmPassword"
                                        value={formData.confirmPassword}
                                        onChange={handleInputChange}
                                        placeholder="Confirm Password"
                                        className="form-input"
                                    />
                                )}

                                <button type="submit" className="submit-btn">
                                    {isLogin ? 'Login' : 'Sign Up'}
                                </button>
                            </form>

                            <div className="toggle-section">
                                <p>
                                    {isLogin ? "Don't have an account? " : "Already have an account? "}
                                    <button type="button" onClick={toggleMode} className="toggle-btn">
                                        {isLogin ? 'Sign Up' : 'Login'}
                                    </button>
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    );
}

export default Auth;