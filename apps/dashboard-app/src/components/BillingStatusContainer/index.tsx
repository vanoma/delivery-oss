import React, { ReactNode } from 'react';
import {
    LinearProgress,
    Box,
    FormHelperText,
    TextField,
    Typography,
} from '@mui/material';
import { useSelector } from 'react-redux';
import { CustomSnackBar } from '@vanoma/ui-components';
import { useTranslation } from 'react-i18next';
import { useGetBillingStatusQuery } from '../../api';
import { selectCustomerId } from '../../redux/slices/authenticationSlice';
import PaymentDueAlert from './PaymentDueAlert';

const BillingStatusContainer: React.FC<{
    isModal?: boolean;
    children: ReactNode;
}> = ({ isModal, children }) => {
    const { t } = useTranslation();
    const customerId = useSelector(selectCustomerId);
    const { data, error, isLoading, refetch } = useGetBillingStatusQuery(
        customerId!
    );

    return (
        <Box>
            {isLoading && <LinearProgress />}
            {data && (
                <>
                    {data.isBillDue ? (
                        <>
                            <PaymentDueAlert gracePeriod={data.gracePeriod} />
                            {data.gracePeriod > 0 && children}
                            {isModal && data.gracePeriod === 0 && (
                                <>
                                    <Typography
                                        variant="h5"
                                        align="center"
                                        mb={3}
                                    >
                                        {t(
                                            'customers.linkGeneratorModal.getDeliveryLink'
                                        )}
                                    </Typography>
                                    <TextField
                                        label={t(
                                            'customers.linkGeneratorModal.customerPhoneNumber'
                                        )}
                                        fullWidth
                                        size="small"
                                        disabled
                                    />
                                </>
                            )}
                        </>
                    ) : (
                        children
                    )}
                </>
            )}
            {error && (
                <>
                    {isModal ? (
                        <FormHelperText error sx={{ mt: 0.5, mx: 1.75 }}>
                            {error}
                        </FormHelperText>
                    ) : (
                        <CustomSnackBar
                            message={error as string}
                            severity="error"
                            onRetry={refetch}
                        />
                    )}
                </>
            )}
        </Box>
    );
};

export default BillingStatusContainer;
