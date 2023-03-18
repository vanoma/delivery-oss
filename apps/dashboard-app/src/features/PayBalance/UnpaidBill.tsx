import { Stack, Typography } from '@mui/material';
import { Box } from '@mui/system';
import React, { ReactElement, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import { Bill } from '@vanoma/types';
import UnpaidBillSkeleton from './UnpaidBillSkeleton';
import PaymentForm from '../../components/PaymentForm';
import { useInvokeBulkPaymentMutation } from '../../api';
import { selectCustomerId } from '../../redux/slices/authenticationSlice';
import CustomSnackBar from '../../components/CustomSnackBar';

const UnpaidBill = ({
    isLoading,
    bill,
    disablePayButton,
    rangeDate,
    refetch,
    branchId,
}: {
    isLoading: boolean;
    bill: Bill | undefined;
    disablePayButton: boolean;
    rangeDate: Date | undefined;
    refetch: () => void;
    branchId?: string;
}): ReactElement => {
    const { t } = useTranslation();

    const [paymentRequestId, setPaymentRequestId] = useState<string | null>(
        null
    );

    const [invokeBulkPayment, { isLoading: isBulkPaymentLoading, error }] =
        useInvokeBulkPaymentMutation();

    const customerId = useSelector(selectCustomerId);

    return (
        <Box
            sx={{
                border: (theme) => `1px solid ${theme.palette.primary.light}`,
                borderRadius: 0.5,
                p: 2,
                flexGrow: 1,
                maxWidth: { sm: 440 },
            }}
        >
            {isLoading && !bill ? (
                <UnpaidBillSkeleton />
            ) : (
                <>
                    <Stack spacing={1} direction="row">
                        <Typography color="text.secondary">
                            {`${t('billing.payBalance.deliveries')}:`}
                        </Typography>
                        <Typography>{bill?.totalCount}</Typography>
                    </Stack>
                    <Stack spacing={1} direction="row">
                        <Typography color="text.secondary">
                            {`${t('billing.payBalance.totalAmount')}:`}
                        </Typography>
                        <Typography>{bill?.totalAmount!}</Typography>
                    </Stack>
                    <Box
                        display="flex"
                        justifyContent="space-between"
                        alignItems="center"
                        mt={2}
                    >
                        <Typography variant="h6">
                            {t('billing.payBalance.paymentMethod')}
                        </Typography>
                    </Box>
                    <PaymentForm
                        submitPayment={({ paymentMethodId }) =>
                            invokeBulkPayment({
                                customerId: customerId!,
                                paymentMethodId,
                                totalAmount: bill?.totalAmount.toString()!,
                                endAt: rangeDate?.toISOString(),
                                branchId,
                            })
                                .unwrap()
                                // eslint-disable-next-line no-shadow
                                .then(({ paymentRequestId }) => {
                                    setPaymentRequestId(paymentRequestId);
                                })
                        }
                        retryPayment={({ paymentMethodId }) => {
                            setPaymentRequestId(null);
                            invokeBulkPayment({
                                customerId: customerId!,
                                paymentMethodId,
                                totalAmount: bill?.totalAmount.toString()!,
                                endAt: rangeDate
                                    ? rangeDate?.toISOString()
                                    : undefined,
                            })
                                .unwrap()
                                // eslint-disable-next-line no-shadow
                                .then(({ paymentRequestId }) => {
                                    setPaymentRequestId(paymentRequestId);
                                });
                        }}
                        cancelPaymentChecking={() => setPaymentRequestId(null)}
                        paymentSuccessful={() => {
                            setPaymentRequestId(null);
                            // When paying some of deliveries, we need to show updated list after paying
                            refetch();
                        }}
                        disabled={isBulkPaymentLoading || disablePayButton}
                        isLoading={isBulkPaymentLoading}
                        paymentRequestId={paymentRequestId}
                    />
                </>
            )}
            <CustomSnackBar message={error as string} severity="error" />
        </Box>
    );
};

export default UnpaidBill;
