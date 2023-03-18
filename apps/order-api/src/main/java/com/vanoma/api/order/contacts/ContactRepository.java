package com.vanoma.api.order.contacts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public
interface ContactRepository extends JpaRepository<Contact, String> {
    List<Contact> findByCustomerIdAndIsSavedOrderByNameAsc(String customerId, Boolean isSaved);

    Contact findFirstByCustomerIdAndPhoneNumberOne(String customerId, String phoneNumberOne);

    Contact findFirstByCustomerIdAndIsSavedAndIsDefault(String customerId, boolean isSaved, boolean isDefault);

    Contact findFirstByCustomerIdAndPhoneNumberOneAndIsSaved(String customerId, String phoneNumberOne, Boolean isSaved);
}
