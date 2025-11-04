import React from 'react';

const HomeHeader = ({ fullName, role, handleLogout }) => {
    return (
        <header style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
            <h1>Hello{fullName ? `, ${fullName}` : ''}!</h1>
            <h1>Hello{role ? `, ${role}` : ''}!</h1>

            <button type="button" onClick={handleLogout} style={{padding: '8px 12px', cursor: 'pointer'}}>
                Logout
            </button>
        </header>
    );
};

export default HomeHeader;