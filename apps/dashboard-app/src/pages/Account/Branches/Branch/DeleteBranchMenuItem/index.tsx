import React, { useState } from 'react';
import { ListItemIcon, ListItemText, MenuItem } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import { Branch } from '@vanoma/types';
import { useTranslation } from 'react-i18next';
import DeleteBranchModal from './DeleteBranchModal';

const DeleteBranchMenuItem: React.FC<{
    branch: Branch;
    handleMenuClose: () => void;
}> = ({ branch, handleMenuClose }) => {
    const { t } = useTranslation();
    const [openDeleteBranch, setOpenDeleteBranch] = useState(false);

    const handleDeleteBranchOpen = (): void => {
        setOpenDeleteBranch(true);
    };
    const handleDeleteBranchClose = (): void => {
        setOpenDeleteBranch(false);
        handleMenuClose();
    };

    return (
        <>
            <MenuItem
                sx={{ py: 1, px: 2.5, height: 48 }}
                onClick={handleDeleteBranchOpen}
            >
                <ListItemIcon>
                    <DeleteIcon />
                </ListItemIcon>
                <ListItemText
                    primaryTypographyProps={{
                        variant: 'body2',
                    }}
                >
                    {t('account.branches.delete')}
                </ListItemText>
            </MenuItem>
            <DeleteBranchModal
                open={openDeleteBranch}
                handleClose={handleDeleteBranchClose}
                branchId={branch.branchId}
            />
        </>
    );
};

export default DeleteBranchMenuItem;
