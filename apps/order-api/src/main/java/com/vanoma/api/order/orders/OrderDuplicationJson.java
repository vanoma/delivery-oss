package com.vanoma.api.order.orders;

import com.vanoma.api.utils.input.TimeUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
public class OrderDuplicationJson implements Serializable {
    private String pickUpStart;

    public OffsetDateTime getPickUpStart() {
        return TimeUtils.parseISOString(pickUpStart);
    }
}