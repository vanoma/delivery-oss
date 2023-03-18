import { Container, Stack, Tab, Tabs, Typography } from '@mui/material';
import { camelCase } from 'change-case';
import React, { lazy } from 'react';
import { useTranslation } from 'react-i18next';
import { Navigate, useNavigate, useParams } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { ACCOUNT, ACCOUNT_TAB } from '../../routeNames';
import { selectAgent } from '../../redux/slices/authenticationSlice';

const Agents = lazy(() => import('./Agents'));
const Branches = lazy(() => import('./Branches'));

const Account: React.FC = () => {
    const { tab } = useParams<'tab'>();
    const navigate = useNavigate();
    const { t } = useTranslation();
    const agent = useSelector(selectAgent);

    const handleTabChange = (_: React.SyntheticEvent, value: string): void => {
        navigate(`${ACCOUNT}/${value}`);
    };

    if (agent && !agent.isRoot) {
        return <Navigate to="/" replace />;
    }
    if (!Object.values(ACCOUNT_TAB).includes(tab!)) {
        return <Navigate to={`${ACCOUNT}/${ACCOUNT_TAB.AGENTS}`} replace />;
    }

    return (
        <Container>
            <Typography sx={{ mt: 1.5 }} variant="h4">
                {t('account.main.account')}
            </Typography>
            <Tabs
                value={tab}
                onChange={handleTabChange}
                aria-label="basic tabs example"
                sx={{ mb: 3, borderBottom: 1, borderColor: 'divider' }}
            >
                {Object.entries(ACCOUNT_TAB).map(([key, value]) => (
                    <Tab
                        label={t(
                            `account.main.${camelCase(key.toLowerCase())}`
                        )}
                        value={value}
                        key={key}
                        // {...a11yProps(key.toLowerCase())}
                    />
                ))}
            </Tabs>
            <Stack spacing={4}>
                {tab === 'agents' && <Agents />}
                {tab === 'branches' && <Branches />}
            </Stack>
        </Container>
    );
};

export default Account;
