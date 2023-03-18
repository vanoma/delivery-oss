package com.vanoma.api.order;

import com.vanoma.api.order.charges.ChargeControllerTest;
import com.vanoma.api.order.charges.ChargeUtilsTest;
import com.vanoma.api.order.contacts.AddressTest;
import com.vanoma.api.order.contacts.ContactAddressServiceTest;
import com.vanoma.api.order.contacts.ContactControllerTest;
import com.vanoma.api.order.contacts.ContactTest;
import com.vanoma.api.order.customers.CustomerControllerTest;
import com.vanoma.api.order.events.PackageEventControllerTest;
import com.vanoma.api.order.invoices.InvoiceControllerTest;
import com.vanoma.api.order.orders.BusinessHourServiceTest;
import com.vanoma.api.order.orders.DeliveryOrderControllerTest;
import com.vanoma.api.order.orders.OrderPlacementWorkflowTest;
import com.vanoma.api.order.packages.BackgroundTaskTest;
import com.vanoma.api.order.packages.PackageControllerTest;
import com.vanoma.api.order.payment.PaymentControllerTest;
import com.vanoma.api.order.pricing.PricingControllerTest;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
@SelectClasses({
        ContactTest.class,
        AddressTest.class,
        BusinessHourServiceTest.class,
        ContactAddressServiceTest.class,
        PricingControllerTest.class,
        DeliveryOrderControllerTest.class,
        PackageControllerTest.class,
        PaymentControllerTest.class,
        ContactControllerTest.class,
        InvoiceControllerTest.class,
        ChargeUtilsTest.class,
        ChargeControllerTest.class,
        PackageEventControllerTest.class,
        OrderPlacementWorkflowTest.class,
        CustomerControllerTest.class,
        BackgroundTaskTest.class
})
public class ApplicationTestSuite {
}