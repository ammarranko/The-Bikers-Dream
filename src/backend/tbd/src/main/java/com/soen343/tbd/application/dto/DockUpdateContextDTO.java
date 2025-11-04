package com.soen343.tbd.application.dto;

import com.soen343.tbd.application.dto.StationDetailsDTO.DockWithBikeDTO;

// Dock context response object for live dock updates
public class DockUpdateContextDTO {
        private final Long stationId;
        private final String stationName;
        private final DockWithBikeDTO dock;

        public DockUpdateContextDTO(Long stationId, String stationName, DockWithBikeDTO dock) {
            this.stationId = stationId;
            this.stationName = stationName;
            this.dock = dock;
        }

        // Getters needed for JSON serialization
        public Long getStationId() { return stationId; }
        public String getStationName() { return stationName; }
        public DockWithBikeDTO getDock() { return dock; }
}