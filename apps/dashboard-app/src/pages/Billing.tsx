import React, { ReactElement } from 'react';
import { Container, Typography, Stack, Tabs, Tab } from '@mui/material';
import { useNavigate, useParams, Navigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { camelCase } from 'change-case';
import '../locales/i18n';
import PaymentMethods from '../features/PaymentMethods';
import PayBalance from '../features/PayBalance';
import { BILLING, BILLING_TAB } from '../routeNames';

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
function a11yProps(tab: string) {
    return {
        id: `simple-tab-${tab}`,
        'aria-controls': `simple-tabpanel-${tab}`,
    };
}

const Billing = (): ReactElement => {
    const { tab } = useParams<'tab'>();
    const navigate = useNavigate();
    const { t } = useTranslation();

    const handleTabChange = (_: React.SyntheticEvent, value: string): void => {
        navigate(`${BILLING}/${value}`);
    };

    if (!Object.values(BILLING_TAB).includes(tab!)) {
        return (
            <Navigate to={`${BILLING}/${BILLING_TAB.PAY_BALANCE}`} replace />
        );
    }

    return (
        <Container>
            <Typography sx={{ mt: 1.5 }} variant="h4">
                {t('billing.payments.billing')}
            </Typography>
            <Tabs
                value={tab}
                onChange={handleTabChange}
                aria-label="basic tabs example"
                sx={{ mb: 3, borderBottom: 1, borderColor: 'divider' }}
            >
                {Object.entries(BILLING_TAB).map(([key, value]) => (
                    <Tab
                        label={t(
                            `billing.payments.${camelCase(key.toLowerCase())}`
                        )}
                        value={value}
                        key={key}
                        {...a11yProps(key.toLowerCase())}
                    />
                ))}
            </Tabs>
            <Stack spacing={4}>
                {tab === 'pay-balance' && <PayBalance />}
                {tab === 'payment-methods' && <PaymentMethods />}
            </Stack>
        </Container>
    );
};

export default Billing;
