import { Stack, TextField } from '@mui/material';
import { removeSpaces } from '@vanoma/helpers';
import { Form, Formik, FormikHelpers, FormikProps } from 'formik';
import React from 'react';
import { useTranslation } from 'react-i18next';
import * as Yup from 'yup';
import { phoneNumberSchema } from '../helpers/yupSchema';

interface NewContactFormValues {
    name: string;
    phoneNumber: string;
    isCustomerPaying?: boolean;
}

interface Props<T> {
    initialValues: T;
    onSubmit: (
        // eslint-disable-next-line no-unused-vars
        values: T,
        // eslint-disable-next-line no-unused-vars
        formikHelpers: FormikHelpers<T>
    ) => void | Promise<any>;
    // eslint-disable-next-line no-unused-vars
    children: (props: FormikProps<T>) => React.ReactNode;
}

const NewContactForm = <T extends NewContactFormValues>({
    initialValues,
    onSubmit,
    children,
}: Props<T>): JSX.Element => {
    const { t } = useTranslation();

    const contactValidationSchema = Yup.object().shape({
        name: Yup.string(),
        phoneNumber: phoneNumberSchema(t),
        // anotherPhoneNumber: Yup.string()
        //     .matches(RegExp(`^[0-9]+$`), 'Phone number must be a number')
        //     .test('len', 'Phone number is invalid', (value) =>
        //         value !== undefined
        //             ? (value.startsWith('07') && value.length === 10) ||
        //               (value.startsWith('2507') && value.length === 12)
        //             : false
        //     ),
    });

    return (
        <Formik
            initialValues={initialValues}
            validationSchema={contactValidationSchema}
            onSubmit={onSubmit}
        >
            {(formikProps: FormikProps<T>) => {
                const { values, handleChange, setFieldValue, errors, touched } =
                    formikProps;
                return (
                    <Form>
                        <Stack spacing={2} sx={{ mt: 3 }}>
                            <TextField
                                label={t(
                                    'customers.linkGeneratorModal.customerPhoneNumber'
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
                            {values.isCustomerPaying === undefined && (
                                <TextField
                                    label={t('delivery.newContact.name')}
                                    name="name"
                                    value={values.name}
                                    onChange={handleChange}
                                    error={!!(errors.name && touched.name)}
                                    helperText={
                                        (touched.name && errors.name) ??
                                        undefined
                                    }
                                    size="small"
                                    fullWidth
                                />
                            )}
                            {children(formikProps)}
                        </Stack>
                    </Form>
                );
            }}
        </Formik>
    );
};

export default NewContactForm;
