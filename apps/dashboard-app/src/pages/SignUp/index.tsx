import React, { FC, useState, useEffect } from 'react';
import {
    Container,
    Stack,
    Step,
    StepIcon,
    StepLabel,
    Stepper,
    Typography,
    stepIconClasses,
} from '@mui/material';
import { styled } from '@mui/material/styles';
import * as Yup from 'yup';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import { prefixNumberWithCountryCode } from '@vanoma/helpers';
import {
    verificationCodeSchema,
    phoneNumberSchema,
} from '../../helpers/yupSchema';
import { useTypedDispatch } from '../../helpers/reduxToolkit';
import {
    signUp,
    sendOtp,
    resetError,
    selectError,
    selectOtpData,
    selectIsLoading,
} from '../../redux/slices/authenticationSlice';
import i18n from '../../locales/i18n';
import SignUpForm from './SignUpForm';
import { SignUpFormValues } from './types';
import { AuthStep } from '../../types';

const StepContainer = styled('div')(({ theme }) => ({
    backgroundColor: theme.palette.primary.light,
    padding: `${theme.spacing(2)} 0`,
}));
const StepIconComponent = styled(StepIcon)(({ theme }) => ({
    border: `${theme.palette.background.default} 5px solid`,
    borderRadius: theme.shape.borderRadius,
    width: 34,
    height: 34,
    [`&.${stepIconClasses.completed}`]: {
        color: theme.palette.success.main,
    },
}));

const SignUp: FC = () => {
    const [step, setStep] = useState<number>(0);
    const otpData = useSelector(selectOtpData);
    const isLoading = useSelector(selectIsLoading);
    const error = useSelector(selectError);

    const dispatch = useTypedDispatch();
    const { t } = useTranslation();

    useEffect(() => {
        dispatch(resetError());
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const steps: AuthStep<SignUpFormValues>[] = [
        {
            label: 'auth.signUpForm.phoneNumber',
            body: {
                title: 'auth.signUpForm.enterYourBusinessPhoneNumber',
                inputLabel: 'auth.signUpForm.phoneNumber',
                buttonText: 'auth.signUpForm.submit',
            },
            validationSchema: Yup.object().shape({
                phoneNumber: phoneNumberSchema(t),
                checked: Yup.bool().oneOf(
                    [true],
                    i18n.t('alertAndValidationMessages.termsOfUseRequired')
                ),
            }),
            initialValues: {
                phoneNumber: '',
                checked: false,
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
            label: 'auth.signUpForm.verificationCode',
            body: {
                title: 'auth.signUpForm.enterVerificationCodeSentTo',
                inputLabel: 'auth.signUpForm.verificationCode',
                buttonText: 'auth.signUpForm.verify',
            },
            validationSchema: Yup.object().shape({
                verificationCode: verificationCodeSchema(t),
                businessName: Yup.string().required(
                    i18n.t('alertAndValidationMessages.businessNameRequired')
                ),
            }),
            initialValues: {
                verificationCode: '',
                businessName: '',
            },
            inputName: 'verificationCode',
            handleSubmit: (values) => {
                dispatch(
                    signUp({
                        phoneNumber: otpData!.phoneNumber,
                        otpId: otpData!.otpId,
                        otpCode: values.verificationCode!,
                        businessName: values.businessName!,
                    })
                );
            },
        },
    ];

    return (
        <Stack py={{ xs: 2, sm: 3 }} spacing={{ xs: 2, sm: 3 }}>
            <Container>
                <Stack spacing={1}>
                    <Typography variant="h4">
                        {t('auth.signUpForm.signUp')}
                    </Typography>
                </Stack>
            </Container>
            <StepContainer>
                <Stepper activeStep={step} alternativeLabel>
                    {steps.map(({ label }) => (
                        <Step key={label}>
                            <StepLabel StepIconComponent={StepIconComponent}>
                                {t(label)}
                            </StepLabel>
                        </Step>
                    ))}
                </Stepper>
            </StepContainer>
            <SignUpForm
                steps={steps}
                step={step}
                isLoading={isLoading}
                errorMessage={error}
                phoneNumber={otpData?.phoneNumber}
                resetError={() => dispatch(resetError())}
            />
        </Stack>
    );
};

export default SignUp;
