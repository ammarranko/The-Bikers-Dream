package com.soen343.tbd.domain.model;

import com.soen343.tbd.domain.model.enums.DockStatus;
import com.soen343.tbd.domain.model.ids.DockId;
import com.soen343.tbd.domain.model.ids.StationId;

public class Dock {
    private DockId dockId;
    private StationId stationId;
    private DockStatus status;

    public Dock(DockId dockId, StationId stationId, DockStatus status) {
        this.dockId = dockId;
        this.stationId = stationId;
        this.status = status;
    }

    public DockId getDockId() {
        return dockId;
    }

    public void setDockId(DockId dockId) {
        this.dockId = dockId;
    }

    public StationId getStationId() {
        return stationId;
    }

    public void setStationId(StationId stationId) {
        this.stationId = stationId;
    }

    public DockStatus getStatus() {
        return status;
    }

    public void setStatus(DockStatus status) {
        this.status = status;
    }
}
