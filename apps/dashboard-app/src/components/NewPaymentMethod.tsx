/* eslint-disable react/require-default-props */
import {
    Button,
    FormHelperText,
    Stack,
    TextField,
    Typography,
} from '@mui/material';
import { Form, Formik, FormikState } from 'formik';
import React, { ReactElement } from 'react';
import { useTranslation } from 'react-i18next';
import * as Yup from 'yup';
import { LoadingIndicator, CustomModal } from '@vanoma/ui-components';
import { prefixNumberWithCountryCode, removeSpaces } from '@vanoma/helpers';
import { useSelector } from 'react-redux';
import { PaymentMethodType } from '@vanoma/types';
import { phoneNumberSchema } from '../helpers/yupSchema';
import { useCreatePaymentMethodMutation } from '../api';
import { PaymentMethod } from '../types';
import { selectCustomerId } from '../redux/slices/authenticationSlice';

interface FormValues {
    phoneNumber: string;
    shortCode: string;
}

const NewPaymentMethod = ({
    openNewPaymentMethod,
    handleNewPaymentMethodClose,
    phoneNumberPreFill,
    onSelect,
}: {
    openNewPaymentMethod: boolean;
    handleNewPaymentMethodClose: () => void;
    phoneNumberPreFill: string;
    // eslint-disable-next-line no-unused-vars
    onSelect?: (value: PaymentMethod | null) => void;
}): ReactElement => {
    const customerId = useSelector(selectCustomerId);
    const { t } = useTranslation();

    const [createPaymentMethod, { isLoading, error }] =
        useCreatePaymentMethodMutation();

    const validationSchema = Yup.object().shape({
        phoneNumber: phoneNumberSchema(t, 'payment'),
        shortCode: Yup.string()
            .matches(
                RegExp(`^[0-9]+$`),
                t('alertAndValidationMessages.momoCodeMustBeNumber')
            )
            .test(
                'len',
                t('alertAndValidationMessages.momoCodeMustHaveValidLength'),
                (value) =>
                    value === undefined ||
                    value.length === 0 ||
                    value.length === 5 ||
                    value.length === 6
            ),
    });

    const handleSubmit = (
        { phoneNumber, shortCode }: FormValues,
        {
            resetForm,
        }: // eslint-disable-next-line no-unused-vars
        { resetForm: (nextState?: Partial<FormikState<FormValues>>) => void }
    ): void => {
        const formattedPhoneNumber = prefixNumberWithCountryCode(phoneNumber);
        const newPaymentMethod = shortCode
            ? {
                  phoneNumber: formattedPhoneNumber,
                  shortCode,
              }
            : { phoneNumber: formattedPhoneNumber };

        createPaymentMethod({
            customerId: customerId!,
            type: PaymentMethodType.MOBILE_MONEY,
            ...newPaymentMethod,
        })
            .unwrap()
            .then((paymentMethod) => {
                if (onSelect) {
                    onSelect(paymentMethod);
                }

                resetForm();
                handleNewPaymentMethodClose();
            });
    };

    return (
        <CustomModal
            open={openNewPaymentMethod}
            handleClose={handleNewPaymentMethodClose}
        >
            <Formik
                initialValues={{
                    phoneNumber:
                        phoneNumberPreFill && phoneNumberPreFill.match(/^\d+$/)
                            ? phoneNumberPreFill
                            : '',
                    shortCode: '',
                }}
                onSubmit={handleSubmit}
                validationSchema={validationSchema}
            >
                {({ values, setFieldValue, errors, touched }) => (
                    <Form>
                        <Typography id="transition-modal-title" variant="h5">
                            {t(
                                'billing.newPaymentMethodModal.addNewPaymentMethod'
                            )}
                        </Typography>
                        <Stack spacing={2} sx={{ alignItems: 'center', mt: 3 }}>
                            <TextField
                                label={t(
                                    'billing.newPaymentMethodModal.phoneNumber'
                                )}
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
                                label={t(
                                    'billing.newPaymentMethodModal.momoCodeOptional'
                                )}
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
                                {isLoading ? (
                                    <LoadingIndicator />
                                ) : (
                                    t('billing.newPaymentMethodModal.add')
                                )}
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
