import { TextField, Typography } from '@mui/material';
import { FormikProps } from 'formik';
import React from 'react';
import { useTranslation } from 'react-i18next';
import { AuthStep } from '../../types';
import { SignUpFormValues } from './types';

const BusinessName: React.FC<{
    formikProps: FormikProps<SignUpFormValues>;
    steps: AuthStep<SignUpFormValues>[];
    step: number;
}> = ({ formikProps, steps, step }) => {
    const { values, errors, touched, handleChange } = formikProps;
    const { t } = useTranslation();

    return step === 1 &&
        !errors[steps[step].inputName] &&
        values[steps[step].inputName] !== '' ? (
        <>
            <Typography variant="body1">
                {t('auth.signUpForm.enterBusinessName')}
            </Typography>
            <TextField
                name="businessName"
                label={t('auth.signUpForm.businessName')}
                value={values.businessName ?? ''}
                onChange={handleChange}
                error={!!(errors.businessName && touched.businessName)}
                helperText={
                    (touched.businessName && errors.businessName) ?? undefined
                }
            />
        </>
    ) : (
        <></>
    );
};

export default BusinessName;
