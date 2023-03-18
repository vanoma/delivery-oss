import React, { ReactElement } from 'react';
import { Card, Typography, Box, Button, Stack } from '@mui/material';
import { styled } from '@mui/material/styles';
import { useTranslation } from 'react-i18next';
import { localizePhoneNumber } from '@vanoma/helpers';
import { PaymentMethod } from '../../types';
import MTN from '../../../public/assets/mtn.svg';
import Airtel from '../../../public/assets/airtel.svg';
import '../../locales/i18n';

const PaymentMethodCard = styled(Card)(({ theme }) => ({
    padding: theme.spacing(3),
    '&:before': {
        position: 'absolute',
        top: 8,
        right: 8,
        zIndex: -10,
    },
    width: '100%',
    [theme.breakpoints.up('sm')]: {
        minWidth: theme.spacing(40),
        width: theme.spacing(40),
    },
}));

const isMTN = (phoneNumber: string): boolean => {
    const formattedPhoneNumber = localizePhoneNumber(phoneNumber);
    if (
        formattedPhoneNumber.startsWith('078') ||
        formattedPhoneNumber.startsWith('079')
    ) {
        return true;
    }
    return false;
};

const PaymentMethodView = ({
    handleRemovePaymentMethodOpen,
    paymentMethod,
}: {
    // eslint-disable-next-line no-unused-vars
    handleRemovePaymentMethodOpen: (id: string) => void;
    paymentMethod: PaymentMethod;
}): ReactElement => {
    const { t } = useTranslation();

    return (
        <PaymentMethodCard
            sx={{
                '&:before': {
                    content: `url(${
                        isMTN(paymentMethod.phoneNumber) ? MTN : Airtel
                    })`,
                },
            }}
        >
            <Stack
                direction="row"
                justifyContent="space-between"
                alignItems="center"
                mt={1}
            >
                <Typography mb={1}>
                    {`${
                        isMTN(paymentMethod.phoneNumber) ? 'MTN' : 'Airtel'
                    } Mobile Money`}
                    {paymentMethod.isDefault && (
                        <Typography
                            variant="caption"
                            sx={{ fontStyle: 'italic', ml: 2 }}
                            color="primary"
                        >
                            {t('billing.paymentMethods.default')}
                        </Typography>
                    )}
                </Typography>
            </Stack>

            <Typography variant="h6" sx={{ fontWeight: 300 }}>
                {localizePhoneNumber(paymentMethod.phoneNumber)}
            </Typography>

            <Box
                sx={{
                    display: 'flex',
                    justifyContent: paymentMethod.shortCode
                        ? 'space-between'
                        : 'flex-end',
                    alignItems: 'center',
                    mt: 2,
                }}
            >
                {paymentMethod.shortCode && (
                    <Typography color="text.secondary">
                        {`Momo code: ${paymentMethod.shortCode}`}
                    </Typography>
                )}
                <Button
                    variant="outlined"
                    size="small"
                    onClick={() =>
                        handleRemovePaymentMethodOpen(
                            paymentMethod.paymentMethodId
                        )
                    }
                >
                    {t('billing.paymentMethods.remove')}
                </Button>
            </Box>
        </PaymentMethodCard>
    );
};

export default PaymentMethodView;
