package com.vanoma.api.order.customers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchRepository  extends JpaRepository<Branch, String> {
    Page<Branch> findAllByCustomerCustomerIdAndIsDeleted(String customerId, boolean isDeleted, Pageable pageable);

    List<Branch> findAllByCustomer(Customer customer);

    Branch findFirstByIsDeleted(boolean isDeleted);
}
