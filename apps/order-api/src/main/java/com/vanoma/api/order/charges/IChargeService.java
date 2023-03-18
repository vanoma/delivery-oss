package com.vanoma.api.order.charges;

import com.vanoma.api.utils.PagedResources;
import org.springframework.data.domain.Pageable;

public interface IChargeService {

    Charge createCharge(String packageId, ChargeJson chargeJson);

    PagedResources<Charge> getCharges(ChargeFilter filter, Pageable pageable);
}
