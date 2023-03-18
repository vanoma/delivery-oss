package com.vanoma.api.order.customers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "agent")
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class Agent {
    @Id
    @Column(name = "agent_id", nullable = false)
    private String agentId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "is_root", nullable = false)
    private Boolean isRoot;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "branch_id", referencedColumnName = "branch_id")
    private Branch branch;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public Agent() { }

    public Agent(Customer customer) {
        this.agentId = UUID.randomUUID().toString();
        this.customer = customer;
        this.isRoot = false;
        this.isDeleted = false;
    }

    public Agent(Branch branch) {
        this(branch.getCustomer());
        this.branch = branch;
    }

    public String getAgentId() {
        return agentId;
    }

    public Agent setFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    public String getFullName() {
        return fullName;
    }

    public Agent setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Agent setIsRoot(boolean isRoot) {
        this.isRoot = isRoot;
        return this;
    }

    public Boolean getIsRoot() {
        return isRoot;
    }

    public Agent setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
        return this;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    @JsonIgnore
    public Customer getCustomer() {
        return customer;
    }

    public Agent setBranch(Branch branch) {
        this.branch = branch;
        return this;
    }

    public Branch getBranch() {
        return branch;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Agent &&
                this.agentId.equals(((Agent) other).getAgentId());
    }

    @Override
    public String toString() {
        String branchStr = "AGENT\n(" +
                "\tagentId = " + this.agentId + "\n";
        if (fullName != null) branchStr += "\tfullName = " + fullName + "\n";
        if (phoneNumber != null) branchStr += "\tphoneNumber = " + phoneNumber + "\n";
        if (isRoot != null) branchStr += "\tisRoot = " + isRoot + "\n";
        return branchStr + ")";
    }
}
