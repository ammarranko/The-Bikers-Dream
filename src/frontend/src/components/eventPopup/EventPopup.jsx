import React from "react";
import './EventPopup.css';

function EventPopup({ message, onCancel }) {
  return (
    <>
      {/* Dark overlay background */}
      <div className="modal-overlay" onClick={onCancel} />
      
      {/* Modal box */}
      <div className="modal-box">
        <p>{message}</p>
        <div className="modal-buttons">
          <button className="close-btn" onClick={onCancel}>Close</button>
        </div>
      </div>
    </>
  );
}

export default EventPopup;
