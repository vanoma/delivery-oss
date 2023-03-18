import React from 'react';
import { Box, List } from '@mui/material';
import { useLocation, matchPath } from 'react-router-dom';
import { SidebarConfigType } from './SidebarConfig';
import NavItem from './NavItem';

export default function NavSection({
    navConfig,
}: {
    navConfig: SidebarConfigType[];
}): JSX.Element {
    const { pathname } = useLocation();
    // eslint-disable-next-line no-unused-vars
    const match: (path: string) => boolean = (path: string) =>
        path ? !!matchPath({ path, end: false }, pathname) : false;

    return (
        <Box>
            <List disablePadding>
                {navConfig.map((item) => (
                    <NavItem key={item.title} item={item} active={match} />
                ))}
            </List>
        </Box>
    );
}
