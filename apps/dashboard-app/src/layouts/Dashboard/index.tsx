import React, { useState } from 'react';
import { styled } from '@mui/material/styles';
import { Outlet } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { Box } from '@mui/material';
import { Hidden } from '@vanoma/ui-components';
import DashboardNavbar from './DashboardNavbar';
import DashboardSidebar from './DashboardSidebar';
import AccountInfo from './AccountInfo';
import { selectAddressId } from '../../redux/slices/authenticationSlice';
import Announcement from '../../components/Announcement';

const APP_BAR_MOBILE = 64;
const APP_BAR_DESKTOP = 92;

const RootStyle = styled('div')({
    display: 'flex',
    minHeight: '100%',
    overflow: 'hidden',
});

const MainStyle = styled('div')(({ theme }) => ({
    flexGrow: 1,
    overflow: 'auto',
    minHeight: '100%',
    paddingTop: APP_BAR_MOBILE + 24,
    paddingBottom: theme.spacing(10),
    [theme.breakpoints.up('lg')]: {
        paddingTop: APP_BAR_DESKTOP + 24,
        paddingLeft: theme.spacing(2),
        paddingRight: theme.spacing(2),
    },
}));

export default function DashboardLayout(): JSX.Element {
    const [open, setOpen] = useState<boolean>(false);
    const defaultAddressId = useSelector(selectAddressId);

    return (
        <RootStyle>
            <DashboardNavbar onOpenSidebar={() => setOpen(true)} />
            <DashboardSidebar
                isOpenSidebar={open}
                onCloseSidebar={() => setOpen(false)}
            />
            <MainStyle>
                <Box px={2} pb={2}>
                    <Hidden width="smUp">
                        <Announcement />
                    </Hidden>
                </Box>
                <Outlet />
                {!defaultAddressId && <AccountInfo />}
            </MainStyle>
        </RootStyle>
    );
}
