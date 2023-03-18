import React, { ReactElement } from 'react';
import { Typography, Box, Skeleton, Button } from '@mui/material';
import PaymentIcon from '@mui/icons-material/Payment';
import { useTranslation } from 'react-i18next';

const Head = ({
    isLoading,
    handleNewPaymentMethodOpen,
}: {
    isLoading: boolean;
    handleNewPaymentMethodOpen: () => void;
}): ReactElement => {
    const { t } = useTranslation();

    return (
        <Box
            sx={{
                display: 'flex',
                justifyContent: 'space-between',
            }}
        >
            <Typography variant="h5">
                {isLoading ? (
                    <Skeleton width={220} />
                ) : (
                    t('billing.payments.paymentMethods')
                )}
            </Typography>
            {isLoading ? (
                <Skeleton
                    width={74.34}
                    height={30.28}
                    variant="rectangular"
                    animation="wave"
                    sx={{ borderRadius: 19 }}
                />
            ) : (
                <Button
                    variant="contained"
                    size="small"
                    sx={{ maxHeight: 31, mb: 1 }}
                    startIcon={<PaymentIcon />}
                    onClick={handleNewPaymentMethodOpen}
                >
                    {t('billing.paymentMethods.new')}
                </Button>
            )}
        </Box>
    );
};

export default Head;
