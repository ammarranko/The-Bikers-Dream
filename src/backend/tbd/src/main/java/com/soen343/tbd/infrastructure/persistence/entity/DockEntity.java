package com.soen343.tbd.infrastructure.persistence.entity;

import com.soen343.tbd.domain.model.enums.DockStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "docks")
public class DockEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dock_id")
    private Long dockId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", referencedColumnName = "station_id")
    private StationEntity station;

    @Enumerated(EnumType.STRING)
    @Column(name = "dock_status", nullable = false)
    private DockStatus status;

    
    /* 
    -----------------------
      GETTERS AND SETTERS 
    -----------------------
    */

    public Long getDockId() {
        return dockId;
    }

    public void setDockId(Long dockId) {
        this.dockId = dockId;
    }

    public StationEntity getStation() {
        return station;
    }

    public void setStation(StationEntity station) {
        this.station = station;
    }

    public DockStatus getStatus() {
        return status;
    }

    public void setStatus(DockStatus status) {
        this.status = status;
    }
}
