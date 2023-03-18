import React from 'react';
import { Button, Typography, useMediaQuery, Theme, Box } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import { LoadingIndicator, CustomModal } from '@vanoma/ui-components';
import { selectIsLoading } from './slice';

const ConfirmPlacement: React.FC<{
    open: boolean;
    onCloseModal: () => void;
    onConfirmPlacement: () => void;
}> = ({ open, onCloseModal, onConfirmPlacement }) => {
    const isSmall = useMediaQuery((theme: Theme) =>
        theme.breakpoints.down('sm')
    );
    const { t } = useTranslation();

    const isLoading = useSelector(selectIsLoading);

    return (
        <CustomModal open={open} handleClose={onCloseModal}>
            <Typography variant="h6" align="center">
                {t('delivery.confirmOrderPlaceModal.youWantToPlaceThisOrder')}
            </Typography>
            <Box sx={{ justifyContent: 'center', display: 'flex' }}>
                <Button
                    sx={{
                        height: 40,
                        mt: 2,
                    }}
                    size="small"
                    fullWidth={isSmall}
                    disabled={isLoading}
                    onClick={() => {
                        onConfirmPlacement();
                    }}
                >
                    {isLoading ? (
                        <LoadingIndicator />
                    ) : (
                        t('delivery.confirmOrderPlaceModal.confirm')
                    )}
                </Button>
            </Box>
        </CustomModal>
    );
};

export default ConfirmPlacement;
