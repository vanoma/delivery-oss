package com.vanoma.api.order.charges;

import com.vanoma.api.utils.exceptions.ExpectedServerError;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ChargeUtilsTest {
    @Test
    public void testComputeTransactionFee_returns25WhenTransactionAmountIs975() {
        BigDecimal deliveryFee = new BigDecimal("975");

        BigDecimal actualTransactionFee = ChargeUtils.computeTransactionFeeGivenTransactionAmount(deliveryFee);

        assertThat(actualTransactionFee.compareTo(new BigDecimal("25.0")) == 0).isTrue();
    }

    @Test
    public void testComputeTransactionFee_returns13WhenTransactionAmountIs487() {
        BigDecimal deliveryFee = new BigDecimal("487");

        BigDecimal actualTransactionFee = ChargeUtils.computeTransactionFeeGivenTransactionAmount(deliveryFee);

        assertThat(actualTransactionFee.compareTo(new BigDecimal("12.49")) == 0).isTrue();
    }

    @Test
    public void testComputeTransactionFee_throwsErrorWhenTransactionAmountIsNull() {
        Exception exception = assertThrows(ExpectedServerError.class, () -> {
            ChargeUtils.computeTransactionFeeGivenTransactionAmount(null);
        });

        assertThat(exception.getMessage()).isEqualTo("crud.payment.transactionAmount.required");
    }

    @Test
    public void testComputeTransactionFee_returnsZeroWhenTransactionAmountIsZero() {
        BigDecimal transactionAmount = new BigDecimal("0");

        BigDecimal actualTransactionFee = ChargeUtils.computeTransactionFeeGivenTransactionAmount(transactionAmount);

        assertThat(actualTransactionFee.compareTo(new BigDecimal("0")) == 0).isTrue();
    }

    @Test
    public void testComputeTransactionAmountGivenTotalAmount_returns25WhenTransactionAmountIs975() {
        BigDecimal totalAmount = new BigDecimal("1000");

        BigDecimal transactionAmount = ChargeUtils.computeTransactionAmountGivenTotalAmount(totalAmount);

        assertThat(transactionAmount.compareTo(new BigDecimal("975.0")) == 0).isTrue();
    }
}
