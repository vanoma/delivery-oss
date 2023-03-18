import React from 'react';
import { Alert, Button } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { BILLING, BILLING_TAB } from '../../routeNames';

const PaymentDueAlert: React.FC<{ gracePeriod: number }> = ({
    gracePeriod,
}) => {
    const { t } = useTranslation();
    const navigate = useNavigate();

    return (
        <Alert
            sx={{ mb: 2 }}
            variant="filled"
            severity={gracePeriod === 0 ? 'error' : 'warning'}
            action={
                <Button
                    size="medium"
                    sx={{ alignSelf: 'center' }}
                    onClick={() =>
                        navigate({
                            pathname: `${BILLING}/${BILLING_TAB.PAY_BALANCE}`,
                        })
                    }
                >
                    {t('billing.paymentDueAlert.pay')}
                </Button>
            }
        >
            {t('billing.paymentDueAlert.paymentRequired')}
        </Alert>
    );
};

export default PaymentDueAlert;
