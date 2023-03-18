import React from 'react';
import { Box, List } from '@mui/material';
import { useLocation, matchPath } from 'react-router-dom';
import FormatListBulletedIcon from '@mui/icons-material/FormatListBulleted';
import PriceCheckIcon from '@mui/icons-material/PriceCheck';
import PinIcon from '@mui/icons-material/Pin';
import PeopleIcon from '@mui/icons-material/People';
import NavItem from './NavItem';
import {
    CHECK_PRICE,
    CUSTOMERS,
    DELIVERIES,
    VERIFICATIONS,
} from '../../../../routeNames';

const sidebarConfig = [
    {
        title: 'deliveries',
        path: DELIVERIES,
        Icon: FormatListBulletedIcon,
    },
    {
        title: 'check price',
        path: CHECK_PRICE,
        Icon: PriceCheckIcon,
    },
    {
        title: 'verifications',
        path: VERIFICATIONS,
        Icon: PinIcon,
    },
    {
        title: 'Customers',
        path: CUSTOMERS,
        Icon: PeopleIcon,
    },
];

export default function NavMenu(): JSX.Element {
    const { pathname } = useLocation();
    // eslint-disable-next-line no-unused-vars
    const match: (path: string) => boolean = (path: string) =>
        path ? !!matchPath({ path, end: false }, pathname) : false;

    return (
        <Box>
            <List disablePadding>
                {sidebarConfig.map((item) => (
                    <NavItem key={item.title} item={item} active={match} />
                ))}
            </List>
        </Box>
    );
}
