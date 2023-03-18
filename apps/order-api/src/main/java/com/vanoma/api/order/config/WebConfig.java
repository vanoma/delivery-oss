package com.vanoma.api.order.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr353.JSR353Module;
import com.vanoma.api.order.contacts.Address;
import com.vanoma.api.order.contacts.Contact;
import com.vanoma.api.order.external.*;
import com.vanoma.api.order.maps.*;
import com.vanoma.api.order.orders.DeliveryOrder;
import com.vanoma.api.order.packages.Package;
import com.vanoma.api.order.utils.JsonPatchMapper;
import com.vanoma.api.order.utils.LanguageUtils;
import com.vanoma.api.utils.httpwrapper.HttpClientWrapper;
import com.vanoma.api.utils.httpwrapper.IHttpClientWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.text.DateFormat;
import java.util.Locale;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

@Configuration
@EnableSpringDataWebSupport
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public LanguageUtils languageUtils() {
        return new LanguageUtils();
    }

    @Bean
    public IHttpClientWrapper httpClientWrapper() {
        return HttpClientWrapper.getInstance();
    }

    @Bean
    public IPaymentAPICaller paymentAPICaller() {
        return PaymentAPICaller.builder()
                .httpClient(this.httpClientWrapper())
                .orderApiUrl(System.getenv("VANOMA_ORDER_API_URL"))
                .paymentApiUrl(System.getenv("VANOMA_PAYMENT_API_URL"))
                .build();
    }

    @Bean
    public IDeliveryApiCaller deliveryApiCaller() {
        return DeliveryApiCaller.builder()
                .httpClient(httpClientWrapper())
                .deliveryApiUrl(System.getenv("VANOMA_DELIVERY_API_URL"))
                .build();
    }

    @Bean
    public IGeocodingService geocodingService() {
        String googleMapsApiKey = System.getenv("API_GOOGLE_MAPS_API_KEY");
        return new GeocodingService(GoogleMapsAPIWrapper.getInstance(googleMapsApiKey));
    }

    @Bean
    public ICommunicationApiCaller communicationApiCaller() {
        return CommunicationApiCaller.builder()
                .communicationApiUrl(System.getenv("VANOMA_COMMUNICATION_API_URL"))
                .httpClient(httpClientWrapper())
                .build();
    }

    @Bean
    public IAuthApiCaller authApiCaller() {
        return AuthApiCaller.builder()
                .httpClient(httpClientWrapper())
                .authURL(System.getenv("VANOMA_AUTH_API_URL"))
                .build();
    }

    @Bean
    public INavigationDistanceApi navigationDistanceApi() {
        return new GoogleDistanceMatrixApiWrapper(System.getenv("API_GOOGLE_MAPS_API_KEY"));
    }

    // JsonPatch Mappers
    @Bean
    public JsonPatchMapper<Package> packageJsonPatchMapper() {
        return new JsonPatchMapper<>();
    }

    @Bean
    public JsonPatchMapper<Contact> contactJsonPatchMapper() {
        return new JsonPatchMapper<>();
    }

    @Bean
    public JsonPatchMapper<Address> addressJsonPatchMapper() {
        return new JsonPatchMapper<>();
    }

    @Bean
    public JsonPatchMapper<DeliveryOrder> deliveryOrderJsonPatchMapper() {
        return new JsonPatchMapper<>();
    }

    // Other configurations
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver slr = new AcceptHeaderLocaleResolver();
        slr.setDefaultLocale(Locale.US);
        return slr;
    }

    @Bean
    public ObjectMapper objectMapper() {
        DateFormat dateFormatter = new StdDateFormat();
        return new ObjectMapper()
                .disable(WRITE_DATES_AS_TIMESTAMPS)
                .setDateFormat(dateFormatter)
                .registerModule(new JavaTimeModule())
                .registerModule(new JSR353Module())
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
