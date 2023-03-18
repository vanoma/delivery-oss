import React, { ReactNode } from 'react';
import {
    ListItemButton,
    ListItemButtonProps,
    ListItemIcon,
    ListItemText,
    useTheme,
} from '@mui/material';
import { styled } from '@mui/material/styles';
import { Link as RouterLink } from 'react-router-dom';
import { alpha } from '@mui/system';
import { SidebarConfigType } from './SidebarConfig';

const ListItemIconStyle = styled(ListItemIcon)({
    width: 22,
    height: 22,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
});

interface ListItemStyleProps extends ListItemButtonProps {
    component?: ReactNode;
    to?: string;
}

const ListItemStyle = styled(ListItemButton)<ListItemStyleProps>(
    ({ theme }) => ({
        ...theme.typography.body2,
        height: 48,
        position: 'relative',
        textTransform: 'capitalize',
        paddingLeft: theme.spacing(5),
        paddingRight: theme.spacing(2.5),
        color: theme.palette.text.secondary,
        '&:before': {
            top: 0,
            right: 0,
            width: 3,
            bottom: 0,
            content: "''",
            display: 'none',
            position: 'absolute',
            borderTopLeftRadius: 4,
            borderBottomLeftRadius: 4,
            backgroundColor: theme.palette.primary.main,
        },
    })
);

interface NavItemProps {
    item: SidebarConfigType;
    // eslint-disable-next-line no-unused-vars
    active: (path: string) => boolean;
}

export default function NavItem({ item, active }: NavItemProps): JSX.Element {
    const theme = useTheme();
    const isActiveRoot = active(item.path);

    const { title, path, Icon } = item;

    const listItemStyle = {
        ...theme.typography.body2,
        height: 48,
        position: 'relative' as const,
        textTransform: 'capitalize' as const,
        paddingLeft: theme.spacing(2),
        paddingRight: theme.spacing(2),
        color: theme.palette.text.secondary,
        '&:before': {
            top: 0,
            right: 0,
            width: 3,
            bottom: 0,
            content: "''",
            display: 'none',
            position: 'absolute',
            borderTopLeftRadius: 4,
            borderBottomLeftRadius: 4,
            backgroundColor: theme.palette.primary.main,
        },
    };

    const activeRootStyle = {
        color: 'primary.main',
        fontWeight: 'fontWeightMedium',
        backgroundColor: alpha(
            theme.palette.primary.main,
            theme.palette.action.selectedOpacity
        ),
        '&:before': { display: 'block' },
    };
    return (
        <ListItemStyle
            component={RouterLink}
            to={path}
            sx={{
                ...listItemStyle,
                ...(isActiveRoot && activeRootStyle),
            }}
        >
            <ListItemIconStyle>
                <Icon color={isActiveRoot ? 'primary' : 'inherit'} />
            </ListItemIconStyle>
            <ListItemText disableTypography primary={title} />
        </ListItemStyle>
    );
}
