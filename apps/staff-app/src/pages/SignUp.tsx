import React, { FC, useState, useEffect } from 'react';
import {
    Alert,
    Button,
    Container,
    Stack,
    Step,
    StepIcon,
    StepLabel,
    Stepper,
    TextField,
    Typography,
    Box,
    stepIconClasses,
    InputAdornment,
    IconButton,
} from '@mui/material';
import { styled } from '@mui/material/styles';
import * as Yup from 'yup';
import { Form, Formik, FormikHelpers, FormikProps } from 'formik';
import { Link as RouterLink } from 'react-router-dom';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import { prefixNumberWithCountryCode } from '@vanoma/helpers';
import { useSelector } from 'react-redux';
import { LoadingIndicator } from '@vanoma/ui-components';
import {
    phoneNumberSchema,
    verificationCodeSchema,
} from '../helpers/yupSchema';
import { useTypedDispatch } from '../redux/typedHooks';
import {
    signUp,
    sendOtp,
    resetError,
    selectError,
    selectIsLoading,
    updateSignUpData,
    selectOtpData,
    selectSuccess,
} from '../redux/slices/authenticationSlice';
import { AppDispatch } from '../redux/store';

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

interface SignUpFormValues {
    phoneNumber?: string;
    verificationCode?: string;
    firstName?: string;
    lastName?: string;
    password?: string;
    confirmPassword?: string;
}

interface AuthStep<T> {
    label: string;
    body: { title: string; inputLabel: string; buttonText: string };
    validationSchema: Yup.SchemaOf<unknown>;
    initialValues: T;
    inputName: keyof T;
    handleSubmit: (
        // eslint-disable-next-line no-unused-vars
        values: T,
        // eslint-disable-next-line no-unused-vars
        dispatch: AppDispatch,
        // eslint-disable-next-line no-unused-vars
        callback: () => void
    ) => void;
}

const steps: AuthStep<SignUpFormValues>[] = [
    {
        label: 'Number',
        body: {
            title: 'Enter your phone number',
            inputLabel: 'Phone number',
            buttonText: 'Submit',
        },
        validationSchema: Yup.object().shape({
            phoneNumber: phoneNumberSchema(),
            checked: Yup.bool().oneOf(
                [true],
                'You have to accept the Terms of Use'
            ),
        }),
        initialValues: {
            phoneNumber: '',
        },
        inputName: 'phoneNumber',
        handleSubmit: (values, dispatch, callback) => {
            dispatch(
                sendOtp({
                    phoneNumber: prefixNumberWithCountryCode(
                        values.phoneNumber!
                    ),
                })
            )
                .unwrap()
                .then(() => callback());
        },
    },
    {
        label: 'Verify',
        body: {
            title: 'Enter verification code sent to ****',
            inputLabel: 'Verification code',
            buttonText: 'Verify',
        },
        validationSchema: Yup.object().shape({
            verificationCode: verificationCodeSchema(),
            firstName: Yup.string().required('Please enter first name'),
            lastName: Yup.string().required('Please enter last name'),
        }),
        initialValues: {
            verificationCode: '',
            firstName: '',
            lastName: '',
        },
        inputName: 'verificationCode',
        handleSubmit: (values, dispatch, callback) => {
            dispatch(
                updateSignUpData({
                    otpCode: values.verificationCode!,
                    firstName: values.firstName!,
                    lastName: values.lastName!,
                })
            );
            callback();
        },
    },
    {
        label: 'Complete',
        body: {
            title: 'Enter new password',
            inputLabel: 'Password',
            buttonText: 'Sign up',
        },
        validationSchema: Yup.object().shape({
            password: Yup.string()
                .required('Please enter new password')
                .matches(
                    /^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.{8,})/,
                    'Must contain 8 characters, at least one Uppercase, one Lowercase and one Number'
                ),
            confirmPassword: Yup.string().test(
                'passwords-match',
                'Passwords must match',
                // eslint-disable-next-line func-names
                function (value) {
                    return this.parent.password === value;
                }
            ),
        }),
        initialValues: {
            password: '',
            confirmPassword: '',
        },
        inputName: 'password',
        handleSubmit: (values, dispatch, callback) => {
            dispatch(
                signUp({
                    password: values.password!,
                })
            )
                .unwrap()
                .then(() => {
                    setTimeout(
                        () => window.location.assign('/auth/sign-in'),
                        3000
                    );
                    callback();
                });
        },
    },
];

