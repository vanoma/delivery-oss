import React, { FC, useEffect, useState } from 'react';
import {
    Button,
    Container,
    Stack,
    Typography,
    Box,
    Alert,
} from '@mui/material';
import * as Yup from 'yup';
import { Form, Formik, FormikHelpers, FormikProps } from 'formik';
import { Link as RouterLink } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { LoadingIndicator } from '@vanoma/ui-components';
import { prefixNumberWithCountryCode } from '@vanoma/helpers';
import { useTranslation } from 'react-i18next';
import {
    verificationCodeSchema,
    phoneNumberSchema,
} from '../../helpers/yupSchema';
import { useTypedDispatch } from '../../helpers/reduxToolkit';
import {
    signIn,
    sendOtp,
    resetError,
    selectError,
    selectOtpData,
    selectIsLoading,
    selectAccounts,
    saveAccount,
} from '../../redux/slices/authenticationSlice';
import '../../locales/i18n';
import { AUTH, SIGN_UP } from '../../routeNames';
import AuthTextField from '../../components/AuthTextField';
import { Account, AuthStep } from '../../types';
import AccountSelector from './AccountSelector';

interface SignInFormValues {
    phoneNumber?: string;
    verificationCode?: string;
    customerId?: string;
}

const SignIn: FC = () => {
    const [step, setStep] = useState<number>(0);
    const otpData = useSelector(selectOtpData);
    const isLoading = useSelector(selectIsLoading);
    const error = useSelector(selectError);
    const accounts = useSelector(selectAccounts);

    const dispatch = useTypedDispatch();
    const { t } = useTranslation();

    useEffect(() => {
        dispatch(resetError());
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const steps: AuthStep<SignInFormValues>[] = [
        {
            label: 'auth.signInForm.number',
            body: {
                title: '',
                inputLabel: 'auth.signInForm.phoneNumber',
                buttonText: 'auth.signInForm.verify',
            },
            validationSchema: Yup.object().shape({
                phoneNumber: phoneNumberSchema(t),
            }),
            initialValues: {
                phoneNumber: '',
            },
            inputName: 'phoneNumber',
            handleSubmit: (values, resetForm) => {
                dispatch(
                    sendOtp({
                        phoneNumber: prefixNumberWithCountryCode(
                            values.phoneNumber!
                        ),
                    })
                )
                    .unwrap()
                    .then(() => {
                        resetForm();
                        setStep(step + 1);
                    });
            },
        },
        {
            label: 'auth.signInForm.verify',
            body: {
                title: '',
                inputLabel: 'auth.signInForm.verificationCode',
                buttonText: 'auth.signInForm.signIn',
            },
            validationSchema: Yup.object().shape({
                verificationCode: verificationCodeSchema(t),
            }),
            initialValues: {
                verificationCode: '',
            },
            inputName: 'verificationCode',
            handleSubmit: (values, resetForm) => {
                dispatch(
                    signIn({
                        phoneNumber: otpData!.phoneNumber,
                        verificationId: otpData!.otpId,
                        verificationCode: values.verificationCode!,
                    })
                )
                    .unwrap()
                    .then((result) => {
                        if (result.length === 1) {
                            const { customerId, agentId } =
                                result[0] as Account;
                            dispatch(saveAccount({ customerId, agentId }));
                        } else {
                            resetForm();
                            setStep(step + 1);
                        }
                    });
            },
        },
        {
            label: '',
            body: {
                title: '',
                inputLabel: '',
                buttonText: 'auth.signInForm.continue',
            },
            validationSchema: Yup.object().shape({
                customerId: Yup.string().required(
                    t('alertAndValidationMessages.accountRequired')
                ),
            }),
            initialValues: {
                customerId: '',
            },
            inputName: 'customerId',
            handleSubmit: (values) => {
                const { customerId, agentId } = accounts.find(
                    (agent) => agent.customerId === values.customerId
                )!;
                dispatch(saveAccount({ customerId, agentId }));
            },
        },
    ];

    return (
        <Stack py={{ xs: 2, sm: 3 }} spacing={{ xs: 2, sm: 3 }}>
            <Container>
                <Stack spacing={1}>
                    <Typography variant="h4">
                        {step === 2
                            ? t('auth.signInForm.selectAccount')
                            : t('auth.signInForm.signIn')}
                    </Typography>
                    <Typography variant="body1" color="text.secondary">
                        {step === 2
                            ? t('auth.signInForm.numberMultiAssociated')
                            : t('auth.signInForm.welcomeBackToVanoma')}
                    </Typography>
                </Stack>
            </Container>
            <Container>
                <Stack spacing={3}>
                    <Formik
                        initialValues={steps[step].initialValues}
                        validationSchema={steps[step].validationSchema}
                        onSubmit={(
                            values: SignInFormValues,
                            { resetForm }: FormikHelpers<SignInFormValues>
                        ) => steps[step].handleSubmit(values, resetForm)}
                        enableReinitialize
                    >
                        {(formikProps: FormikProps<SignInFormValues>) => (
                            <Form>
                                <Stack spacing={1}>
                                    {step === 2 ? (
                                        <AccountSelector
                                            formikProps={formikProps}
                                        />
                                    ) : (
                                        <AuthTextField
                                            formikProps={formikProps}
                                            steps={steps}
                                            step={step}
                                        />
                                    )}
                                </Stack>
                                {error && (
                                    <Alert
                                        severity="error"
                                        onClose={() => {
                                            dispatch(resetError());
                                        }}
                                        sx={{ mt: 2 }}
                                    >
                                        {error}
                                    </Alert>
                                )}
                                <Stack spacing={2} mt={3}>
                                    <Button type="submit" disabled={isLoading}>
                                        {isLoading ? (
                                            <LoadingIndicator />
                                        ) : (
                                            t(steps[step].body.buttonText)
                                        )}
                                    </Button>
                                </Stack>
                            </Form>
                        )}
                    </Formik>
                </Stack>
            </Container>
        </Stack>
    );
};

export default SignIn;
