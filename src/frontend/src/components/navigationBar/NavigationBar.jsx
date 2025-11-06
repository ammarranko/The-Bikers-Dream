import React, { useState, useEffect, useRef } from 'react';
import './NavigationBar.css';

function NavigationBar({ fullName, role, handleLogout, handleBillingClick, handleHomeClick, activePage }) {
    const [isOpen, setIsOpen] = useState(false);
    const sidebarRef = useRef(null);
    const initials = fullName
        ? fullName
            .split(' ')
            .map(name => name[0])
            .join('')
            .toUpperCase()
        : 'U';  // Default to 'U' for User if fullName is null/undefined

    // Toggle sidebar when clicking the button
    const handleToggle = (e) => {
        e.stopPropagation(); // prevent closing immediately
        setIsOpen(!isOpen);
    };

    // Close sidebar if clicking outside of it
    useEffect(() => {
        const handleClickOutside = (event) => {
        if (
            isOpen &&
            sidebarRef.current &&
            !sidebarRef.current.contains(event.target)
        ) {
            setIsOpen(false);
        }
    };

    document.addEventListener("click", handleClickOutside);

    // Cleanup listener on unmount
    return () => {
        document.removeEventListener("click", handleClickOutside);
    };
}, [isOpen]);

    return (
        <>
            <button className="nav-toggle" onClick={(e) => handleToggle(e)}>
                <i className="fas fa-bars"></i>
            </button>
            <nav
                ref={sidebarRef}
                className={`nav-sidebar ${isOpen ? "open" : ""}`}
            >
                <div className="nav-content">
                    <ul className="nav-menu">
                        <li className={`nav-item ${activePage === 'home' ? 'active' : ''}`} onClick={handleHomeClick}>
                            <i className="fas fa-home"></i>
                            Home
                        </li>
                        <li className={`nav-item ${activePage === 'billing' ? 'active' : ''}`} onClick={handleBillingClick}>
                            <i className="fas fa-file-invoice-dollar"></i>
                            My Bills
                        </li>
                        <li className="nav-item">
                            <i className="fas fa-history"></i>
                            History
                        </li>
                        <li className="nav-item">
                            <i className="fas fa-cog"></i>
                            Settings
                        </li>
                        <li className="nav-item" onClick={handleLogout}>
                            <i className="fas fa-sign-out-alt"></i>
                            Logout
                        </li>
                    </ul>

                    <div className="user-profile">
                        <div className="profile-image">
                            {initials}
                        </div>
                        <div className="profile-info">
                            <p className="profile-name">{fullName || 'User'}</p>
                            <p className="profile-role">{role || 'Guest'}</p>
                        </div>
                    </div>
                </div>
            </nav>
        </>
    );
}

export default NavigationBar;