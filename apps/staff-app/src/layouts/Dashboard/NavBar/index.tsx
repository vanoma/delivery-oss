import React from 'react';
import { AppBar, IconButton, Toolbar, Box, Stack } from '@mui/material';
import { styled } from '@mui/material/styles';
import { alpha } from '@mui/system';
import { Hidden } from '@vanoma/ui-components';
import MenuIcon from '../../../components/MenuIcon';
import AccountPopover from './AccountPopover';
import DriversMapButton from './DriversMapButton';
import DriverStopsButton from './DriverStopsButton';
import NotificationsPopover from '../../../features/NotificationsPopover';

const DRAWER_WIDTH = 280;
const APPBAR_MOBILE = 64;
const APPBAR_DESKTOP = 92;

const RootStyle = styled(AppBar)(({ theme }) => ({
    boxShadow: 'none',
    backdropFilter: 'blur(6px)',
    WebkitBackdropFilter: 'blur(6px)', // Fix on Mobile
    backgroundColor: alpha(theme.palette.background.default, 0.72),
    borderBottom: `1px solid ${alpha(theme.palette.primary.main, 0.15)}`,
    [theme.breakpoints.up('lg')]: {
        width: `calc(100% - ${DRAWER_WIDTH + 1}px)`,
    },
}));

const ToolbarStyle = styled(Toolbar)(({ theme }) => ({
    minHeight: APPBAR_MOBILE,
    [theme.breakpoints.up('lg')]: {
        minHeight: APPBAR_DESKTOP,
        padding: theme.spacing(0, 5),
    },
}));

export default function DashboardNavbar({
    onOpenSidebar,
}: {
    onOpenSidebar: () => void;
}): JSX.Element {
    return (
        <RootStyle>
            <ToolbarStyle>
                <Hidden width="lgUp">
                    <IconButton
                        onClick={onOpenSidebar}
                        sx={{ mr: 1, color: 'text.primary' }}
                    >
                        <MenuIcon />
                    </IconButton>
                </Hidden>
                <Box sx={{ flexGrow: 1 }} />
                <Stack
                    direction="row"
                    alignItems="center"
                    spacing={{ xs: 1, sm: 2 }}
                >
                    <NotificationsPopover />
                    <DriverStopsButton />
                    <DriversMapButton />
                    <AccountPopover />
                </Stack>
            </ToolbarStyle>
        </RootStyle>
    );
}
