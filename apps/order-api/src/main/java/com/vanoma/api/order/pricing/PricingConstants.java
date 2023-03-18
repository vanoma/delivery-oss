package com.vanoma.api.order.pricing;

import java.math.BigDecimal;

public class PricingConstants {

    public static int MIN_PRICE = 715;
    public static int PRICE_PER_KILOMETER = 138;
    public static int BASELINE_DELIVERY_COST = 550;

    public static BigDecimal SMALL_PACKAGE_PRICE = BigDecimal.valueOf(1170);

    public static BigDecimal MEDIUM_PACKAGE_PRICE = BigDecimal.valueOf(1755);

    public static BigDecimal LARGE_PACKAGE_PRICE = BigDecimal.valueOf(4680);

}
