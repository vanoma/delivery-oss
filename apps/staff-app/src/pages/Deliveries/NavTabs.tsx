import React, { FC } from 'react';
import { Box, Tabs, Tab } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import HourglassTopIcon from '@mui/icons-material/HourglassTop';
import AddLocationAltIcon from '@mui/icons-material/AddLocationAlt';
import TwoWheelerIcon from '@mui/icons-material/TwoWheeler';
import CheckIcon from '@mui/icons-material/Check';
import AddIcon from '@mui/icons-material/Add';
import SearchIcon from '@mui/icons-material/Search';
import { DELIVERIES, DELIVERIES_TAB } from '../../routeNames';
import PaymentStatusFilter from './PaymentStatusFilter';

const TAB_ICONS = {
    [DELIVERIES_TAB.REQUEST]: <AddLocationAltIcon />,
    [DELIVERIES_TAB.DRAFT]: <AddIcon />,
    [DELIVERIES_TAB.PENDING]: <HourglassTopIcon />,
    [DELIVERIES_TAB.ACTIVE]: <TwoWheelerIcon />,
    [DELIVERIES_TAB.COMPLETE]: <CheckIcon />,
    [DELIVERIES_TAB.SEARCH]: <SearchIcon />,
};

interface Props {
    tab: string;
}

const NavTabs: FC<Props> = ({ tab }) => {
    const navigate = useNavigate();

    const handleChangeTab = (_: React.SyntheticEvent, value: string): void => {
        navigate(`${DELIVERIES}/${value}`);
    };

    return (
        <Box
            sx={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                mb: 3,
                borderBottom: 1,
                borderColor: 'divider',
            }}
        >
            <Tabs
                value={tab}
                onChange={handleChangeTab}
                variant="scrollable"
                scrollButtons="auto"
                allowScrollButtonsMobile
            >
                {Object.entries(DELIVERIES_TAB).map(([key, value]) => (
                    <Tab
                        label={key}
                        value={value}
                        key={key}
                        icon={TAB_ICONS[value]}
                        iconPosition="start"
                    />
                ))}
            </Tabs>
            <PaymentStatusFilter tab={tab!} />
        </Box>
    );
};

export default NavTabs;
