import { Typography, FormHelperText, Alert } from '@mui/material';
import React, { useState } from 'react';
import { CustomModal } from '@vanoma/ui-components';
import { useTranslation } from 'react-i18next';
import { Package } from '@vanoma/types';
import { useNavigate } from 'react-router-dom';
import { useInvokeDeliveryOrderPaymentMutation } from '../../../../api';
import { InvokeDeliveryOrderPaymentResponse } from '../../../../types';
import PaymentForm from '../../../../components/PaymentForm';
import { DELIVERIES, DELIVERIES_TAB } from '../../../../routeNames';

const Payment: React.FC<{
    open: boolean;
    delivery: Package;
    handleClose: () => void;
}> = ({ open, delivery, handleClose }) => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [paymentRequestId, setPaymentRequestId] = useState<string | null>(
        null
    );
    const [isPaid, setIsPaid] = useState(false);

    const [invokePayment, { isLoading, error }] =
        useInvokeDeliveryOrderPaymentMutation();

    const handlePayment = (paymentMethodId: string): void => {
        invokePayment({
            deliveryOrderId: delivery.deliveryOrder.deliveryOrderId,
            paymentMethodId,
        })
            .unwrap()
            .then((payload) => {
                // eslint-disable-next-line no-shadow
                const { paymentRequestId } =
                    payload as InvokeDeliveryOrderPaymentResponse;
                setPaymentRequestId(paymentRequestId);
            });
    };

    return (
        <CustomModal open={open} handleClose={handleClose}>
            <Typography variant="h5" color="primary">
                {t('deliveries.request.payment')}
            </Typography>
            <PaymentForm
                submitPayment={({ paymentMethodId }) =>
                    handlePayment(paymentMethodId)
                }
                retryPayment={({ paymentMethodId }) => {
                    setPaymentRequestId(null);
                    handlePayment(paymentMethodId);
                }}
                cancelPaymentChecking={() => setPaymentRequestId(null)}
                paymentSuccessful={() => {
                    setIsPaid(true);
                    setPaymentRequestId(null);
                    setTimeout(() => {
                        setIsPaid(false);
                        const deliveryOrderUrl = `${DELIVERIES}/${DELIVERIES_TAB.ACTIVE}#oid=${delivery.deliveryOrder.deliveryOrderId}`;
                        navigate(deliveryOrderUrl);
                    }, 2000);
                }}
                disabled={isLoading}
                isLoading={isLoading}
                paymentRequestId={paymentRequestId}
            />
            {error && (
                <FormHelperText sx={{ mt: 2 }} error>
                    {error}
                </FormHelperText>
            )}
            {isPaid && (
                <Alert severity="success">
                    {t('alertAndValidationMessages.paymentReceivedSuccessful')}
                </Alert>
            )}
        </CustomModal>
    );
};

export default Payment;
