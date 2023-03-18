import React from 'react';
import { Box, Button, FormHelperText, Typography } from '@mui/material';
import { CustomModal, LoadingIndicator } from '@vanoma/ui-components';
import { useTranslation } from 'react-i18next';

const DeleteContactModal: React.FC<{
    openDeleteContact: boolean;
    handleDeleteContactClose: () => void;
    isLoading: boolean;
    onYesClick: () => void;
    error: string | undefined;
}> = ({
    openDeleteContact,
    handleDeleteContactClose,
    isLoading,
    onYesClick,
    error,
}) => {
    const { t } = useTranslation();

    return (
        <CustomModal
            open={openDeleteContact}
            handleClose={handleDeleteContactClose}
        >
            <Typography variant="h5">
                {t('customers.deleteContactModal.deleteThisContact')}?
            </Typography>
            <Box
                sx={{
                    mt: 3,
                    display: 'flex',
                    justifyContent: 'space-between',
                }}
            >
                <Button
                    variant="outlined"
                    size="small"
                    onClick={handleDeleteContactClose}
                >
                    {t('customers.deleteContactModal.no')}
                </Button>
                <Button variant="outlined" size="small" onClick={onYesClick}>
                    {isLoading ? (
                        <LoadingIndicator />
                    ) : (
                        t('customers.deleteContactModal.yes')
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

export default DeleteContactModal;
