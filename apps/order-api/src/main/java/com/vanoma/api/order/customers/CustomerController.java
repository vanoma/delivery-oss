package com.vanoma.api.order.customers;

import com.vanoma.api.utils.PagedResources;
import com.vanoma.api.order.utils.annotations.PatchMappingJson;
import com.vanoma.api.order.utils.annotations.PostMappingJson;
import com.vanoma.api.order.utils.annotations.RequestMappingJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMappingJson
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private BranchRepository branchRepository;
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private ICustomerService customerService;

    @GetMapping(value = "/customers")
    public ResponseEntity<PagedResources<Customer>> getCustomers(CustomerFilter filter, Pageable pageable) {
        return ResponseEntity.ok(PagedResources.of(this.customerRepository.findAll(filter.getSpec(), pageable)));
    }

    @PostMappingJson(value = "/customers")
    public ResponseEntity<Customer> createCustomer(@RequestBody CustomerJson json) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.customerService.createCustomer(json));
    }

    @GetMapping(value = "/customers/{customerId}")
    public ResponseEntity<Customer> getCustomer(@PathVariable String customerId) {
        return ResponseEntity.ok(this.customerRepository.getById(customerId));
    }

    @PatchMappingJson(value = "/customers/{customerId}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable String customerId, @RequestBody CustomerJson json) {
        return ResponseEntity.ok(this.customerService.updateCustomer(customerId, json));
    }

    @GetMapping(value = "/customers/{customerId}/branches")
    public ResponseEntity<PagedResources<Branch>> getBranches(@PathVariable String customerId,
                                                              Pageable pageable) {
        Page<Branch> page = this.branchRepository.findAllByCustomerCustomerIdAndIsDeleted(customerId, false, pageable);
        return ResponseEntity.ok(PagedResources.of(page));
    }

    @PostMappingJson(value = "/customers/{customerId}/branches")
    public ResponseEntity<Branch> createBranch(@PathVariable String customerId,
                                               @RequestBody BranchJson json) {
        Branch branch = this.customerService.createBranch(customerId, json);
        return ResponseEntity.status(HttpStatus.CREATED).body(branch);
    }

    @GetMapping(value = "/branches/{branchId}")
    public ResponseEntity<Branch> getBranch(@PathVariable String branchId) {
        return ResponseEntity.ok(this.branchRepository.getById(branchId));
    }

    @PatchMappingJson(value = "/branches/{branchId}")
    public ResponseEntity<Branch> updateBranch(@PathVariable String branchId, @RequestBody BranchJson json) {
        return ResponseEntity.ok(this.customerService.updateBranch(branchId, json));
    }

    @DeleteMapping(value = "/branches/{branchId}")
    public ResponseEntity<Void> deleteBranch(@PathVariable String branchId) {
        this.customerService.deleteBranch(branchId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/customers/{customerId}/agents")
    public ResponseEntity<PagedResources<Agent>> getAgents(@PathVariable String customerId,
                                                           Pageable pageable) {
        Page<Agent> page = this.agentRepository.findAllByCustomerCustomerIdAndIsDeleted(customerId, false, pageable);
        return ResponseEntity.ok(PagedResources.of(page));
    }

    @PostMappingJson(value = "/customers/{customerId}/agents")
    public ResponseEntity<Agent> createAgent(@PathVariable String customerId,
                                               @RequestBody AgentJson json) {
        Agent agent = this.customerService.createAgent(customerId, json);
        return ResponseEntity.status(HttpStatus.CREATED).body(agent);
    }

    @GetMapping(value = "/agents/{agentId}")
    public ResponseEntity<Agent> getAgent(@PathVariable String agentId) {
        return ResponseEntity.ok(this.agentRepository.getById(agentId));
    }

    @PatchMappingJson(value = "/agents/{agentId}")
    public ResponseEntity<Agent> updateAgent(@PathVariable String agentId, @RequestBody AgentJson json) {
        return ResponseEntity.ok(this.customerService.updateAgent(agentId, json));
    }

    @DeleteMapping(value = "/agents/{agentId}")
    public ResponseEntity<Void> deleteAgent(@PathVariable String agentId) {
        this.customerService.deleteAgent(agentId);
        return ResponseEntity.noContent().build();
    }
}