const SignUp: FC = () => {
    const [step, setStep] = useState<number>(0);
    const [showPassword, setShowPassword] = useState(false);
    const isLoading = useSelector(selectIsLoading);
    const error = useSelector(selectError);
    const success = useSelector(selectSuccess);
    const otpData = useSelector(selectOtpData);
    const dispatch = useTypedDispatch();

    const handleClickShowPassword = (): void => {
        setShowPassword(!showPassword);
    };

    const handleMouseDownPassword = (
        event: React.MouseEvent<HTMLButtonElement>
    ): void => {
        event.preventDefault();
    };

    useEffect(() => {
        dispatch(resetError());
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const HideOrShowPassword = (): JSX.Element => (
        <InputAdornment position="start">
            <IconButton
                onClick={handleClickShowPassword}
                onMouseDown={handleMouseDownPassword}
                edge="end"
                sx={{
                    p: 0.7,
                }}
            >
                {showPassword ? <VisibilityOff /> : <Visibility />}
            </IconButton>
        </InputAdornment>
    );

    return (
        <Stack py={{ xs: 2, sm: 3 }} spacing={{ xs: 2, sm: 3 }}>
            <Container>
                <Stack spacing={1}>
                    <Typography variant="h4">Sign up</Typography>
                </Stack>
            </Container>
            <StepContainer>
                <Stepper activeStep={step} alternativeLabel>
                    {steps.map(({ label }, index) => (
                        <Step key={label} completed={index < step}>
                            <StepLabel StepIconComponent={StepIconComponent}>
                                {label}
                            </StepLabel>
                        </Step>
                    ))}
                </Stepper>
            </StepContainer>
            <Container>
                <Stack spacing={3}>
                    <Formik
                        initialValues={steps[step].initialValues}
                        validationSchema={steps[step].validationSchema}
                        onSubmit={(
                            values: SignUpFormValues,
                            { resetForm }: FormikHelpers<SignUpFormValues>
                        ) => {
                            const callback = (): void => {
                                resetForm();
                                if (step < steps.length - 1) setStep(step + 1);
                            };

                            steps[step].handleSubmit(
                                values,
                                dispatch,
                                callback
                            );
                        }}
                        enableReinitialize
                    >
                        {({
                            values,
                            handleChange,
                            errors,
                            touched,
                        }: FormikProps<SignUpFormValues>) => (
                            <Form>
                                <Stack spacing={1}>
                                    <Typography variant="body1">
                                        {`${steps[step].body.title}${
                                            step === 1
                                                ? otpData?.phoneNumber.substring(
                                                      7,
                                                      10
                                                  )
                                                : ''
                                        }`}
                                    </Typography>
                                    <TextField
                                        name={steps[step].inputName}
                                        label={steps[step].body.inputLabel}
                                        value={
                                            values[steps[step].inputName] ?? ''
                                        }
                                        onChange={handleChange}
                                        error={
                                            !!(
                                                errors[steps[step].inputName] &&
                                                touched[steps[step].inputName]
                                            )
                                        }
                                        helperText={
                                            (touched[steps[step].inputName] &&
                                                errors[
                                                    steps[step].inputName
                                                ]) ??
                                            undefined
                                        }
                                        InputProps={
                                            step === 2
                                                ? {
                                                      endAdornment: (
                                                          <HideOrShowPassword />
                                                      ),
                                                  }
                                                : {}
                                        }
                                        type={
                                            step === 2 && !showPassword
                                                ? 'password'
                                                : 'text'
                                        }
                                    />

                                    {step === 1 &&
                                        !errors[steps[step].inputName] &&
                                        values[steps[step].inputName] !==
                                            '' && (
                                            <>
                                                <Typography variant="body1">
                                                    Enter the first name
                                                </Typography>
                                                <TextField
                                                    name="firstName"
                                                    label="First name"
                                                    value={
                                                        values.firstName ?? ''
                                                    }
                                                    onChange={handleChange}
                                                    error={
                                                        !!(
                                                            errors.firstName &&
                                                            touched.firstName
                                                        )
                                                    }
                                                    helperText={
                                                        (touched.firstName &&
                                                            errors.firstName) ??
                                                        undefined
                                                    }
                                                />
                                                <Typography variant="body1">
                                                    Enter the last name
                                                </Typography>
                                                <TextField
                                                    name="lastName"
                                                    label="Last name"
                                                    value={
                                                        values.lastName ?? ''
                                                    }
                                                    onChange={handleChange}
                                                    error={
                                                        !!(
                                                            errors.lastName &&
                                                            touched.lastName
                                                        )
                                                    }
                                                    helperText={
                                                        (touched.lastName &&
                                                            errors.lastName) ??
                                                        undefined
                                                    }
                                                />
                                            </>
                                        )}
                                    {step === 2 && (
                                        <>
                                            <Typography variant="body1">
                                                Confirm new password
                                            </Typography>
                                            <TextField
                                                name="confirmPassword"
                                                label="Password"
                                                type={
                                                    showPassword
                                                        ? 'text'
                                                        : 'password'
                                                }
                                                value={
                                                    values.confirmPassword ?? ''
                                                }
                                                onChange={handleChange}
                                                error={
                                                    !!(
                                                        errors.confirmPassword &&
                                                        touched.confirmPassword
                                                    )
                                                }
                                                helperText={
                                                    (touched.confirmPassword &&
                                                        errors.confirmPassword) ??
                                                    undefined
                                                }
                                                InputProps={{
                                                    endAdornment: (
                                                        <HideOrShowPassword />
                                                    ),
                                                }}
                                            />
                                        </>
                                    )}
                                </Stack>
                                {success && (
                                    <Alert severity="success" sx={{ mt: 2 }}>
                                        {success}
                                    </Alert>
                                )}
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
                                <Stack spacing={2} mt={2}>
                                    <Button type="submit" disabled={isLoading}>
                                        {isLoading ? (
                                            <LoadingIndicator />
                                        ) : (
                                            steps[step].body.buttonText
                                        )}
                                    </Button>
                                    <Typography variant="body1">
                                        Already have an account?{' '}
                                        <Box
                                            component={RouterLink}
                                            to="/auth/sign-in"
                                        >
                                            Sign in here
                                        </Box>
                                    </Typography>
                                </Stack>
                            </Form>
                        )}
                    </Formik>
                </Stack>
            </Container>
        </Stack>
    );
};

export default SignUp;
