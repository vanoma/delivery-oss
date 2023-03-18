import React, { ReactElement } from 'react';
import { MenuItem, ListItemIcon, ListItemText } from '@mui/material';

interface Props {
    label: string;
    disabled: boolean;
    icon: ReactElement;
    onClick: () => void;
}

const ActionMenuItem: React.FC<Props> = ({
    icon,
    label,
    disabled,
    onClick,
}) => {
    return (
        <MenuItem
            disabled={disabled}
            onClick={onClick}
            sx={{ py: 1, px: 2.5, height: 48 }}
        >
            <ListItemIcon>{icon}</ListItemIcon>
            <ListItemText
                primaryTypographyProps={{
                    variant: 'body2',
                }}
            >
                {label}
            </ListItemText>
        </MenuItem>
    );
};

export default ActionMenuItem;
