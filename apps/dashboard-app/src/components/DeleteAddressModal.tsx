import { Box, Button, FormHelperText, Typography } from '@mui/material';
import { CustomModal, LoadingIndicator } from '@vanoma/ui-components';
import React from 'react';
import { useTranslation } from 'react-i18next';

const DeleteAddressModal: React.FC<{
    openDeleteAddress: boolean;
    handleDeleteAddressClose: () => void;
    isLoading: boolean;
    onYesClick: () => void;
    error: string | undefined;
}> = ({
    openDeleteAddress,
    handleDeleteAddressClose,
    isLoading,
    onYesClick,
    error,
}) => {
    const { t } = useTranslation();
    return (
        <CustomModal
            open={openDeleteAddress}
            handleClose={handleDeleteAddressClose}
        >
            <Typography variant="h5">
                {t('customers.deleteAddressModal.deleteThisAddress')}?
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
                    onClick={handleDeleteAddressClose}
                >
                    {t('customers.deleteAddressModal.no')}
                </Button>
                <Button variant="outlined" size="small" onClick={onYesClick}>
                    {isLoading ? (
                        <LoadingIndicator />
                    ) : (
                        t('customers.deleteAddressModal.yes')
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

export default DeleteAddressModal;
