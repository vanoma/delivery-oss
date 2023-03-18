import React, { FC, ReactNode } from 'react';
import { Popover, Theme } from '@mui/material';
import { styled } from '@mui/material/styles';
import { alpha, SxProps } from '@mui/system';

const ArrowStyle = styled('span')(({ theme }) => ({
    [theme.breakpoints.up('sm')]: {
        top: -7,
        zIndex: 1,
        width: 12,
        right: 20,
        height: 12,
        content: "''",
        position: 'absolute',
        borderRadius: '0 0 4px 0',
        transform: 'rotate(-135deg)',
        background: theme.palette.background.paper,
        borderRight: `solid 1px ${alpha(theme.palette.grey[500], 0.12)}`,
        borderBottom: `solid 1px ${alpha(theme.palette.grey[500], 0.12)}`,
    },
}));

interface MenuPopoverProps {
    children: ReactNode;
    sx?: SxProps<Theme>;
    anchorRef: React.MutableRefObject<Element | null>;
    open: boolean;
    onClose: () => void;
}

const MenuPopover: FC<MenuPopoverProps> = ({
    children,
    sx,
    anchorRef,
    open,
    onClose,
    ...other
}) => {
    return (
        <Popover
            anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
            transformOrigin={{ vertical: 'top', horizontal: 'right' }}
            PaperProps={{
                sx: {
                    mt: 1.5,
                    ml: 0.5,
                    overflow: 'inherit',
                    boxShadow: (theme) => theme.shadows[16],
                    border: (theme) =>
                        `solid 1px ${theme.palette.primary.light}`,
                    width: 200,
                    borderRadius: (theme) => theme.spacing(1.75),
                    ...sx,
                },
            }}
            open={open}
            onClose={onClose}
            anchorEl={anchorRef.current}
            {...other}
        >
            <ArrowStyle className="arrow" />
            {children}
        </Popover>
    );
};

export default MenuPopover;
