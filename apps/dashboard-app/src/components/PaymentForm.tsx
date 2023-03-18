import React, { ReactElement, useState } from 'react';
import { Stack, Button } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import { LoadingIndicator } from '@vanoma/ui-components';
import WaitingForPayment from './WaitingForPayment';
import SelectPaymentMethod from './SelectPaymentMethod';
import { PaymentMethod } from '../types';
import { selectPaymentMethod } from '../redux/slices/authenticationSlice';

const PaymentForm = ({
    submitPayment,
    retryPayment,
    isLoading,
    disabled,
    paymentRequestId,
    paymentSuccessful,
    cancelPaymentChecking,
}: {
    // eslint-disable-next-line no-unused-vars
    submitPayment: (value: PaymentMethod) => void;
    // eslint-disable-next-line no-unused-vars
    retryPayment: (value: PaymentMethod) => void;
    paymentSuccessful: () => void;
    isLoading: boolean;
    disabled: boolean;
    paymentRequestId: string | null;
    cancelPaymentChecking: () => void;
}): ReactElement => {
    const { t } = useTranslation();
    const [selectedPaymentMethod, setSelectedPaymentMethod] =
        useState<PaymentMethod | null>(useSelector(selectPaymentMethod));

    return (
        <>
            {paymentRequestId !== null && paymentRequestId !== undefined ? (
                <WaitingForPayment
                    paymentRequestId={paymentRequestId}
                    paymentSuccessful={paymentSuccessful}
                    handleRetryPayment={() =>
                        retryPayment(selectedPaymentMethod!)
                    }
                    cancelPaymentChecking={cancelPaymentChecking}
                />
            ) : (
                <>
                    <Stack spacing={2} my={2}>
                        <SelectPaymentMethod
                            selectedPaymentMethod={selectedPaymentMethod}
                            onSelect={setSelectedPaymentMethod}
                        />
                        <Button
                            type="submit"
                            sx={{
                                height: 40,
                            }}
                            fullWidth
                            disabled={disabled || !selectedPaymentMethod}
                            onClick={() =>
                                submitPayment(selectedPaymentMethod!)
                            }
                        >
                            {isLoading ? (
                                <LoadingIndicator />
                            ) : (
                                t('delivery.payment.pay')
                            )}
                        </Button>
                    </Stack>
                </>
            )}
        </>
    );
};

export default PaymentForm;
