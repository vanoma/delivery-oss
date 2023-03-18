package com.vanoma.api.order.charges;

import com.vanoma.api.order.orders.DeliveryOrder;
import com.vanoma.api.order.orders.Discount;
import com.vanoma.api.order.payment.PaymentStatus;
import com.vanoma.api.order.payment.TransactionBreakdown;
import com.vanoma.api.utils.exceptions.ExpectedServerError;
import com.vanoma.api.utils.exceptions.InvalidParameterException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// TODO: Avoid converting BigDecimal to double by all means. This is trouble in the making. Use strings instead.
public class ChargeUtils {
    public static BigDecimal TRANSACTION_FEE_PERCENTAGE = BigDecimal.valueOf(0.025);

    public static int roundBigDecimal(BigDecimal value) {
        return value.setScale(0, RoundingMode.UP).intValue();
    }

    public static BigDecimal computeTransactionFeeGivenTransactionAmount(BigDecimal transactionAmount) {
        if (transactionAmount == null) {
            throw new ExpectedServerError("crud.payment.transactionAmount.required");
        }

        if (transactionAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // Formula: TransactionFee = (deliveryFee * transactionFeePercentage) / (1 - transactionFeePercentage)
        double numerator = transactionAmount.multiply(TRANSACTION_FEE_PERCENTAGE).doubleValue();
        double denominator = BigDecimal.valueOf(1L).subtract(TRANSACTION_FEE_PERCENTAGE).doubleValue();
        double transactionFee = numerator / denominator;

        return BigDecimal.valueOf(round(transactionFee, 2));
    }

    public static BigDecimal computeTransactionAmountGivenTotalAmount(BigDecimal totalAmount) {
        if (totalAmount == null) {
            throw new ExpectedServerError("crud.payment.transactionAmount.required");
        }

        if (totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        // Formula: transactionAmount = (1/transactionFeePercentage - 1) * totalAmount * transactionFeePercentage
        double transactionAmount = (1 / TRANSACTION_FEE_PERCENTAGE.doubleValue() - 1) * totalAmount.doubleValue() * TRANSACTION_FEE_PERCENTAGE.doubleValue();
        return BigDecimal.valueOf(transactionAmount);
    }

    public static BigDecimal computeTotalAmountGivenTransactionAmount(BigDecimal transactionAmount) {
        if (transactionAmount == null) {
            throw new ExpectedServerError("crud.payment.transactionAmount.required");
        }

        if (transactionAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal transactionFee = computeTransactionFeeGivenTransactionAmount(transactionAmount);
        return transactionAmount.add(transactionFee);

    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.UP);
        return bd.doubleValue();
    }

    public static BigDecimal getTransactionAmount(Set<Charge> charges, Set<Discount> discounts) {
        BigDecimal chargeAmount = charges.stream()
                .map(Charge::getTransactionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountAmount = discounts.stream()
                .map(Discount::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return chargeAmount.subtract(discountAmount);
    }

    public static Set<Charge> getUnpaidCharges(List<DeliveryOrder> deliveryOrders) {
        return deliveryOrders
                .stream()
                .map(DeliveryOrder::getUnpaidCharges)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    public static PaymentStatus getPaymentStatus(Set<Charge> charges) {
        if (charges.size() == 0) return PaymentStatus.NO_CHARGE;

        Set<Charge> unpaidCharges = charges
                .stream()
                .filter(charge -> charge.getStatus() == ChargeStatus.UNPAID)
                .collect(Collectors.toSet());

        if (unpaidCharges.size() == 0) {
            return PaymentStatus.PAID;
        } else if (unpaidCharges.size() == charges.size()) {
            return PaymentStatus.UNPAID;
        }
        return PaymentStatus.PARTIAL;
    }

    public static TransactionBreakdown getTransactionBreakdown(Set<Charge> charges, Set<Discount> discounts) {
        return getTransactionBreakdown(charges, discounts, null);
    }

    public static TransactionBreakdown getTransactionBreakdown(Set<Charge> charges, Set<Discount> discounts, BigDecimal expectedTotalAmount) {
        BigDecimal transactionAmount = getTransactionAmount(charges, discounts);
        BigDecimal transactionFee = computeTransactionFeeGivenTransactionAmount(transactionAmount);
        BigDecimal totalAmount = transactionAmount.add(transactionFee);

        if (expectedTotalAmount != null && totalAmount.compareTo(expectedTotalAmount) != 0) {
            throw new InvalidParameterException("crud.paymentAttempt.totalAmount.incorrect");
        }

        return new TransactionBreakdown()
                .setTransactionAmount(transactionAmount)
                .setTransactionFee(transactionFee)
                .setTotalAmount(transactionAmount.add(transactionFee));
    }
}
