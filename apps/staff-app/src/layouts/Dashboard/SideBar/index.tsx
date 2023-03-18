import React, { useEffect, ReactElement } from 'react';
import { styled } from '@mui/material/styles';
import { useLocation } from 'react-router-dom';
import { Drawer } from '@mui/material';
import { Hidden } from '@vanoma/ui-components';
import Header from './Header';
import NavMenu from './NavMenu';

const DRAWER_WIDTH = 280;

const RootStyle = styled('div')(({ theme }) => ({
    [theme.breakpoints.up('lg')]: {
        flexShrink: 0,
        width: DRAWER_WIDTH,
    },
}));

export default function SideBar({
    isOpenSidebar,
    onCloseSidebar,
}: {
    isOpenSidebar: boolean;
    onCloseSidebar: () => void;
}): ReactElement {
    const { pathname } = useLocation();

    useEffect(() => {
        if (isOpenSidebar) {
            onCloseSidebar();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [pathname]);

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
                    <Header />
                    <NavMenu />
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
                    <Header />
                    <NavMenu />
                </Drawer>
            </Hidden>
        </RootStyle>
    );
}
