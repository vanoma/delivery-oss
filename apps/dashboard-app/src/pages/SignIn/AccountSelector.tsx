import {
    Box,
    FormHelperText,
    MenuItem,
    Select,
    LinearProgress,
} from '@mui/material';
import { makeStyles } from '@mui/styles';
import { FormikProps } from 'formik';
import React from 'react';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import { CustomSnackBar } from '@vanoma/ui-components';
import { useGetCustomersQuery } from '../../api';
import { selectAccounts } from '../../redux/slices/authenticationSlice';

const useStyles = makeStyles({
    paper: {
        borderRadius: 16,
        maxHeight: 140,
        overflow: 'scroll',
    },
});

interface FormValues {
    customerId?: string;
}

const AccountSelector: React.FC<{
    formikProps: FormikProps<FormValues>;
}> = ({ formikProps }) => {
    const classes = useStyles();
    const { t } = useTranslation();
    const accounts = useSelector(selectAccounts);

    const { data, error, isLoading, refetch } = useGetCustomersQuery(
        accounts.map(({ customerId }) => customerId),
        { skip: accounts.length === 0 || accounts.length === 1 }
    );

    return (
        <Box>
            <Select
                onChange={formikProps.handleChange}
                displayEmpty
                MenuProps={{
                    classes: {
                        paper: classes.paper,
                    },
                }}
                fullWidth
                value={formikProps.values.customerId ?? ''}
                name="customerId"
            >
                <MenuItem value="" disabled>
                    {t('auth.signInForm.selectAccount')}
                </MenuItem>
                {data ? (
                    data.results.map(({ customerId, businessName }) => (
                        <MenuItem value={customerId} key={customerId}>
                            {businessName}
                        </MenuItem>
                    ))
                ) : (
                    <MenuItem />
                )}
            </Select>
            {formikProps.touched.customerId && formikProps.errors.customerId && (
                <FormHelperText
                    error
                    sx={{
                        mt: 0.5,
                        mx: 1.75,
                    }}
                >
                    {formikProps.errors.customerId}
                </FormHelperText>
            )}
            {isLoading && <LinearProgress />}
            <CustomSnackBar
                message={error as string}
                severity="error"
                onRetry={refetch}
            />
        </Box>
    );
};

export default AccountSelector;
