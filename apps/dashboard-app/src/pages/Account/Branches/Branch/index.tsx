import React, { useRef, useState } from 'react';
import {
    Card,
    IconButton,
    ListItem,
    ListItemText,
    Typography,
} from '@mui/material';
import MoreHorizIcon from '@mui/icons-material/MoreHoriz';
import { MenuPopover } from '@vanoma/ui-components';
import { Branch } from '@vanoma/types';
import {
    formatAddressForPrivateView,
    formatContactForPrivateView,
} from '@vanoma/helpers';
import EditBranchMenuItem from './EditBranchMenuItem';
import DeleteBranchMenuItem from './DeleteBranchMenuItem';

const BranchView: React.FC<{
    branch: Branch;
}> = ({ branch }) => {
    const [openMenu, setOpenMenu] = useState(false);
    const anchorRef = useRef(null);

    const handleMenuOpen = (): void => {
        setOpenMenu(true);
    };
    const handleMenuClose = (): void => {
        setOpenMenu(false);
    };
    return (
        <Card sx={{ mb: 2 }}>
            <ListItem>
                <ListItemText
                    primary={branch.branchName}
                    secondary={
                        <>
                            {formatContactForPrivateView(branch.contact)}
                            <Typography variant="body2">
                                {formatAddressForPrivateView(branch.address)}
                            </Typography>
                        </>
                    }
                />
                <IconButton
                    size="small"
                    sx={{ p: 0.5, ml: 1 }}
                    ref={anchorRef}
                    onClick={handleMenuOpen}
                >
                    <MoreHorizIcon />
                </IconButton>
            </ListItem>
            <MenuPopover
                open={openMenu}
                onClose={handleMenuClose}
                anchorRef={anchorRef}
                sx={{ py: 1.25 }}
            >
                <EditBranchMenuItem
                    branch={branch}
                    handleMenuClose={handleMenuClose}
                />
                <DeleteBranchMenuItem
                    branch={branch}
                    handleMenuClose={handleMenuClose}
                />
            </MenuPopover>
        </Card>
    );
};

export default BranchView;
