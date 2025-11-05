package com.soen343.tbd.domain.model.enums;

public enum EntityStatus {
    // General NONE status, for when a entity is created (used as previous state)
    NONE,
    // BikeStatus
    AVAILABLE, RESERVED, ON_TRIP, MAINTENANCE,
    // TripStatus
    ONGOING, COMPLETED,
    // StationStatus
    ACTIVE, OUT_OF_SERVICE,
    // ReservationStatus
    RES_ACTIVE, RES_COMPLETED, CANCELLED, EXPIRED,
    // DockStatus
    EMPTY, OCCUPIED, DOCK_OUT_OF_SERVICE;

    public static EntityStatus fromSpecificStatus(Object status) {
        if (status instanceof BikeStatus) {
            return switch ((BikeStatus) status) {
                case AVAILABLE -> AVAILABLE;
                case RESERVED -> RESERVED;
                case ON_TRIP -> ON_TRIP;
                case MAINTENANCE -> MAINTENANCE;
            };
        } else if (status instanceof TripStatus) {
            return switch ((TripStatus) status) {
                case ONGOING -> ONGOING;
                case COMPLETED -> COMPLETED;
            };
        } else if (status instanceof StationStatus) {
            return switch ((StationStatus) status) {
                case ACTIVE -> ACTIVE;
                case OUT_OF_SERVICE -> OUT_OF_SERVICE;
            };
        } else if (status instanceof ReservationStatus) {
            return switch ((ReservationStatus) status) {
                case ACTIVE -> RES_ACTIVE;
                case COMPLETED -> RES_COMPLETED;
                case CANCELLED -> CANCELLED;
                case EXPIRED -> EXPIRED;
            };
        } else if (status instanceof DockStatus) {
            return switch ((DockStatus) status) {
                case EMPTY -> EMPTY;
                case OCCUPIED -> OCCUPIED;
                case OUT_OF_SERVICE -> DOCK_OUT_OF_SERVICE;
            };
        } else {
            throw new IllegalArgumentException("Unsupported status type");
        }
    }
}
