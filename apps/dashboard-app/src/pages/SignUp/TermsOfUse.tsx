import {
    Checkbox,
    FormHelperText,
    Stack,
    Typography,
    Link,
} from '@mui/material';
import { FormikProps } from 'formik';
import React from 'react';
import { useTranslation } from 'react-i18next';
import { AuthStep } from '../../types';
import { SignUpFormValues } from './types';

const TermsOfUse: React.FC<{
    formikProps: FormikProps<SignUpFormValues>;
    steps: AuthStep<SignUpFormValues>[];
    step: number;
}> = ({ formikProps, steps, step }) => {
    const { values, errors, touched, handleChange } = formikProps;
    const { t } = useTranslation();
    const termsOfServiceUrl = `${process.env.WEB_APP_URL}/terms-of-service`;

    return step === 0 ? (
        <>
            <Stack spacing={1} mt={2} direction="row">
                <Checkbox
                    name="checked"
                    value={values[steps[step].inputName]}
                    onChange={handleChange}
                    disableRipple
                    sx={{ padding: 0 }}
                />
                <Typography variant="body1" alignSelf="center">
                    {`${t('auth.signUpForm.termsOfUsePrefix')} `}
                    <Link target="_blank" href={termsOfServiceUrl}>
                        {t('auth.signUpForm.termsOfUseSuffix')}
                    </Link>
                </Typography>
            </Stack>
            <FormHelperText
                sx={{ ml: 1.75 }}
                error={!!(errors.checked && touched.checked)}
            >
                {(touched.checked && errors.checked) ?? null}
            </FormHelperText>
        </>
    ) : (
        <></>
    );
};

export default TermsOfUse;
