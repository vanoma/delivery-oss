package com.vanoma.api.order.pricing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vanoma.api.order.charges.*;
import com.vanoma.api.order.contacts.Address;
import com.vanoma.api.order.customers.Customer;
import com.vanoma.api.order.maps.Coordinates;
import com.vanoma.api.order.maps.IGeocodingService;
import com.vanoma.api.order.maps.INavigationDistanceApi;
import com.vanoma.api.order.orders.*;
import com.vanoma.api.order.packages.Package;
import com.vanoma.api.order.packages.PackageRepository;
import com.vanoma.api.order.packages.PackageSize;
import com.vanoma.api.order.packages.PackageUtils;
import com.vanoma.api.order.payment.IPaymentService;
import com.vanoma.api.utils.exceptions.InvalidParameterException;
import com.vanoma.api.utils.input.CoordinatesJson;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class PricingService implements IPricingService {

    @Autowired
    private CustomPricingRepository customPricingRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PackageRepository packageRepository;
    @Autowired
    private IChargeService chargeService;
    @Autowired
    private ChargeRepository chargeRepository;
    @Autowired
    private DiscountRepository discountRepository;
    @Autowired
    private IPaymentService paymentService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private IGeocodingService geocodingService;
    @Autowired
    private INavigationDistanceApi distanceApi;

    @Override
    public CustomPricing createCustomPricing(String customerId, CustomPricingJson customPricingJson) {
        customPricingJson.validate();
        CustomPricing customPricing = new CustomPricing(customerId)
                .setCustomerName(customPricingJson.getCustomerName())
                .setPrice(customPricingJson.getPrice())
                .setExpireAt(customPricingJson.getExpireAt());
        return this.customPricingRepository.save(customPricing);
    }

    @Override
    public Map<String, Object> createDeliveryFees(String deliveryOrderId) {
        return createDeliveryFees(this.orderRepository.getById(deliveryOrderId));
    }

    @Override
    public Map<String, Object> createDeliveryFees(DeliveryOrder order) {
        List<Package> packages = this.packageRepository.findByDeliveryOrder(order);
        PackageUtils.validateDeliveryOrderPackages(packages);

        BigDecimal weightingFactor = order.getCustomer().getWeightingFactor();
        BigDecimal customDeliveryPrice = this.getCustomDeliveryPrice(order.getCustomer());

        BigDecimal transactionAmount = packages.stream()
                .map(pkg -> createDeliveryFee(pkg, customDeliveryPrice, weightingFactor))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal transactionFee = ChargeUtils.computeTransactionFeeGivenTransactionAmount(transactionAmount);
        BigDecimal totalAmount = transactionAmount.add(transactionFee);

        return Map.of(
                "transactionFee", ChargeUtils.roundBigDecimal(transactionFee),
                "transactionAmount", ChargeUtils.roundBigDecimal(transactionAmount),
                "totalAmount", ChargeUtils.roundBigDecimal(totalAmount),
                "isPrepaid", order.getCustomer().getIsPrepaid()
        );
    }

    @Override
    public Map<String, Object> getDeliveryPricing(PricingJson pricingJson) {
        this.validateLocationsAreValid(pricingJson);

        BigDecimal transactionAmount = BigDecimal.ZERO;
        for (PricingItemJson item : pricingJson.getPackages()) {
            if (!item.hasSize()) throw new InvalidParameterException("crud.pricingJson.size.required");

            BigDecimal deliveryPrice = getPriceForPackage(item.getSize());
            transactionAmount = transactionAmount.add(deliveryPrice);
        }

        BigDecimal transactionFee = ChargeUtils.computeTransactionFeeGivenTransactionAmount(transactionAmount);
        return Map.of(
                "transactionFee", ChargeUtils.roundBigDecimal(transactionFee),
                "transactionAmount", ChargeUtils.roundBigDecimal(transactionAmount),
                "totalAmount", ChargeUtils.roundBigDecimal(transactionAmount.add(transactionFee))
        );
    }

    private BigDecimal getPriceForPackage(PackageSize size) {
        if (size == PackageSize.SMALL) return PricingConstants.SMALL_PACKAGE_PRICE;
        if (size == PackageSize.MEDIUM) return PricingConstants.MEDIUM_PACKAGE_PRICE;
        return PricingConstants.LARGE_PACKAGE_PRICE;
    }

    private BigDecimal createDeliveryFee(Package pkg, BigDecimal customDeliveryPrice, BigDecimal weightingFactor) {
        BigDecimal rawTransactionAmount = getPriceForPackage(pkg.getSize());
        BigDecimal actualTransactionAmount = rawTransactionAmount.multiply(weightingFactor);

        BigDecimal transactionAmount = customDeliveryPrice == null ? actualTransactionAmount : customDeliveryPrice;

        Charge charge = this.chargeRepository.findFirstByPkgAndType(pkg, ChargeType.DELIVERY_FEE);
        if (charge == null) {
            charge = new Charge(pkg)
                    .setType(ChargeType.DELIVERY_FEE)
                    .setStatus(ChargeStatus.UNPAID)
                    .setDescription("Delivery fee");
        }

        charge
                .setTransactionAmount(transactionAmount)
                .setActualTransactionAmount(actualTransactionAmount);

        this.chargeRepository.save(charge);
        return transactionAmount;
    }

    private void validateLocationsAreValid(PricingJson pricingJson) {
        // throws InvalidDataParameter if coordinates are outside covered areas
        for (PricingItemJson item : pricingJson.getPackages()) {
            Coordinates origin = getCoordinates(item.getOrigin());
            this.geocodingService.reverseGeocode(origin);

            Coordinates destination = getCoordinates(item.getDestination());
            this.geocodingService.reverseGeocode(destination);
        }
    }

    private BigDecimal getCustomDeliveryPrice(Customer customer) {
        if (!customer.getHasFixedPrice()) {
            return null;
        }

        return ChargeUtils.computeTransactionAmountGivenTotalAmount(customer.getFixedPriceAmount());
    }

    private BigDecimal computeDeliveryPrice(Coordinates origin, Coordinates destination) {
        long navigationDistanceMeters = this.distanceApi.getNavigationDistance(origin, destination);
        double metersInKilometer = 1000.0;
        double price = PricingConstants.BASELINE_DELIVERY_COST +
                PricingConstants.PRICE_PER_KILOMETER * (navigationDistanceMeters / metersInKilometer);
        if (price < PricingConstants.MIN_PRICE) {
            price = PricingConstants.MIN_PRICE;
        }
        return new BigDecimal(price);
    }
    
    private Coordinates getCoordinates(CoordinatesJson json) {
        return new Coordinates()
                .setLat(json.getLatitude())
                .setLng(json.getLongitude());
    }
}
