package com.vanoma.api.order.charges;

import com.vanoma.api.order.packages.PackageRepository;
import com.vanoma.api.utils.PagedResources;
import com.vanoma.api.utils.exceptions.UnauthorizedAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public class ChargeService implements IChargeService {

    @Autowired
    private ChargeRepository chargeRepository;
    @Autowired
    private PackageRepository packageRepository;

    @Override
    public Charge createCharge(String packageId, ChargeJson chargeJson) {
        chargeJson.validate();
        // Charges that are not delivery fee are inclusive of the transaction fee.
        BigDecimal transactionAmount = ChargeUtils.computeTransactionAmountGivenTotalAmount(chargeJson.getTransactionAmount());
        Charge charge = new Charge(this.packageRepository.getById(packageId))
                .setType(chargeJson.getType())
                .setStatus(ChargeStatus.UNPAID)
                .setDescription(chargeJson.getDescription())
                .setTransactionAmount(transactionAmount);
        return this.chargeRepository.save(charge);
    }

    @Override
    public PagedResources<Charge> getCharges(ChargeFilter filter, Pageable pageable) {
        if (!filter.isEmpty()) {
            Page<Charge> charges = this.chargeRepository.findAll(filter.getSpec(), pageable);
            return PagedResources.of(charges);
        }

        // TODO: Return all charges if the request was made by staff user.
        throw new UnauthorizedAccessException("crud.charges.unauthorizedGetAll");
    }
}
