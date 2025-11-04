import React from "react";
import './ConfirmationPopup.css';

function ConfirmationPopup({ message, onConfirm, onCancel }) {
  return (
    <>
      {/* Dark overlay background */}
      <div className="modal-overlay" onClick={onCancel} />
      
      {/* Modal box */}
      <div className="modal-box">
        <p>{message}</p>
        <div className="modal-buttons">
          <button className="confirm-btn" onClick={onConfirm}>Yes</button>
          <button className="cancel-btn" onClick={onCancel}>No</button>
        </div>
      </div>
    </>
  );
}

export default ConfirmationPopup;
