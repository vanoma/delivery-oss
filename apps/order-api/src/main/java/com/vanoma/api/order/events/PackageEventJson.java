package com.vanoma.api.order.events;

import com.vanoma.api.utils.exceptions.InvalidParameterException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PackageEventJson {

    private String eventName;
    private String assignmentId;

    public EventName getEventName() {
        return EventName.create(eventName);
    }

    public void validate() {
        if (eventName == null) {
            throw new InvalidParameterException("crud.packageEvent.eventName.missing");
        }

        if (assignmentId == null) {
            throw new InvalidParameterException("crud.packageEvent.assignmentId.missing");
        }

        if (getEventName() == EventName.ORDER_PLACED || getEventName() == EventName.PACKAGE_CANCELLED) {
            throw new InvalidParameterException("crud.packageEvent.eventName.notAllowed");
        }
    }
}
