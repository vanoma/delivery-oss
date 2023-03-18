package com.vanoma.api.order.events;

import com.vanoma.api.utils.exceptions.InvalidParameterException;

public enum EventName {
    ORDER_PLACED, DRIVER_ASSIGNED, DRIVER_CONFIRMED, DRIVER_DEPARTING_PICK_UP,
    DRIVER_ARRIVED_PICK_UP, PACKAGE_PICKED_UP, DRIVER_DEPARTING_DROP_OFF,
    DRIVER_ARRIVED_DROP_OFF, PACKAGE_DELIVERED, PACKAGE_CANCELLED;

    public static EventName create(String eventName) {
        if (eventName != null && !isValid(eventName)) {
            throw new InvalidParameterException("crud.packageEvent.eventName.invalid");
        } else if (eventName == null) {
            return null;
        } else {
            return EventName.valueOf(eventName);
        }
    }

    private static boolean isValid(String eventName) {
        if (eventName == null) return false;
        for (EventName p : EventName.values()) {
            if (eventName.equals(p.name())) return true;
        }
        return false;
    }
}
