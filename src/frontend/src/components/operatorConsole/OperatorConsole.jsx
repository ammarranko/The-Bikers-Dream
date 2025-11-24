import React, { useState, useEffect, useRef } from "react";
import axios from "axios";
import "./OperatorConsole.css";

const OperatorConsole = ({ operatorEvents = [] }) => {
  const eventListRef = useRef(null);
  const [autoScroll, setAutoScroll] = useState(true);

  // Auto-scroll to bottom when new events arrive (if autoScroll is enabled)
  useEffect(() => {
    if (autoScroll && eventListRef.current) {
      eventListRef.current.scrollTop = eventListRef.current.scrollHeight;
    }
  }, [operatorEvents, autoScroll]);

  // Handle manual scrolling - disable auto-scroll if user scrolls up
  const handleScroll = () => {
    if (eventListRef.current) {
      const { scrollTop, scrollHeight, clientHeight } = eventListRef.current;
      const isAtBottom = Math.abs(scrollHeight - clientHeight - scrollTop) < 10;
      setAutoScroll(isAtBottom);
    }
  };

  // Format entity type for display
  const formatEntityType = (entityType) => {
    if (!entityType) return "Unknown";
    return entityType.charAt(0).toUpperCase() + entityType.slice(1).toLowerCase();
  };

  // Get icon based on entity type
  const getEntityIcon = (entityType) => {
    switch (entityType?.toUpperCase()) {
      case "BIKE":
        return "🚲";
      case "STATION":
        return "🏢";
      case "DOCK":
        return "🔌";
      case "USER":
        return "👤";
      default:
        return "📝";
    }
  };

  // Get color class based on action/state
  const getEventTypeClass = (event) => {
    const newState = event.newState?.toUpperCase();
    const metadata = event.metadata?.toLowerCase() || "";

    if (newState === "MAINTENANCE" || metadata.includes("maintenance")) {
      return "event-maintenance";
    }
    if (newState === "RENTED" || metadata.includes("rent")) {
      return "event-rental";
    }
    if (newState === "AVAILABLE" || metadata.includes("return")) {
      return "event-available";
    }
    if (newState === "RESERVED" || metadata.includes("reserv")) {
      return "event-reserved";
    }
    if (newState === "OUT_OF_SERVICE" || metadata.includes("out of service")) {
      return "event-outofservice";
    }
    if (newState === "ACTIVE" || metadata.includes("active")) {
      return "event-active";
    }
    if (newState === "STATION_EMPTY" || metadata.includes("alert")) {
        return "event-stationempty";
    }
    if (newState === "STATION_FULL" || metadata.includes("alert")) {
        return "event-stationfull";
    }

    return "event-default";
  };

  // Format timestamp to be more readable
  const formatTimestamp = (timestamp) => {
    const date = new Date(timestamp);
    const now = new Date();
    const isToday = date.toDateString() === now.toDateString();

    if (isToday) {
      return date.toLocaleTimeString([], { 
        hour: '2-digit', 
        minute: '2-digit',
        second: '2-digit'
      });
    }
    return date.toLocaleString([], { 
      month: 'short',
      day: 'numeric',
      hour: '2-digit', 
      minute: '2-digit'
    });
  };

  // Clean up and format the description
  const formatDescription = (event) => {
    if (event.metadata) {
      return event.metadata;
    }
    
    // Fallback: construct description from available data
    const entity = formatEntityType(event.entityType);
    const id = event.entityId;
    const state = event.newState;
    
    return `${entity} #${id} changed to ${state}`;
  };

  const handleReset = async () => {
    if (window.confirm("Are you sure you want to reset the system? This will delete all bills, reservations, trips, and rebalance stations.")) {
      try {
        await axios.post("http://localhost:8080/api/admin/reset");
        alert("System reset successfully.");
        window.location.reload();
      } catch (error) {
        console.error("Failed to reset system:", error);
        alert("Failed to reset system. See console for details.");
      }
    }
  };

  return (
    <div className="operator-console">
      <div className="console-header">
        <h3 className="console-title">
          <span className="console-icon">📊</span>
          Events Console
        </h3>
        <div className="console-stats">
          <button 
            className="reset-system-btn"
            onClick={handleReset}
            title="Reset System"
          >
            Reset System
          </button>
          <span className="event-count">{operatorEvents.length} events</span>
          {!autoScroll && (
            <button 
              className="scroll-to-bottom-btn"
              onClick={() => setAutoScroll(true)}
              title="Scroll to latest event"
            >
              ⬇️
            </button>
          )}
        </div>
      </div>
      
      <div 
        className="event-list" 
        ref={eventListRef}
        onScroll={handleScroll}
      >
        {operatorEvents.length > 0 ? (
          operatorEvents.map((event, index) => (
            <div 
              key={`${event.eventId}-${index}`} 
              className={`event-item ${getEventTypeClass(event)}`}
            >
              <div className="event-icon">
                {getEntityIcon(event.entityType)}
              </div>
              
              <div className="event-content">
                <div className="event-header">
                  <span className="event-entity">
                    {formatEntityType(event.entityType)} #{event.entityId}
                  </span>
                  <span className="event-timestamp">
                    {formatTimestamp(event.occuredAt)}
                  </span>
                </div>
                
                <div className="event-description">
                  {formatDescription(event)}
                </div>
                
                {event.previousState && event.newState && (
                  <div className="event-status-change">
                    <span className="status-badge status-old">
                      {event.previousState}
                    </span>
                    <span className="status-arrow">→</span>
                    <span className="status-badge status-new">
                      {event.newState}
                    </span>
                  </div>
                )}
                
                {event.triggeredBy && (
                  <div className="event-footer">
                    <span className="triggered-by">
                      👤 {event.triggeredBy}
                    </span>
                  </div>
                )}
              </div>
            </div>
          ))
        ) : (
          <div className="no-events">
            <div className="no-events-icon">📭</div>
            <p className="no-events-title">No events yet</p>
            <p className="no-events-subtitle">
              System events will appear here in real-time
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default OperatorConsole;