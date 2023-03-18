import React, { useState } from 'react';
import { ListItemIcon, ListItemText, MenuItem } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import { Branch } from '@vanoma/types';
import { CustomModal } from '@vanoma/ui-components';
import { useTranslation } from 'react-i18next';
import BranchFormModal from './BranchFormModal';

const EditBranchMenuItem: React.FC<{
    branch: Branch;
    handleMenuClose: () => void;
}> = ({ branch, handleMenuClose }) => {
    const { t } = useTranslation();
    const [openModal, setOpenModal] = useState(false);

    const handleModalOpen = (): void => {
        setOpenModal(true);
    };
    const handleModalClose = (): void => {
        setOpenModal(false);
        handleMenuClose();
    };

    return (
        <>
            <MenuItem
                sx={{ py: 1, px: 2.5, height: 48 }}
                onClick={handleModalOpen}
            >
                <ListItemIcon>
                    <EditIcon />
                </ListItemIcon>
                <ListItemText
                    primaryTypographyProps={{
                        variant: 'body2',
                    }}
                >
                    {t('account.branches.edit')}
                </ListItemText>
            </MenuItem>
            <CustomModal
                open={openModal}
                handleClose={handleModalClose}
                sx={{ p: 0, width: 700 }}
            >
                <BranchFormModal
                    handleClose={handleModalClose}
                    branch={branch}
                    open={openModal}
                />
            </CustomModal>
        </>
    );
};

export default EditBranchMenuItem;
