import { TextField, Typography } from '@mui/material';
import { FormikProps } from 'formik';
import React from 'react';
import { useTranslation } from 'react-i18next';
import { SignUpFormValues } from './types';

const Names: React.FC<{
    formikProps: FormikProps<SignUpFormValues>;
    step: number;
}> = ({ formikProps, step }) => {
    const { values, errors, touched, handleChange } = formikProps;
    const { t } = useTranslation();

    return step === 2 ? (
        <>
            <Typography variant="body1">
                {t('auth.signUpForm.enterYourLastName')}
            </Typography>
            <TextField
                name="lastName"
                label={t('auth.signUpForm.lastName')}
                value={values.lastName ?? ''}
                onChange={handleChange}
                error={!!(errors.lastName && touched.lastName)}
                helperText={(touched.lastName && errors.lastName) ?? undefined}
            />
        </>
    ) : (
        <></>
    );
};

export default Names;
