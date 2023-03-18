import React, { useEffect } from 'react';
import { styled } from '@mui/material/styles';
import { useLocation } from 'react-router-dom';
import { Avatar, Box, Card, Divider, Drawer, Typography } from '@mui/material';
import { useSelector } from 'react-redux';
import { Hidden } from '@vanoma/ui-components';
import NavSection from './NavSection';
import sidebarConfig from './SidebarConfig';
import {
    selectAgent,
    selectBusinessName,
} from '../../../redux/slices/authenticationSlice';
import '../../../locales/i18n';
import { ACCOUNT } from '../../../routeNames';

const DRAWER_WIDTH = 280;

const RootStyle = styled('div')(({ theme }) => ({
    [theme.breakpoints.up('lg')]: {
        flexShrink: 0,
        width: DRAWER_WIDTH,
    },
}));

const AccountStyle = styled(Card)(({ theme }) => ({
    display: 'flex',
    alignItems: 'center',
    padding: theme.spacing(2, 2.5),
}));

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
export default function DashboardSidebar({
    isOpenSidebar,
    onCloseSidebar,
}: {
    isOpenSidebar: boolean;
    onCloseSidebar: () => void;
}) {
    const { pathname } = useLocation();
    const businessName = useSelector(selectBusinessName);
    const agent = useSelector(selectAgent);

    useEffect(() => {
        if (isOpenSidebar) {
            onCloseSidebar();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [pathname]);

    const renderContent = (
        <>
            <Box sx={{ m: 1 }}>
                <AccountStyle>
                    <Avatar>{businessName?.charAt(0)}</Avatar>
                    <Box sx={{ ml: 2, flexGrow: 1 }}>
                        <Typography
                            variant="subtitle2"
                            sx={{ color: 'text.primary' }}
                        >
                            {businessName}
                        </Typography>
                        {agent && agent.branch && (
                            <Typography
                                variant="body2"
                                sx={{ color: 'text.secondary' }}
                            >
                                {agent.branch.branchName}
                            </Typography>
                        )}
                    </Box>
                </AccountStyle>
            </Box>
            <Divider />

            <NavSection
                navConfig={sidebarConfig().filter(
                    ({ path }) =>
                        !(path.includes(ACCOUNT) && agent && !agent.isRoot)
                )}
            />
        </>
    );

    return (
        <RootStyle>
            <Hidden width="lgUp">
                <Drawer
                    open={isOpenSidebar}
                    onClose={onCloseSidebar}
                    PaperProps={{
                        sx: { width: DRAWER_WIDTH },
                    }}
                >
                    {renderContent}
                </Drawer>
            </Hidden>

            <Hidden width="lgDown">
                <Drawer
                    open
                    variant="persistent"
                    PaperProps={{
                        sx: {
                            width: DRAWER_WIDTH,
                            backgroundColor: 'background.default',
                        },
                    }}
                >
                    {renderContent}
                </Drawer>
            </Hidden>
        </RootStyle>
    );
}
