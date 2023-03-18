package com.vanoma.api.order.customers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentRepository extends JpaRepository<Agent, String>, JpaSpecificationExecutor<Agent> {
    Agent findFirstByCustomerAndPhoneNumberAndIsDeleted(Customer customer, String phoneNumber, boolean isDeleted);

    Page<Agent> findAllByCustomerCustomerIdAndIsDeleted(String customerId, boolean isDeleted, Pageable pageable);

    List<Agent> findAllByCustomer(Customer customer);

    List<Agent> findAllByBranch(Branch branch);
}
