package com.vanoma.api.order.contacts;

import com.vanoma.api.order.maps.Coordinates;
import com.vanoma.api.order.maps.IGeocodingService;
import com.vanoma.api.order.maps.KigaliDistrict;
import com.vanoma.api.order.tests.OrderFactory;
import com.vanoma.api.order.tests.ResourceMapper;
import com.vanoma.api.utils.httpwrapper.HttpResult;
import com.vanoma.api.utils.jsonpatch.PatchUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.vanoma.api.order.tests.ControllerTestUtils.parseResponseBody;
import static com.vanoma.api.order.tests.ControllerTestUtils.stringifyRequestBody;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class ContactControllerTest {

    private static String customerId;

    @Autowired
    private MockMvc mvc;
    @Autowired
    private OrderFactory orderFactory;
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private AddressRepository addressRepository;
    @MockBean
    private IGeocodingService geocodingServiceMock;

    @BeforeEach
    public void setUp() {
        customerId = UUID.randomUUID().toString();
    }

    @Test
    public void testCreateAddress_reversesGeocodeCoordinatesWhenDistrictCannotBeInferredFromStreetName() throws Exception {
        Map<String, String> contactJson = Map.of(
                "phoneNumberOne", "250788225544"
        );

        RequestBuilder requestOne = post("/customers/" + customerId + "/contacts")
                .contentType("application/json")
                .content(new JSONObject(contactJson).toString());
        MockHttpServletResponse resultOne = this.mvc.perform(requestOne).andReturn().getResponse();

        assertThat(resultOne.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        String contactId = (String) parseResponseBody(resultOne).get("contactId");

        when(this.geocodingServiceMock.reverseGeocode(any(Coordinates.class)))
                .thenReturn(new HttpResult(Map.of(
                        "district", KigaliDistrict.NYARUGENGE.name(),
                        "streetName", "KK Street"
                ), HttpStatus.OK.value()));

        Map<String, Object> addressJson = Map.of(
                "addressName", "St Joseph Secondary School",
                "district", "",
                "latitude", -1.9766309,
                "longitude", 30.0513318,
                "placeName", "St Joseph Secondary School",
                "streetName", "Rue des Sports"
        );
        RequestBuilder requestTwo = post("/contacts/" + contactId + "/addresses")
                .contentType("application/json")
                .content(new JSONObject(addressJson).toString());
        MockHttpServletResponse resultTwo = this.mvc.perform(requestTwo).andReturn().getResponse();
        assertThat(resultTwo.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        Map address = parseResponseBody(resultTwo);
        assertThat(address.get("district")).isEqualTo(KigaliDistrict.NYARUGENGE.name());
        assertThat(address.get("streetName")).isEqualTo("Rue des Sports".toUpperCase());
    }

    @Test
    public void testUpdateAddress() throws Exception {
        Address address = this.orderFactory.createAddress(customerId);
        Map<String, Object> requestBody = Map.of(
                "isConfirmed", true,
                "houseNumber", "12",
                "coordinates", Map.of(
                        "type", "Point",
                        "coordinates", List.of(1.90, -30.23)
                )
        );

        RequestBuilder requestBuilder = patch("/addresses/" + address.getAddressId())
                .contentType("application/json")
                .content(stringifyRequestBody(requestBody));
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(200);
        address = this.addressRepository.getById(address.getAddressId());
        Map<String, Object> actualBody = parseResponseBody(result);
        Map<String, Object> expectedBody = ResourceMapper.createAddressMap(address);
        assertThat(actualBody).isEqualTo(expectedBody);

        assertThat(address.getLatitude()).isEqualTo(1.90);
        assertThat(address.getLongitude()).isEqualTo(-30.23);
        assertThat(address.getIsConfirmed()).isTrue();
        assertThat(address.getHouseNumber()).isEqualTo("12");
    }

    @Test
    public void testContactService_create_createsAndSavesContactToDB() throws Exception {
        Map<String, Object> contactJson = Map.of(
                "name", "Joe",
                "phoneNumberOne", "250788112233",
                "phoneNumberTwo", "250788445566"
        );

        RequestBuilder requestBuilder = post("/customers/" + customerId + "/contacts")
                .contentType("application/json")
                .content(new JSONObject(contactJson).toString());

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        Map<String, Object> savedContact = parseResponseBody(result);

        assertThat(savedContact).isNotNull();
        assertThat(contactJson.get("name")).isEqualTo(savedContact.get("name"));
        assertThat(contactJson.get("phoneNumberOne")).isEqualTo(savedContact.get("phoneNumberOne"));
        assertThat(contactJson.get("phoneNumberTwo")).isEqualTo(savedContact.get("phoneNumberTwo"));
    }

    @Test
    public void tesContactController_createContact_returns400ForExistingContact() throws Exception {
        Map<String, Object> contactJson = Map.of(
                "name", "Joe",
                "isSaved", true,
                "phoneNumberOne", "250788112233",
                "phoneNumberTwo", "250788445566"
        );

        RequestBuilder requestBuilder = post("/customers/" + customerId + "/contacts")
                .contentType("application/json")
                .content(new JSONObject(contactJson).toString());

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        Map<String, Object> sameNumber = Map.of(
                "isSaved", true,
                "phoneNumberOne", "250788112233"
        );
        RequestBuilder requestBuilder2 = post("/customers/" + customerId + "/contacts")
                .contentType("application/json")
                .content(new JSONObject(sameNumber).toString());

        MockHttpServletResponse result2 = this.mvc.perform(requestBuilder2).andReturn().getResponse();
        assertThat(result2.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Map<String, Object> response = parseResponseBody(result2);
        String errorMessage = (String) response.get("errorMessage");
        assertThat(errorMessage).contains("existing contact");
    }

    @Test
    public void tesContactController_UpdatesNameOfExistingContact() throws Exception {
        Map<String, Object> contactJson = Map.of(
                "name", "Joe",
                "isSaved", true,
                "phoneNumberOne", "250788112233",
                "phoneNumberTwo", "250788445566"
        );

        RequestBuilder requestBuilder = post("/customers/" + customerId + "/contacts")
                .contentType("application/json")
                .content(new JSONObject(contactJson).toString());

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        Map<String, Object> response = parseResponseBody(result);
        String contactId = (String) response.get("contactId");
        assertThat(contactId).isNotNull();

        Map<String, Object> newName = Map.of(
                "name", "New Name"
        );
        // Update contact
        RequestBuilder requestBuilder2 = patch("/contacts/" + contactId)
                .contentType("application/json-patch+json")
                .content(PatchUtils.getPatchRequestBody(newName).toString());
        MockHttpServletResponse result2 = this.mvc.perform(requestBuilder2).andReturn().getResponse();
        assertThat(result2.getStatus()).isEqualTo(HttpStatus.OK.value());

        Contact updatedContact = this.contactRepository.findById(contactId).orElse(null);
        assertThat(updatedContact).isNotNull();
        assertThat(updatedContact.getName()).isEqualTo("New Name");
    }

    @Test
    public void testContactController_createsAndSavesAddressToDB() throws Exception {
        Contact contact = this.orderFactory.createContact(customerId);

        Map<String, Object> addressJson = Map.of(
                "houseNumber", "32",
                "streetName", "KK 123 ST",
                "addressName", "My Place",
                "latitude", -1.9803,
                "longitude", 30.1233,
                "district", KigaliDistrict.KICUKIRO.name()
        );

        RequestBuilder requestBuilder = post("/contacts/" + contact.getContactId() + "/addresses")
                .contentType("application/json")
                .content(new JSONObject(addressJson).toString());

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        Map<String, Object> response = parseResponseBody(result);

        assertThat(response.get("houseNumber")).isEqualTo(addressJson.get("houseNumber"));
        assertThat(response.get("streetName")).isEqualTo(addressJson.get("streetName"));
        assertThat(response.get("addressName")).isEqualTo(addressJson.get("addressName"));
        assertThat(response.get("latitude")).isEqualTo(addressJson.get("latitude"));
        assertThat(response.get("longitude")).isEqualTo(addressJson.get("longitude"));
        assertThat(response.get("district")).isEqualTo(addressJson.get("district"));
    }

    @Test
    public void testContactController_returns404WhenContactIsNotFound() throws Exception {
        Map<String, Object> addressJson = Map.of(
                "houseNumber", "32",
                "streetName", "KK 123 ST",
                "addressName", "My Place",
                "latitude", -1.9803,
                "longitude", 30.1233,
                "district", KigaliDistrict.KICUKIRO.name()
        );

        RequestBuilder requestBuilder = post("/contacts/" + UUID.randomUUID().toString() + "/addresses")
                .contentType("application/json")
                .content(new JSONObject(addressJson).toString());

        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        Map<String, Object> response = parseResponseBody(result);

        assertThat(response.get("errorMessage")).isEqualTo("Contact not found");
    }

    @Test
    public void testContactController_getCustomerContacts_returnsContacts() throws Exception {
        Contact contactOne = this.orderFactory.createContact(customerId);
        Contact contactTwo = this.orderFactory.createContact(customerId);

        RequestBuilder requestBuilder = get("/customers/" + customerId + "/contacts");
        MockHttpServletResponse result = this.mvc.perform(requestBuilder).andReturn().getResponse();

        assertThat(result.getStatus()).isEqualTo(HttpStatus.OK.value());
        Map<String, Object> response = parseResponseBody(result);
        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("contacts");
        assertThat(results).isNotNull();
        assertThat(results.size()).isEqualTo(2);

        Set<String> contactIds = results.stream().map(c -> (String) c.get("contactId"))
                .collect(Collectors.toSet());
        assertThat(contactIds.contains(contactOne.getContactId())).isTrue();
        assertThat(contactIds.contains(contactTwo.getContactId())).isTrue();
    }
}
