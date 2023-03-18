import { Box, Button, FormHelperText, Typography } from '@mui/material';
import { CustomModal, LoadingIndicator } from '@vanoma/ui-components';
import React from 'react';
import { useTranslation } from 'react-i18next';
import { useDeleteBranchMutation } from '../../../../../api';

const DeleteBranchModal: React.FC<{
    open: boolean;
    handleClose: () => void;
    branchId: string;
}> = ({ open, handleClose, branchId }) => {
    const { t } = useTranslation();
    const [deleteBranch, { isLoading, error }] = useDeleteBranchMutation();

    const handleDeleteBranch = (): void => {
        deleteBranch(branchId).unwrap().then(handleClose);
    };

    return (
        <CustomModal open={open} handleClose={handleClose}>
            <Typography variant="h5">
                {t('account.deleteBranchModal.deleteThisBranch')}?
            </Typography>
            <Box
                sx={{
                    mt: 3,
                    display: 'flex',
                    justifyContent: 'space-between',
                }}
            >
                <Button variant="outlined" size="small" onClick={handleClose}>
                    {t('account.deleteBranchModal.no')}
                </Button>
                <Button
                    variant="outlined"
                    size="small"
                    onClick={handleDeleteBranch}
                >
                    {isLoading ? (
                        <LoadingIndicator />
                    ) : (
                        t('account.deleteBranchModal.yes')
                    )}
                </Button>
            </Box>
            {error && (
                <FormHelperText sx={{ mt: 2 }} error>
                    {error}
                </FormHelperText>
            )}
        </CustomModal>
    );
};

export default DeleteBranchModal;
