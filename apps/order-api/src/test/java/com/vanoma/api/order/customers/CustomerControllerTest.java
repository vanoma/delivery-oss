package com.vanoma.api.order.customers;

import com.vanoma.api.order.contacts.Address;
import com.vanoma.api.order.contacts.Contact;
import com.vanoma.api.order.contacts.ContactRepository;
import com.vanoma.api.order.tests.ObjectFactory;
import com.vanoma.api.order.tests.OrderFactory;
import com.vanoma.api.order.tests.ResourceMapper;
import com.vanoma.api.utils.NullableValueMapBuilder;
import com.vanoma.api.utils.httpwrapper.HttpResult;
import com.vanoma.api.utils.httpwrapper.IHttpClientWrapper;
import com.vanoma.api.utils.input.TimeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.vanoma.api.order.tests.ControllerTestUtils.parseResponseBody;
import static com.vanoma.api.order.tests.ControllerTestUtils.stringifyRequestBody;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class CustomerControllerTest {

    @Autowired
    private OrderFactory orderFactory;

    @Autowired
    private MockMvc mvc;
    @MockBean
    private IHttpClientWrapper httpClientWrapper;

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private BranchRepository branchRepository;
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private ContactRepository contactRepository;

    @BeforeEach
    public void setUp() {
        reset(this.httpClientWrapper);
        when(this.httpClientWrapper.post(any(String.class), any(Map.class)))
                .thenReturn(new HttpResult(new HashMap<>(), HttpStatus.OK.value()));
    }

    @Test
    public void testGetCustomers_filtersByCustomerId() throws Exception  {
        List<Customer> customers = List.of(
                this.orderFactory.createCustomer(),
                this.orderFactory.createCustomer(),
                this.orderFactory.createCustomer()
        );

        RequestBuilder requestBuilder = get(String.format("/customers?customerId=%s,%s,%s&sort=createdAt", customers.get(0).getCustomerId(), customers.get(1).getCustomerId(), UUID.randomUUID()));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(HttpStatus.OK.value());
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = new NullableValueMapBuilder<String, Object>()
                .put("count", 2)
                .put("next", null)
                .put("previous", null)
                .put("results", List.of(ResourceMapper.createCustomerMap(customers.get(0)), ResourceMapper.createCustomerMap(customers.get(1))))
                .build();
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testGetCustomers_filtersByBusinessName() throws Exception  {
        List<Customer> customers = List.of(
                this.orderFactory.createCustomer("Hello", ObjectFactory.getRandomPhoneNumber()),
                this.orderFactory.createCustomer("World", ObjectFactory.getRandomPhoneNumber())
        );

        RequestBuilder requestBuilder = get("/customers?businessName=ello");
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(HttpStatus.OK.value());
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = new NullableValueMapBuilder<String, Object>()
                .put("count", 1)
                .put("next", null)
                .put("previous", null)
                .put("results", List.of(ResourceMapper.createCustomerMap(customers.get(0))))
                .build();
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testGetCustomers_filtersByPhoneNumber() throws Exception  {
        List<Customer> customers = List.of(
                this.orderFactory.createCustomer(ObjectFactory.getRandomString(), "250788123123"),
                this.orderFactory.createCustomer(ObjectFactory.getRandomString(), ObjectFactory.getRandomPhoneNumber())
        );

        RequestBuilder requestBuilder = get("/customers?phoneNumber=250788123123");
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(HttpStatus.OK.value());
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = new NullableValueMapBuilder<String, Object>()
                .put("count", 1)
                .put("next", null)
                .put("previous", null)
                .put("results", List.of(ResourceMapper.createCustomerMap(customers.get(0))))
                .build();
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testCreateCustomer() throws Exception {
        String phoneNumber = ObjectFactory.getRandomPhoneNumber();
        String otpId = UUID.randomUUID().toString();
        String otpCode = "12345";
        Map<String, Object> requestBody = Map.of(
                "businessName", ObjectFactory.getRandomString(),
                "phoneNumber", phoneNumber,
                "otpId", otpId,
                "otpCode", otpCode
        );

        RequestBuilder requestBuilder = post("/customers")
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        // Validate created customer
        Customer customer = this.customerRepository.getFirstByPhoneNumber(phoneNumber);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createCustomerMap(customer);
        assertThat(actualBody).isEqualTo(expectedBody);

        // Validate created contact
        Contact contact = this.contactRepository.findFirstByCustomerIdAndPhoneNumberOne(customer.getCustomerId(), phoneNumber);
        assertThat(contact.getName()).isEqualTo(customer.getBusinessName());
        assertThat(contact.getIsDefault()).isTrue();
        assertThat(contact.getIsSaved()).isTrue();

        // Validate created agent
        List<Agent> agents = this.agentRepository.findAllByCustomer(customer);
        assertThat(agents.size()).isEqualTo(1);
        assertThat(agents.get(0).getBranch()).isNull();
        assertThat(agents.get(0).getIsRoot()).isTrue();
        assertThat(agents.get(0).getIsDeleted()).isFalse();
        assertThat(agents.get(0).getPhoneNumber()).isEqualTo(customer.getPhoneNumber());
        assertThat(agents.get(0).getFullName()).isEqualTo(customer.getBusinessName());

        // Validate communication-api request to verify otp
        ArgumentCaptor<Map<String, Object>> otpPayloadCaptor = ArgumentCaptor.forClass(Map.class);
        String otpFullUrl = String.format("null/otp/%s/verification", otpId);
        verify(this.httpClientWrapper, times(1)).post(eq(otpFullUrl), otpPayloadCaptor.capture());
        Map<String, Object> actualOtpPayload = otpPayloadCaptor.getValue();
        Map<String, Object> expectedOtpPayload = Map.of(
                "otpCode", otpCode,
                "phoneNumber", phoneNumber
        );
        assertThat(actualOtpPayload).isEqualTo(expectedOtpPayload);

        // Validate auth-api request to create login
        ArgumentCaptor<Map<String, Object>> loginPayloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(this.httpClientWrapper, times(1)).post(eq("null/login-creation"), loginPayloadCaptor.capture());
        Map<String, Object> actualLoginPayload = loginPayloadCaptor.getValue();
        Map<String, Object> expectedLoginPayload = Map.of(
                "type", "AGENT",
                "agentId", agents.get(0).getAgentId(),
                "customerId", customer.getCustomerId(),
                "phoneNumber", phoneNumber
        );
        assertThat(actualLoginPayload).isEqualTo(expectedLoginPayload);

        // Validate payment-api request to create payment method
        ArgumentCaptor<Map<String, Object>> paymentMethodPayloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(this.httpClientWrapper, times(1)).post(eq(String.format("null/users/%s/payment-methods", customer.getCustomerId())), paymentMethodPayloadCaptor.capture());
        Map<String, Object> actualPaymentMethodPayload = paymentMethodPayloadCaptor.getValue();
        Map<String, Object> expectedPaymentMethodPayload = Map.of(
                "isDefault", true,
                "type", "MOBILE_MONEY",
                "extra", Map.of(
                        "phoneNumber", phoneNumber
                )
        );
        assertThat(actualPaymentMethodPayload).isEqualTo(expectedPaymentMethodPayload);
    }

    @Test
    public void testCreateCustomer_returnsErrorIfPhoneNumberInUse() throws Exception {
        Customer existingCustomer = this.orderFactory.createCustomer();
        Map<String, Object> requestBody = Map.of(
                "businessName", ObjectFactory.getRandomString(),
                "phoneNumber", existingCustomer.getPhoneNumber(),
                "otpId", UUID.randomUUID().toString(),
                "otpCode", "12345"
        );

        RequestBuilder requestBuilder = post("/customers")
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        // Validate response
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "errorCode", "INVALID_REQUEST",
                "errorMessage", "There is an existing account with same phone number"
        );
        assertThat(actualBody).isEqualTo(expectedBody);

        // Should not attempt to call auth-api
        verify(this.httpClientWrapper, never()).post(eq("null/login-creation"), anyMap());
    }

    @Test
    public void testGetCustomer() throws Exception  {
        Customer customer = this.orderFactory.createCustomer();

        RequestBuilder requestBuilder = get("/customers/" + customer.getCustomerId());
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(HttpStatus.OK.value());
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createCustomerMap(customer);
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testUpdateCustomer() throws Exception {
        OffsetDateTime expiry = TimeUtils.getUtcNow().plusDays(1);
        Customer customer = this.orderFactory.createCustomer();
        Map<String, Object> requestBody = Map.of(
                "weightingFactor", 1.25,
                "billingInterval", 7,
                "billingGracePeriod", 10,
                "postpaidExpiry", expiry.toString(),
                "fixedPriceAmount", 1200.00,
                "fixedPriceExpiry", expiry.toString()
        );

        RequestBuilder requestBuilder = patch("/customers/" + customer.getCustomerId())
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.OK.value());

        customer = this.customerRepository.getById(customer.getCustomerId());
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createCustomerMap(customer);
        assertThat(actualBody).isEqualTo(expectedBody);
        assertThat(customer.getWeightingFactor()).isEqualTo(new BigDecimal("1.25"));
        assertThat(customer.getBillingInterval()).isEqualTo(7);
        assertThat(customer.getBillingGracePeriod()).isEqualTo(10);
        assertThat(customer.getPostpaidExpiry()).isEqualTo(expiry);
        assertThat(customer.getFixedPriceAmount()).isEqualTo(new BigDecimal("1200.00"));
        assertThat(customer.getFixedPriceExpiry()).isEqualTo(expiry);
        assertThat(customer.getIsPrepaid()).isFalse();
        assertThat(customer.getHasFixedPrice()).isTrue();
    }

    @Test
    public void testGetBranches() throws Exception  {
        Customer customer = this.orderFactory.createCustomer();
        List<Branch> branches = List.of(
                this.orderFactory.createBranch(customer),
                this.orderFactory.createBranch(customer),
                this.orderFactory.createBranch(customer, true)
        );

        RequestBuilder requestBuilder = get(String.format("/customers/%s/branches", customer.getCustomerId()));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(HttpStatus.OK.value());
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = new NullableValueMapBuilder<String, Object>()
                .put("count", 2)
                .put("next", null)
                .put("previous", null)
                .put("results", List.of(
                        ResourceMapper.createBranchMap(branches.get(0)),
                        ResourceMapper.createBranchMap(branches.get(1))
                ))
                .build();
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testCreateBranch() throws Exception  {
        Customer customer = this.orderFactory.createCustomer();
        Agent agent = this.orderFactory.createAgent(customer);
        Contact contact = this.orderFactory.createContact(customer.getCustomerId());
        Address address = this.orderFactory.createAddress(customer.getCustomerId());
        Map<String, Object> requestBody = Map.of(
                "branchName", ObjectFactory.getRandomString(),
                "contactId", contact.getContactId(),
                "addressId", address.getAddressId()
        );

        RequestBuilder requestBuilder = post(String.format("/customers/%s/branches", customer.getCustomerId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        // Validate created branch
        Branch branch = this.branchRepository.findAllByCustomer(customer).get(0);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createBranchMap(branch);
        assertThat(actualBody).isEqualTo(expectedBody);

        // Validate agent update
        agent = this.agentRepository.getById(agent.getAgentId());
        assertThat(agent.getBranch().getBranchId()).isEqualTo(branch.getBranchId());
    }

    @Test
    public void testGetBranch() throws Exception  {
        Branch branch = this.orderFactory.createBranch();

        RequestBuilder requestBuilder = get("/branches/" + branch.getBranchId());
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(HttpStatus.OK.value());
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createBranchMap(branch);
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testUpdateBranch() throws Exception  {
        Branch branch = this.orderFactory.createBranch();
        Map<String, Object> requestBody = Map.of(
                "branchName", "New Name"
        );

        RequestBuilder requestBuilder = patch("/branches/" + branch.getBranchId())
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.OK.value());

        branch = this.branchRepository.getById(branch.getBranchId());
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createBranchMap(branch);
        assertThat(actualBody).isEqualTo(expectedBody);
        assertThat(branch.getBranchName()).isEqualTo("New Name");
    }

    @Test
    public void testDeleteBranch_withoutAnotherActiveBranch() throws Exception  {
        Branch branch = this.orderFactory.createBranch();
        Agent agent = this.orderFactory.createAgent(branch);

        RequestBuilder requestBuilder = delete("/branches/" + branch.getBranchId());
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate branch deletion
        assertThat(result.getStatus()).isEqualTo(204);
        assertThat(this.branchRepository.getById(branch.getBranchId()).getIsDeleted()).isTrue();

        // Validate agent update
        agent = this.agentRepository.getById(agent.getAgentId());
        assertThat(agent.getBranch()).isNull();
    }

    @Test
    public void testDeleteBranch_withAnotherActiveBranch() throws Exception  {
        Customer customer = this.orderFactory.createCustomer();
        Branch branch1 = this.orderFactory.createBranch(customer);
        Branch branch2 = this.orderFactory.createBranch(customer);
        Agent agent = this.orderFactory.createAgent(branch1);

        RequestBuilder requestBuilder = delete("/branches/" + branch1.getBranchId());
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate branch deletion
        assertThat(result.getStatus()).isEqualTo(204);
        assertThat(this.branchRepository.getById(branch1.getBranchId()).getIsDeleted()).isTrue();

        // Validate agent update
        agent = this.agentRepository.getById(agent.getAgentId());
        assertThat(agent.getBranch().getBranchId()).isEqualTo(branch2.getBranchId());
    }

    @Test
    public void testGetAgents() throws Exception  {
        Customer customer = this.orderFactory.createCustomer();
        List<Agent> agents = List.of(
                this.orderFactory.createAgent(customer),
                this.orderFactory.createAgent(customer),
                this.orderFactory.createAgent(customer, true)
        );

        RequestBuilder requestBuilder = get(String.format("/customers/%s/agents", customer.getCustomerId()));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(HttpStatus.OK.value());
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = new NullableValueMapBuilder<String, Object>()
                .put("count", 2)
                .put("next", null)
                .put("previous", null)
                .put("results", List.of(
                        ResourceMapper.createAgentMap(agents.get(0)),
                        ResourceMapper.createAgentMap(agents.get(1))
                ))
                .build();
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testCreateAgent_withoutBranch() throws Exception  {
        Customer customer = this.orderFactory.createCustomer();
        String otpId = UUID.randomUUID().toString();
        String otpCode = "12345";
        Map<String, Object> requestBody = Map.of(
                "fullName", ObjectFactory.getRandomString(),
                "phoneNumber", ObjectFactory.getRandomPhoneNumber(),
                "otpId", otpId,
                "otpCode", otpCode
        );

        RequestBuilder requestBuilder = post(String.format("/customers/%s/agents", customer.getCustomerId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        // Validate created agent
        Agent agent = this.agentRepository.findAllByCustomer(customer).get(0);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createAgentMap(agent);
        assertThat(actualBody).isEqualTo(expectedBody);

        assertThat(agent.getBranch()).isNull();
        assertThat(agent.getIsRoot()).isFalse();
        assertThat(agent.getIsDeleted()).isFalse();
        assertThat(agent.getPhoneNumber()).isEqualTo(agent.getPhoneNumber());
        assertThat(agent.getFullName()).isEqualTo(agent.getFullName());

        // Validate auth-api request to create login
        ArgumentCaptor<Map<String, Object>> loginPayloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(this.httpClientWrapper, times(1)).post(eq("null/login-creation"), loginPayloadCaptor.capture());
        Map<String, Object> actualLoginPayload = loginPayloadCaptor.getValue();
        Map<String, Object> expectedLoginPayload = Map.of(
                "type", "AGENT",
                "agentId", agent.getAgentId(),
                "customerId", customer.getCustomerId(),
                "phoneNumber", agent.getPhoneNumber()
        );
        assertThat(actualLoginPayload).isEqualTo(expectedLoginPayload);
    }

    @Test
    public void testCreateAgent_withBranch() throws Exception  {
        Customer customer = this.orderFactory.createCustomer();
        Branch branch = this.orderFactory.createBranch(customer);
        String otpId = UUID.randomUUID().toString();
        String otpCode = "12345";
        Map<String, Object> requestBody = Map.of(
                "fullName", ObjectFactory.getRandomString(),
                "phoneNumber", ObjectFactory.getRandomPhoneNumber(),
                "branchId", branch.getBranchId(),
                "otpId", otpId,
                "otpCode", otpCode
        );

        RequestBuilder requestBuilder = post(String.format("/customers/%s/agents", customer.getCustomerId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        // Validate created agent
        Agent agent = this.agentRepository.findAllByCustomer(customer).get(0);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createAgentMap(agent);
        assertThat(actualBody).isEqualTo(expectedBody);

        assertThat(agent.getBranch().getBranchId()).isEqualTo(branch.getBranchId());
        assertThat(agent.getIsRoot()).isFalse();
        assertThat(agent.getIsDeleted()).isFalse();
        assertThat(agent.getPhoneNumber()).isEqualTo(agent.getPhoneNumber());
        assertThat(agent.getFullName()).isEqualTo(agent.getFullName());

        // Validate auth-api request to create login
        ArgumentCaptor<Map<String, Object>> loginPayloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(this.httpClientWrapper, times(1)).post(eq("null/login-creation"), loginPayloadCaptor.capture());
        Map<String, Object> actualLoginPayload = loginPayloadCaptor.getValue();
        Map<String, Object> expectedLoginPayload = Map.of(
                "type", "AGENT",
                "agentId", agent.getAgentId(),
                "customerId", customer.getCustomerId(),
                "phoneNumber", agent.getPhoneNumber()
        );
        assertThat(actualLoginPayload).isEqualTo(expectedLoginPayload);
    }

    @Test
    public void testCreateAgent_reusesPhoneNumberForDeletedAgents() throws Exception  {
        Customer customer = this.orderFactory.createCustomer();
        Agent existingAgent = this.orderFactory.createAgent(customer, true);
        String otpId = UUID.randomUUID().toString();
        String otpCode = "12345";
        Map<String, Object> requestBody = Map.of(
                "fullName", ObjectFactory.getRandomString(),
                "phoneNumber", existingAgent.getPhoneNumber(),
                "otpId", otpId,
                "otpCode", otpCode
        );

        RequestBuilder requestBuilder = post(String.format("/customers/%s/agents", customer.getCustomerId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        // Validate created agent
        Agent agent = this.agentRepository.findFirstByCustomerAndPhoneNumberAndIsDeleted(customer, existingAgent.getPhoneNumber(), false);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createAgentMap(agent);
        assertThat(actualBody).isEqualTo(expectedBody);

        assertThat(agent.getBranch()).isNull();
        assertThat(agent.getIsRoot()).isFalse();
        assertThat(agent.getIsDeleted()).isFalse();
        assertThat(agent.getPhoneNumber()).isEqualTo(agent.getPhoneNumber());
        assertThat(agent.getFullName()).isEqualTo(agent.getFullName());

        // Validate auth-api request to create login
        ArgumentCaptor<Map<String, Object>> loginPayloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(this.httpClientWrapper, times(1)).post(eq("null/login-creation"), loginPayloadCaptor.capture());
        Map<String, Object> actualLoginPayload = loginPayloadCaptor.getValue();
        Map<String, Object> expectedLoginPayload = Map.of(
                "type", "AGENT",
                "agentId", agent.getAgentId(),
                "customerId", customer.getCustomerId(),
                "phoneNumber", agent.getPhoneNumber()
        );
        assertThat(actualLoginPayload).isEqualTo(expectedLoginPayload);
    }

    @Test
    public void testCreateAgent_returnsErrorIfPhoneNumberInUse() throws Exception  {
        Customer customer = this.orderFactory.createCustomer();
        Agent agent = this.orderFactory.createAgent(customer);
        String otpId = UUID.randomUUID().toString();
        String otpCode = "12345";
        Map<String, Object> requestBody = Map.of(
                "fullName", ObjectFactory.getRandomString(),
                "phoneNumber", agent.getPhoneNumber(),
                "otpId", otpId,
                "otpCode", otpCode
        );

        RequestBuilder requestBuilder = post(String.format("/customers/%s/agents", customer.getCustomerId()))
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        // Validate response
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "errorCode", "INVALID_REQUEST",
                "errorMessage", "There is an existing agent with same phone number"
        );
        assertThat(actualBody).isEqualTo(expectedBody);

        // Should not attempt to call auth-api
        verify(this.httpClientWrapper, never()).post(eq("null/login-creation"), anyMap());
    }

    @Test
    public void testGetAgent() throws Exception  {
        Agent agent = this.orderFactory.createAgent();

        RequestBuilder requestBuilder = get("/agents/" + agent.getAgentId());
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(HttpStatus.OK.value());
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createAgentMap(agent);
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void testUpdateAgent() throws Exception  {
        Customer customer = this.orderFactory.createCustomer();
        Branch branch = this.orderFactory.createBranch(customer);
        Agent agent = this.orderFactory.createAgent(customer);
        Map<String, Object> requestBody = Map.of(
                "fullName", "New Name",
                "branchId", branch.getBranchId()
        );

        RequestBuilder requestBuilder = patch("/agents/" + agent.getAgentId())
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.OK.value());

        agent = this.agentRepository.getById(agent.getAgentId());
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createAgentMap(agent);
        assertThat(actualBody).isEqualTo(expectedBody);
        assertThat(agent.getFullName()).isEqualTo("New Name");
        assertThat(agent.getBranch().getBranchId()).isEqualTo(branch.getBranchId());
    }

    @Test
    public void testDeleteAgent_nonRootAgent() throws Exception  {
        Agent agent = this.orderFactory.createAgent();

        RequestBuilder requestBuilder = delete("/agents/" + agent.getAgentId());
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        // Validate deletion
        assertThat(result.getStatus()).isEqualTo(204);
        assertThat(this.agentRepository.getById(agent.getAgentId()).getIsDeleted()).isTrue();

        // Validate auth-api request to delete login
        ArgumentCaptor<Map<String, Object>> loginPayloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(this.httpClientWrapper, times(1)).post(eq("null/login-deletion"), loginPayloadCaptor.capture());
        Map<String, Object> actualLoginPayload = loginPayloadCaptor.getValue();
        Map<String, Object> expectedLoginPayload = Map.of(
                "type", "AGENT",
                "agentId", agent.getAgentId()
        );
        assertThat(actualLoginPayload).isEqualTo(expectedLoginPayload);
    }

    @Test
    public void testDeleteAgent_rootAgent() throws Exception  {
        Agent agent = this.orderFactory.createAgent();
        this.agentRepository.save(agent.setIsRoot(true));

        RequestBuilder requestBuilder = delete("/agents/" + agent.getAgentId());
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(400);
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = Map.of(
                "errorCode", "INVALID_REQUEST",
                "errorMessage", "You can not delete this agent."
        );
        assertThat(actualBody).isEqualTo(expectedBody);
    }
}
