import React, { ReactElement, useEffect } from 'react';
import {
    Button,
    IconButton,
    LinearProgress,
    Stack,
    Typography,
} from '@mui/material';
import ReplayIcon from '@mui/icons-material/Replay';
import { useTranslation } from 'react-i18next';
import { PaymentStatus } from '@vanoma/types';
import { useGetPaymentStatusQuery } from '../api';
import CustomSnackBar from './CustomSnackBar';

const WaitingForPayment = ({
    paymentRequestId,
    paymentSuccessful,
    handleRetryPayment,
    cancelPaymentChecking,
}: {
    paymentRequestId: string;
    paymentSuccessful: () => void;
    handleRetryPayment: () => void;
    cancelPaymentChecking: () => void;
}): ReactElement => {
    const { t } = useTranslation();
    const { data, error, refetch } = useGetPaymentStatusQuery(
        paymentRequestId,
        { pollingInterval: 5000 }
    );

    useEffect(() => {
        if (data && data.paymentStatus === PaymentStatus.PAID) {
            paymentSuccessful();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [data]);

    return (
        <>
            <Stack
                direction="row"
                sx={{
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    mt: 2,
                }}
            >
                <Typography>
                    {t('delivery.payment.waitingForYourPayment')}
                </Typography>
                <Stack direction="row" spacing={2}>
                    <IconButton onClick={handleRetryPayment}>
                        <ReplayIcon />
                    </IconButton>
                    <Button
                        size="small"
                        variant="outlined"
                        onClick={cancelPaymentChecking}
                    >
                        {t('delivery.payment.cancel')}
                    </Button>
                </Stack>
            </Stack>
            <LinearProgress sx={{ width: 190 }} />
            <CustomSnackBar
                message={error as string}
                severity="error"
                onRetry={refetch}
            />
        </>
    );
};

export default WaitingForPayment;
