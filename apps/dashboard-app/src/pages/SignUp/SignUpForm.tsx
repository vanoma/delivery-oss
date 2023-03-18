import {
    Alert,
    Box,
    Button,
    Container,
    Stack,
    Typography,
} from '@mui/material';
import { Form, Formik, FormikHelpers, FormikProps } from 'formik';
import React from 'react';
import { useTranslation } from 'react-i18next';
import { Link as RouterLink } from 'react-router-dom';
import { LoadingIndicator } from '@vanoma/ui-components';
import { AUTH, SIGN_IN } from '../../routeNames';
import BusinessName from './BusinessName';
import Names from './Names';
import AuthTextField from '../../components/AuthTextField';
import { SignUpFormValues } from './types';
import { AuthStep } from '../../types';
import TermsOfUse from './TermsOfUse';

const SignUpForm: React.FC<{
    steps: AuthStep<SignUpFormValues>[];
    step: number;
    isLoading: boolean;
    errorMessage: string | null;
    phoneNumber: string | undefined;
    resetError: () => void;
}> = ({ steps, step, isLoading, errorMessage, phoneNumber, resetError }) => {
    const { t } = useTranslation();

    return (
        <Container>
            <Stack spacing={3}>
                <Formik
                    initialValues={steps[step].initialValues}
                    validationSchema={steps[step].validationSchema}
                    onSubmit={(
                        values: SignUpFormValues,
                        { resetForm }: FormikHelpers<SignUpFormValues>
                    ) => steps[step].handleSubmit(values, resetForm)}
                    enableReinitialize
                >
                    {(formikProps: FormikProps<SignUpFormValues>) => {
                        const { errors, touched } = formikProps;

                        return (
                            <Form>
                                <Stack spacing={1}>
                                    <Typography variant="body1">
                                        {`${t(steps[step].body.title)}${
                                            step === 1
                                                ? phoneNumber?.substring(9, 12)
                                                : ''
                                        }`}
                                    </Typography>
                                    <AuthTextField
                                        formikProps={formikProps}
                                        steps={steps}
                                        step={step}
                                    />
                                    <BusinessName
                                        formikProps={formikProps}
                                        steps={steps}
                                        step={step}
                                    />
                                    <Names
                                        formikProps={formikProps}
                                        step={step}
                                    />
                                    <TermsOfUse
                                        formikProps={formikProps}
                                        steps={steps}
                                        step={step}
                                    />
                                </Stack>
                                {errorMessage && (
                                    <Alert
                                        severity="error"
                                        onClose={resetError}
                                        sx={{ mt: 2 }}
                                    >
                                        {errorMessage}
                                    </Alert>
                                )}
                                <Stack
                                    spacing={2}
                                    mt={
                                        (errors.checked && touched.checked) ||
                                        step !== 0
                                            ? 2
                                            : 1
                                    }
                                >
                                    <Button type="submit" disabled={isLoading}>
                                        {isLoading ? (
                                            <LoadingIndicator />
                                        ) : (
                                            t(steps[step].body.buttonText)
                                        )}
                                    </Button>
                                    <Typography variant="body1">
                                        {`${t(
                                            'auth.signUpForm.alreadyHaveAccount'
                                        )} `}
                                        <Box
                                            component={RouterLink}
                                            to={`${AUTH}/${SIGN_IN}`}
                                        >
                                            {t('auth.signUpForm.signInHere')}
                                        </Box>
                                    </Typography>
                                </Stack>
                            </Form>
                        );
                    }}
                </Formik>
            </Stack>
        </Container>
    );
};

export default SignUpForm;
