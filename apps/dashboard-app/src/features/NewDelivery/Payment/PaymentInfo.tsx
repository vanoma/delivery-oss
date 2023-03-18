/* eslint-disable no-nested-ternary */
import React from 'react';
import { Button } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import { LoadingIndicator } from '@vanoma/ui-components';
import { DeliveryOrderPricing } from '@vanoma/types';
import { useTypedDispatch } from '../../../helpers/reduxToolkit';
import {
    selectIsValid,
    selectPaymentRequestId,
    validatePackages,
    payDeliveryOrder,
    updatePaymentRequestId,
} from '../slice';
import PaymentForm from '../../../components/PaymentForm';

interface Props {
    pricing: DeliveryOrderPricing;
    isLoading: boolean;
    postDeliveryPlacement: () => void;
    // eslint-disable-next-line no-unused-vars
    setShowOrderPlacement: (value: boolean) => void;
}

const Payment: React.FC<Props> = ({
    pricing,
    isLoading,
    postDeliveryPlacement,
    setShowOrderPlacement,
}) => {
    const { t } = useTranslation();
    const dispatch = useTypedDispatch();

    const isValid = useSelector(selectIsValid);
    const paymentRequestId = useSelector(selectPaymentRequestId);

    return (
        <>
            {pricing.isPrepaid ? (
                <PaymentForm
                    submitPayment={({ paymentMethodId }) =>
                        dispatch(validatePackages())
                            .unwrap()
                            .then(() => {
                                dispatch(payDeliveryOrder(paymentMethodId));
                            })
                    }
                    retryPayment={({ paymentMethodId }) =>
                        dispatch(payDeliveryOrder(paymentMethodId))
                    }
                    cancelPaymentChecking={() =>
                        dispatch(updatePaymentRequestId(null))
                    }
                    paymentSuccessful={() => {
                        dispatch(updatePaymentRequestId(null));
                        postDeliveryPlacement();
                    }}
                    disabled={isLoading || !isValid || !pricing}
                    isLoading={isLoading}
                    paymentRequestId={paymentRequestId}
                />
            ) : (
                <Button
                    sx={{
                        height: 40,
                        mt: 2,
                    }}
                    fullWidth
                    disabled={isLoading || !isValid || !pricing}
                    onClick={() =>
                        dispatch(validatePackages())
                            .unwrap()
                            .then(() => setShowOrderPlacement(true))
                    }
                >
                    {isLoading ? (
                        <LoadingIndicator />
                    ) : (
                        t('delivery.payment.order')
                    )}
                </Button>
            )}
        </>
    );
};

export default Payment;
