import React, { ReactElement, useState } from 'react';
import {
    Autocomplete,
    Box,
    FormHelperText,
    ListItemIcon,
    ListItemText,
    MenuItem,
    Paper,
    TextField,
    Typography,
} from '@mui/material';
import { FormikProps } from 'formik';
import { localizePhoneNumber } from '@vanoma/helpers';
import { PaymentMethod } from '@vanoma/types';
import AddIcon from '@mui/icons-material/Add';
import NewPaymentMethod from './NewPaymentMethod';
import { useGetPaymentMethodsQuery } from '../../../../../api';
import { PaymentFormValue } from '../types';

const findPaymentMethod = (
    methodId: string,
    paymentMethods: PaymentMethod[]
): PaymentMethod | null =>
    paymentMethods.find(
        ({ paymentMethodId }) => paymentMethodId === methodId
    ) ?? null;

const SelectPaymentMethod = ({
    formikProps,
    customerId,
}: {
    formikProps: FormikProps<PaymentFormValue>;
    customerId: string;
}): ReactElement => {
    const [openNewPaymentMethod, setOpenNewPaymentMethod] = useState(false);
    const [inputValue, setInputValue] = useState('');

    const { data, error } = useGetPaymentMethodsQuery(customerId);

    const handleNewPaymentMethodClose = (): void => {
        setOpenNewPaymentMethod(false);
    };

    const paymentMethods = data ? data.results : [];

    return (
        <>
            <Box width="100%">
                <Autocomplete
                    value={findPaymentMethod(
                        formikProps.values.paymentMethodId,
                        paymentMethods
                    )}
                    onChange={(event: any, value: PaymentMethod | null) => {
                        if (value) {
                            formikProps.setFieldValue(
                                'paymentMethodId',
                                value!.paymentMethodId
                            );
                            setInputValue(
                                localizePhoneNumber(value!.extra.phoneNumber)
                            );
                        } else {
                            formikProps.setFieldValue('paymentMethodId', '');
                            setInputValue('');
                        }
                    }}
                    inputValue={inputValue}
                    onInputChange={(event, newInputValue) => {
                        setInputValue(newInputValue);
                    }}
                    options={paymentMethods}
                    isOptionEqualToValue={(option, value) =>
                        option.paymentMethodId === value.paymentMethodId
                    }
                    getOptionLabel={(option) =>
                        localizePhoneNumber(option.extra.phoneNumber)
                    }
                    renderInput={(params) => (
                        // eslint-disable-next-line react/jsx-props-no-spreading
                        <TextField {...params} label="Payment method" />
                    )}
                    fullWidth
                    size="small"
                    noOptionsText={
                        <>
                            <Typography mb={2}>Not found</Typography>
                            <MenuItem
                                value="new"
                                sx={{
                                    border: (theme) =>
                                        `1px solid ${theme.palette.primary.light}`,
                                }}
                                onClick={() => setOpenNewPaymentMethod(true)}
                            >
                                <ListItemIcon>
                                    <AddIcon color="primary" />
                                </ListItemIcon>
                                <ListItemText
                                    primaryTypographyProps={{
                                        color: 'primary',
                                        sx: { fontWeight: 'bold' },
                                    }}
                                >
                                    New payment method
                                </ListItemText>
                            </MenuItem>
                        </>
                    }
                    PaperComponent={(props) => (
                        <Paper
                            // eslint-disable-next-line react/jsx-props-no-spreading
                            {...props}
                            elevation={8}
                            sx={{ borderRadius: 0.5 }}
                        />
                    )}
                />
                {formikProps.touched.paymentMethodId &&
                formikProps.errors.paymentMethodId ? (
                    <FormHelperText
                        error={
                            !!(
                                formikProps.errors.paymentMethodId &&
                                formikProps.touched.paymentMethodId
                            )
                        }
                        sx={{ mt: 0.5, mx: 1.75 }}
                    >
                        {formikProps.errors.paymentMethodId}
                    </FormHelperText>
                ) : undefined}
            </Box>
            <NewPaymentMethod
                openNewPaymentMethod={openNewPaymentMethod}
                handleNewPaymentMethodClose={handleNewPaymentMethodClose}
                customerId={customerId}
                callback={({ paymentMethodId }) => {
                    formikProps.setFieldValue(
                        'paymentMethodId',
                        paymentMethodId
                    );
                }}
                initialPhoneNumber={inputValue}
            />
            {error && (
                <FormHelperText sx={{ mt: 2 }} error>
                    {error}
                </FormHelperText>
            )}
        </>
    );
};

export default SelectPaymentMethod;
