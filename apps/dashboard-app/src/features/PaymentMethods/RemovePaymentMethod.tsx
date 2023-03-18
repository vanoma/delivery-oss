import React, { ReactElement } from 'react';
import { Typography, Box, Button, FormHelperText } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { LoadingIndicator, CustomModal } from '@vanoma/ui-components';
import { useDeletePaymentMethodMutation } from '../../api';

const RemovePaymentMethod = ({
    openRemovePaymentMethod,
    handleRemovePaymentMethodClose,
}: {
    openRemovePaymentMethod: { open: boolean; id: string | null };
    handleRemovePaymentMethodClose: () => void;
}): ReactElement => {
    const { t } = useTranslation();
    const [deletePaymentMethod, { isLoading, error }] =
        useDeletePaymentMethodMutation();

    return (
        <CustomModal
            open={openRemovePaymentMethod.open}
            handleClose={handleRemovePaymentMethodClose}
        >
            <Typography variant="h5">
                {t('billing.removePaymentMethodModal.removeThisPaymentMethod')}
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
                    onClick={handleRemovePaymentMethodClose}
                >
                    {t('billing.removePaymentMethodModal.no')}
                </Button>
                <Button
                    variant="outlined"
                    size="small"
                    onClick={() =>
                        deletePaymentMethod(openRemovePaymentMethod.id!)
                            .unwrap()
                            .then(handleRemovePaymentMethodClose)
                    }
                >
                    {isLoading ? (
                        <LoadingIndicator />
                    ) : (
                        t('billing.removePaymentMethodModal.yes')
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

export default RemovePaymentMethod;
