package com.vanoma.api.order.contacts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public
interface AddressRepository extends JpaRepository<Address, String> {
    Address findFirstByCustomerIdAndIsSavedAndIsDefault(String customerId, boolean isSaved, boolean isDefault);
}
