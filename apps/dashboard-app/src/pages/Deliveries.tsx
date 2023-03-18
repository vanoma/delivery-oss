import React, { FC, lazy, useState } from 'react';
import { Container, Typography, Tabs, Tab, Box } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useParams, useNavigate, Navigate } from 'react-router-dom';
import AddLocationAltOutlinedIcon from '@mui/icons-material/AddLocationAltOutlined';
import TwoWheelerIcon from '@mui/icons-material/TwoWheeler';
import CheckIcon from '@mui/icons-material/Check';
import AddIcon from '@mui/icons-material/Add';
import '../locales/i18n';
import { useSelector } from 'react-redux';
import { DELIVERIES, DELIVERIES_TAB } from '../routeNames';
import BranchSelector from '../components/BranchSelector';
import { selectAgentBranch } from '../redux/slices/authenticationSlice';

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
function a11yProps(tab: string) {
    return {
        id: `simple-tab-${tab}`,
        'aria-controls': `simple-tabpanel-${tab}`,
    };
}

function parseSelectedBranchId(selectedBranch: string): string | undefined {
    return selectedBranch !== 'all' && selectedBranch !== ''
        ? selectedBranch
        : undefined;
}

const TAB_ICONS = {
    [DELIVERIES_TAB.NEW]: <AddIcon />,
    [DELIVERIES_TAB.REQUEST]: <AddLocationAltOutlinedIcon />,
    [DELIVERIES_TAB.ACTIVE]: <TwoWheelerIcon />,
    [DELIVERIES_TAB.COMPLETE]: <CheckIcon />,
};

const NewDelivery = lazy(() => import('../features/NewDelivery'));
const RequestDeliveries = lazy(() => import('../features/RequestDeliveries'));
const ActiveDeliveries = lazy(() => import('../features/ActiveDeliveries'));
const CompleteDeliveries = lazy(() => import('../features/CompleteDeliveries'));

const Deliveries: FC = () => {
    const { tab } = useParams<'tab'>();
    const navigate = useNavigate();
    const { t } = useTranslation();
    const agentBranch = useSelector(selectAgentBranch);

    const [selectedBranchId, setSelectedBranchId] = useState(
        agentBranch?.branchId ?? ''
    );

    const handleTabChange = (_: React.SyntheticEvent, value: string): void => {
        navigate(`${DELIVERIES}/${value}`);
    };

    if (!Object.values(DELIVERIES_TAB).includes(tab!)) {
        return <Navigate to={`${DELIVERIES}/${DELIVERIES_TAB.NEW}`} replace />;
    }

    const parsedBranchId = parseSelectedBranchId(selectedBranchId);

    return (
        <Container>
            <Box
                display="flex"
                justifyContent="space-between"
                alignItems="center"
                mt={1.5}
            >
                <Typography variant="h4">
                    {t('deliveries.orders.deliveries')}
                </Typography>
                {tab !== DELIVERIES_TAB.NEW && (
                    <BranchSelector
                        value={selectedBranchId}
                        onChange={(value) => setSelectedBranchId(value)}
                        allBranches
                    />
                )}
            </Box>
            <Tabs
                value={tab}
                onChange={handleTabChange}
                variant="scrollable"
                scrollButtons="auto"
                allowScrollButtonsMobile
                sx={{ mb: 3, borderBottom: 1, borderColor: 'divider' }}
            >
                {Object.entries(DELIVERIES_TAB).map(([key, value]) => (
                    <Tab
                        label={t(`deliveries.orders.${key.toLowerCase()}`)}
                        value={value}
                        key={key}
                        {...a11yProps(key.toLowerCase())}
                        icon={TAB_ICONS[value]}
                        iconPosition="start"
                    />
                ))}
            </Tabs>
            {tab === DELIVERIES_TAB.NEW && <NewDelivery />}
            {tab === DELIVERIES_TAB.REQUEST && (
                <RequestDeliveries selectedBranchId={parsedBranchId} />
            )}
            {tab === DELIVERIES_TAB.ACTIVE && (
                <ActiveDeliveries selectedBranchId={parsedBranchId} />
            )}
            {tab === DELIVERIES_TAB.COMPLETE && (
                <CompleteDeliveries selectedBranchId={parsedBranchId} />
            )}
        </Container>
    );
};

export default Deliveries;
