package com.vanoma.api.order.contacts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactAddressRepository extends JpaRepository<ContactAddress, String> {
    long count();

    long countByContact(Contact contact);

    long countByCustomerId(String customerId);

    List<ContactAddress> findByContact(Contact contact);

    List<ContactAddress> findByCustomerId(String customerId);

    ContactAddress findFirstByContactAndAddress(Contact contact, Address address);

    List<ContactAddress> findByContactOrderByCreatedAtDesc(Contact contact);

    List<ContactAddress> findByCustomerIdOrderByCreatedAtDesc(String customerId);
}
