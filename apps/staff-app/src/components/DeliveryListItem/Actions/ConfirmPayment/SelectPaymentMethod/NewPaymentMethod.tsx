import React, { ReactElement } from 'react';
import {
    Button,
    Stack,
    TextField,
    Typography,
    FormHelperText,
} from '@mui/material';
import { Form, Formik } from 'formik';
import * as Yup from 'yup';
import { CustomModal, LoadingIndicator } from '@vanoma/ui-components';
import { removeSpaces, prefixNumberWithCountryCode } from '@vanoma/helpers';
import { PaymentMethod, PaymentMethodType } from '@vanoma/types';
import { phoneNumberSchema } from '../../../../../helpers/yupSchema';
import { useCreatePaymentMethodMutation } from '../../../../../api';

const NewPaymentMethod = ({
    openNewPaymentMethod,
    handleNewPaymentMethodClose,
    customerId,
    callback,
    initialPhoneNumber,
}: {
    openNewPaymentMethod: boolean;
    handleNewPaymentMethodClose: () => void;
    customerId: string;
    // eslint-disable-next-line no-unused-vars
    callback: (newPaymentMethod: PaymentMethod) => void;
    initialPhoneNumber: string;
}): ReactElement => {
    const [createPaymentMethod, { error, isLoading }] =
        useCreatePaymentMethodMutation();

    return (
        <CustomModal
            open={openNewPaymentMethod}
            handleClose={handleNewPaymentMethodClose}
        >
            <Formik
                initialValues={{
                    phoneNumber: initialPhoneNumber,
                    shortCode: '',
                }}
                onSubmit={({ phoneNumber, shortCode }) => {
                    createPaymentMethod({
                        customerId,
                        type: PaymentMethodType.MOBILE_MONEY,
                        extra: {
                            shortCode:
                                shortCode.length > 0 ? shortCode : undefined,
                            phoneNumber:
                                prefixNumberWithCountryCode(phoneNumber),
                        },
                    })
                        .unwrap()
                        .then((paymentMethod) => {
                            callback(paymentMethod);
                            handleNewPaymentMethodClose();
                        });
                }}
                validationSchema={Yup.object().shape({
                    phoneNumber: phoneNumberSchema('payment'),
                    shortCode: Yup.string()
                        .matches(
                            RegExp(`^[0-9]+$`),
                            'Momo code must be a number'
                        )
                        .test(
                            'len',
                            'Momo code must be 5 or 6 numbers',
                            (value) =>
                                value === undefined ||
                                value.length === 0 ||
                                value.length === 5 ||
                                value.length === 6
                        ),
                })}
            >
                {({ values, setFieldValue, errors, touched }) => (
                    <Form>
                        <Typography id="transition-modal-title" variant="h5">
                            Add new payment method
                        </Typography>
                        <Stack spacing={2} sx={{ alignItems: 'center', mt: 3 }}>
                            <TextField
                                label="Phone number"
                                name="phoneNumber"
                                value={values.phoneNumber}
                                onChange={(e) => {
                                    setFieldValue(
                                        'phoneNumber',
                                        removeSpaces(e.target.value)
                                    );
                                }}
                                error={
                                    !!(
                                        errors.phoneNumber &&
                                        touched.phoneNumber
                                    )
                                }
                                helperText={
                                    (touched.phoneNumber &&
                                        errors.phoneNumber) ??
                                    undefined
                                }
                                size="small"
                                fullWidth
                            />
                            <TextField
                                label="Momo code (optional)"
                                name="shortCode"
                                value={values.shortCode}
                                onChange={(e) => {
                                    setFieldValue(
                                        'shortCode',
                                        removeSpaces(e.target.value)
                                    );
                                }}
                                error={
                                    !!(errors.shortCode && touched.shortCode)
                                }
                                helperText={
                                    (touched.shortCode && errors.shortCode) ??
                                    undefined
                                }
                                size="small"
                                fullWidth
                            />
                            <Button
                                type="submit"
                                sx={{ height: 40 }}
                                fullWidth
                                disabled={isLoading}
                            >
                                {isLoading ? <LoadingIndicator /> : 'Add'}
                            </Button>
                        </Stack>
                    </Form>
                )}
            </Formik>
            {error && (
                <FormHelperText sx={{ mt: 2 }} error>
                    {error}
                </FormHelperText>
            )}
        </CustomModal>
    );
};

export default NewPaymentMethod;
