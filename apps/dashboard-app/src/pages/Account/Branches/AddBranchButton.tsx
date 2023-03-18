import { Button } from '@mui/material';
import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import BranchFormModal from './Branch/BranchFormModal';

const AddBranchButton: React.FC = () => {
    const { t } = useTranslation();
    const [open, setOpen] = useState(false);

    const handleOpen = (): void => {
        setOpen(true);
    };
    const handleClose = (): void => {
        setOpen(false);
    };

    return (
        <>
            <Button size="medium" onClick={() => handleOpen()}>
                {t('account.branches.newBranch')}
            </Button>
            {open && <BranchFormModal handleClose={handleClose} open={open} />}
        </>
    );
};

export default AddBranchButton;
