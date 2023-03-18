package com.vanoma.api.order.customers;

public interface ICustomerService {
    Customer createCustomer(CustomerJson json);

    Customer updateCustomer(String customerId, CustomerJson json);

    Branch createBranch(String customerId, BranchJson json);

    Branch updateBranch(String branchId, BranchJson json);

    void deleteBranch(String branchId);

    Agent createAgent(String customerId, AgentJson json);

    Agent updateAgent(String agentId, AgentJson json);

    void deleteAgent(String agentId);
}
