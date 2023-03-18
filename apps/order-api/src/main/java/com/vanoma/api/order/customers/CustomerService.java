package com.vanoma.api.order.customers;

import com.vanoma.api.order.contacts.AddressRepository;
import com.vanoma.api.order.contacts.Contact;
import com.vanoma.api.order.contacts.ContactRepository;
import com.vanoma.api.order.external.IAuthApiCaller;
import com.vanoma.api.order.external.ICommunicationApiCaller;
import com.vanoma.api.order.external.IPaymentAPICaller;
import com.vanoma.api.order.external.PaymentMethodParams;
import com.vanoma.api.utils.exceptions.InvalidParameterException;
import com.vanoma.api.utils.httpwrapper.HttpResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CustomerService implements ICustomerService {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private BranchRepository branchRepository;
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private IAuthApiCaller authApiCaller;
    @Autowired
    private IPaymentAPICaller paymentAPICaller;
    @Autowired
    private ICommunicationApiCaller communicationApiCaller;

    @Override
    @Transactional
    public Customer createCustomer(CustomerJson json) {
        json.validateForCreation();

        HttpResult otpResult = this.communicationApiCaller.verifyOtp(json.getOtpId(), json.getOtpCode(), json.getPhoneNumber());
        if (!otpResult.isSuccess()) {
            throw new InvalidParameterException("crud.otp.invalid");
        }

        Customer existingCustomer = this.customerRepository.getFirstByPhoneNumber(json.getPhoneNumber());
        if (existingCustomer != null) {
            throw new InvalidParameterException("crud.customer.existingCustomer");
        }

        Customer customer = this.customerRepository.save(new Customer()
                .setBusinessName(json.getBusinessName())
                .setPhoneNumber(json.getPhoneNumber()));

        // Create agent needed to log in
        createAgent(json.getBusinessName(), json.getPhoneNumber(), customer, null, true);

        // Create a default contact. Address will be created through the UI
        Contact contact = new Contact(customer.getCustomerId())
                .setPhoneNumberOne(json.getPhoneNumber())
                .setName(json.getBusinessName())
                .setIsDefault(true)
                .setIsSaved(true);
        this.contactRepository.save(contact);

        // Create a default payment method
        PaymentMethodParams params = PaymentMethodParams.builder()
                .customerId(customer.getCustomerId())
                .phoneNumber(customer.getPhoneNumber())
                .type("MOBILE_MONEY")
                .isDefault(true)
                .build();
        HttpResult paymentMethodResult = this.paymentAPICaller.createPaymentMethod(params);
        if (!paymentMethodResult.isSuccess()) {
            throw new InvalidParameterException("curd.paymentMethod.failedCreation");
        }

        return customer;
    }

    @Override
    public Customer updateCustomer(String customerId, CustomerJson json) {
        json.validateForUpdate();

        Customer customer = this.customerRepository.getById(customerId);
        if (Objects.nonNull(json.getWeightingFactor())) {
            customer.setWeightingFactor(json.getWeightingFactor());
        }
        if (Objects.nonNull(json.getBillingInterval())) {
            customer.setBillingInterval(json.getBillingInterval());
        }
        if (Objects.nonNull(json.getBillingGracePeriod())) {
            customer.setBillingGracePeriod(json.getBillingGracePeriod());
        }
        if (!Objects.equals(json.getPostpaidExpiry(), CustomerJson.NO_STRING_VALUE)) {
            if (Objects.nonNull(json.getPostpaidExpiry())) {
                customer.setPostpaidExpiry(OffsetDateTime.parse(json.getPostpaidExpiry()));
            } else {
                customer.setPostpaidExpiry(null);
            }
        }
        if (json.getFixedPriceAmount() != CustomerJson.NO_DECIMAL_VALUE) {
            if (Objects.nonNull(json.getFixedPriceAmount())) {
                customer.setFixedPriceAmount(json.getFixedPriceAmount());
            } else {
                customer.setFixedPriceAmount(null);
            }
        }
        if (!Objects.equals(json.getFixedPriceExpiry(), CustomerJson.NO_STRING_VALUE)) {
            if (Objects.nonNull(json.getFixedPriceExpiry())) {
                customer.setFixedPriceExpiry(OffsetDateTime.parse(json.getFixedPriceExpiry()));
            } else {
                customer.setFixedPriceExpiry(null);
            }
        }
        return this.customerRepository.save(customer);
    }

    @Override
    public Branch createBranch(String customerId, BranchJson json) {
        json.validate();
        Customer customer = this.customerRepository.getById(customerId);
        Branch branch = this.branchRepository.save(new Branch(customer)
                .setBranchName(json.getBranchName())
                .setContact(this.contactRepository.getById(json.getContactId()))
                .setAddress(this.addressRepository.getById(json.getAddressId())));

        List<Agent> agents = this.agentRepository.findAllByCustomer(customer).stream()
                .filter(a -> Objects.isNull(a.getBranch()))
                .collect(Collectors.toList());
        this.agentRepository.saveAll(agents.stream().map(a -> a.setBranch(branch)).collect(Collectors.toList()));
        return branch;
    }

    @Override
    public Branch updateBranch(String branchId, BranchJson json) {
        Branch branch = this.branchRepository.getById(branchId);
        if (Objects.nonNull(json.getBranchName())) {
            branch.setBranchName(json.getBranchName());
        }
        if (Objects.nonNull(json.getContactId())) {
            branch.setContact(this.contactRepository.getById(json.getContactId()));
        }
        if (Objects.nonNull(json.getAddressId())) {
            branch.setAddress(this.addressRepository.getById(json.getAddressId()));
        }
        return this.branchRepository.save(branch);
    }

    @Override
    public void deleteBranch(String branchId) {
        Branch branchToDelete = this.branchRepository.getById(branchId);
        this.branchRepository.save(branchToDelete.setIsDeleted(true)); // Do a soft delete as they may be delivery orders associated with this branch

        Branch anotherActiveBranch = this.branchRepository.findAllByCustomer(branchToDelete.getCustomer()).stream()
                .filter(b -> !b.getIsDeleted() && !b.getBranchId().equals(branchToDelete.getBranchId()))
                .findFirst().orElse(null);
        List<Agent> agents = this.agentRepository.findAllByBranch(branchToDelete);
        this.agentRepository.saveAll(agents.stream().map(a -> a.setBranch(anotherActiveBranch)).collect(Collectors.toList()));
    }

    @Override
    public Agent createAgent(String customerId, AgentJson json) {
        json.validate();

        Customer customer = this.customerRepository.getById(customerId);

        // We don't have a unique index on agent's phone number so we must validate manually agent uniqueness per customer
        Agent existingAgent = this.agentRepository.findFirstByCustomerAndPhoneNumberAndIsDeleted(customer, json.getPhoneNumber(), false);
        if (existingAgent != null) {
            throw new InvalidParameterException("crud.agent.existingAgent");
        }

        return createAgent(json.getFullName(), json.getPhoneNumber(), customer, json.getBranchId(), false);
    }

    @Override
    public Agent updateAgent(String agentId, AgentJson json) {
        Agent agent = this.agentRepository.getById(agentId);

        if (Objects.nonNull(json.getFullName())) {
            agent.setFullName(json.getFullName());
        }

        if (Objects.nonNull(json.getBranchId())) {
            agent.setBranch(this.branchRepository.getById(json.getBranchId()));
        }

        return this.agentRepository.save(agent);
    }

    @Override
    public void deleteAgent(String agentId) {
        Agent agent = this.agentRepository.getById(agentId);
        if (agent.getIsRoot()) {
            throw new InvalidParameterException("crud.agent.deletion.notAllowed");
        }

        HttpResult loginResult = this.authApiCaller.deleteLogin(Map.of(
                "type", "AGENT",
                "agentId", agent.getAgentId()
        ));
        if (!loginResult.isSuccess()) {
            throw new InvalidParameterException("crud.agent.login.failedDeletion");
        }

        this.agentRepository.save(agent.setIsDeleted(true));
    }


    private Agent createAgent(String fullName, String phoneNumber, Customer customer, String branchId, boolean isRoot) {
        Agent agent = (Objects.nonNull(branchId)
                ? new Agent(this.branchRepository.getById(branchId))
                : new Agent(customer))
                .setFullName(fullName)
                .setPhoneNumber(phoneNumber)
                .setIsRoot(isRoot);

        HttpResult loginResult = this.authApiCaller.createLogin(Map.of(
                "type", "AGENT",
                "agentId", agent.getAgentId(),
                "customerId", customer.getCustomerId(),
                "phoneNumber", phoneNumber
        ));
        if (!loginResult.isSuccess()) {
            throw new InvalidParameterException("crud.agent.login.failedCreation");
        }

        return this.agentRepository.save(agent);
    }
}
