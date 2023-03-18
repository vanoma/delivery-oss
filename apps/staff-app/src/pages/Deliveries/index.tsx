import React, { FC, lazy, useEffect, useRef, useState } from 'react';
import { Box, Container, IconButton, Stack, Typography } from '@mui/material';
import { useParams, useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';
import RefreshIcon from '@mui/icons-material/Refresh';
import { storage } from '@vanoma/helpers';
import { DELIVERIES, DELIVERIES_TAB } from '../../routeNames';
import { useTypedDispatch } from '../../redux/typedHooks';
import {
    changeCurrentPage,
    changeCurrentTab,
    selectCurrentPage,
    selectCurrentTab,
    selectTotalCount,
} from '../../redux/slices/deliveriesSlice';
import NavTabs from './NavTabs';
import AutoRefresh from './AutoRefresh';

const SearchDeliveries = lazy(() => import('../../features/SearchDeliveries'));
const ActiveDeliveries = lazy(() => import('../../features/ActiveDeliveries'));
const DraftDeliveries = lazy(() => import('../../features/DraftDeliveries'));
const PendingDeliveries = lazy(
    () => import('../../features/PendingDeliveries')
);
const RequestDeliveries = lazy(
    () => import('../../features/RequestDeliveries')
);
const CompleteDeliveries = lazy(
    () => import('../../features/CompleteDeliveries')
);

const Deliveries: FC = () => {
    const savedInterval = storage.getItem('refreshInterval') ?? '0';
    const [expandAll, setExpandAll] = useState(false);
    const [refreshInterval, setRefreshInterval] = useState(
        Number.parseInt(savedInterval, 10)
    );

    const { tab } = useParams<'tab'>();
    const navigate = useNavigate();
    const dispatch = useTypedDispatch();
    const currentTab = useSelector(selectCurrentTab);
    const totalCount = useSelector(selectTotalCount);
    const currentPage = useSelector(selectCurrentPage);

    // eslint-disable-next-line no-undef
    const timeoutId = useRef<NodeJS.Timeout>();

    const clearTimeoutId = (): void => {
        const timer = timeoutId.current;
        if (timer) {
            clearTimeout(timer);
        }
    };

    const refreshDeliveries = (): void => {
        clearTimeoutId();
        dispatch(changeCurrentPage(currentPage));

        if (refreshInterval > 0) {
            timeoutId.current = setTimeout(
                refreshDeliveries,
                refreshInterval * 60 * 1000
            );
        }
    };

    useEffect(() => {
        if (Object.values(DELIVERIES_TAB).includes(tab!)) {
            dispatch(changeCurrentTab(tab!));
        } else {
            navigate(`${DELIVERIES}/${DELIVERIES_TAB.ACTIVE}`);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [tab]);

    useEffect(() => {
        refreshDeliveries();
        return clearTimeoutId;
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [refreshInterval]);

    return (
        <Container>
            <Stack
                sx={{ mt: 1.5, mb: 2, height: 40 }}
                direction="row"
                justifyContent="space-between"
            >
                <Typography variant="h4">Deliveries</Typography>
                <Box display="flex" gap={2}>
                    <AutoRefresh
                        refreshInterval={refreshInterval}
                        setRefreshInterval={(interval) => {
                            setRefreshInterval(interval);
                            storage.setItem(
                                'refreshInterval',
                                interval.toString()
                            );
                        }}
                    />
                    <IconButton onClick={refreshDeliveries}>
                        <RefreshIcon />
                    </IconButton>
                    <IconButton
                        disabled={totalCount === 0}
                        onClick={() => setExpandAll(!expandAll)}
                    >
                        {expandAll ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                    </IconButton>
                </Box>
            </Stack>
            <NavTabs tab={tab!} />
            {tab === DELIVERIES_TAB.REQUEST && tab === currentTab && (
                <RequestDeliveries expandAll={expandAll} />
            )}
            {tab === DELIVERIES_TAB.DRAFT && tab === currentTab && (
                <DraftDeliveries expandAll={expandAll} />
            )}
            {tab === DELIVERIES_TAB.PENDING && tab === currentTab && (
                <PendingDeliveries expandAll={expandAll} />
            )}
            {tab === DELIVERIES_TAB.ACTIVE && tab === currentTab && (
                <ActiveDeliveries expandAll={expandAll} />
            )}
            {tab === DELIVERIES_TAB.COMPLETE && tab === currentTab && (
                <CompleteDeliveries expandAll={expandAll} />
            )}
            {tab === DELIVERIES_TAB.SEARCH && tab === currentTab && (
                <SearchDeliveries expandAll={expandAll} />
            )}
        </Container>
    );
};

export default Deliveries;
