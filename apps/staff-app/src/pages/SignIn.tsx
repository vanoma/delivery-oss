import React, { FC, useEffect, useState } from 'react';
import {
    Button,
    Container,
    Stack,
    TextField,
    Typography,
    Box,
    Alert,
    InputAdornment,
    IconButton,
} from '@mui/material';
import * as Yup from 'yup';
import { Form, Formik, FormikProps } from 'formik';
import { Link as RouterLink } from 'react-router-dom';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import { useSelector } from 'react-redux';
import { LoadingIndicator } from '@vanoma/ui-components';
import { prefixNumberWithCountryCode } from '@vanoma/helpers';
import { phoneNumberSchema } from '../helpers/yupSchema';
import { useTypedDispatch } from '../redux/typedHooks';
import {
    resetError,
    selectError,
    selectIsLoading,
    signIn,
} from '../redux/slices/authenticationSlice';

interface SignInFormValues {
    phoneNumber: string;
    password: string;
}

const SignIn: FC = () => {
    const dispatch = useTypedDispatch();
    const isLoading = useSelector(selectIsLoading);
    const error = useSelector(selectError);
    const [showPassword, setShowPassword] = useState(false);

    useEffect(() => {
        dispatch(resetError());
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const signInValidationSchema: Yup.SchemaOf<SignInFormValues> =
        Yup.object().shape({
            phoneNumber: phoneNumberSchema(),
            password: Yup.string().required('Please enter your password'),
        });

    const handleClickShowPassword = (): void => {
        setShowPassword(!showPassword);
    };

    const handleMouseDownPassword = (
        event: React.MouseEvent<HTMLButtonElement>
    ): void => {
        event.preventDefault();
    };

    return (
        <Stack py={{ xs: 2, sm: 3 }} spacing={{ xs: 2, sm: 3 }}>
            <Container>
                <Stack spacing={1}>
                    <Typography variant="h4">Sign in</Typography>
                    <Typography variant="body1" color="text.secondary">
                        Welcome back to Vanoma!
                    </Typography>
                </Stack>
            </Container>
            <Container>
                <Stack spacing={3}>
                    <Formik
                        initialValues={{
                            phoneNumber: '',
                            password: '',
                        }}
                        validationSchema={signInValidationSchema}
                        onSubmit={(values: SignInFormValues) => {
                            dispatch(resetError());
                            dispatch(
                                signIn({
                                    ...values,
                                    phoneNumber: prefixNumberWithCountryCode(
                                        values.phoneNumber
                                    ),
                                })
                            );
                        }}
                        enableReinitialize
                    >
                        {({
                            values,
                            handleChange,
                            errors,
                            touched,
                        }: FormikProps<SignInFormValues>) => (
                            <Form>
                                <Stack spacing={3}>
                                    <TextField
                                        name="phoneNumber"
                                        label="Phone number"
                                        value={values.phoneNumber}
                                        onChange={handleChange}
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
                                    />
                                    <TextField
                                        name="password"
                                        label="Password"
                                        type={
                                            showPassword ? 'text' : 'password'
                                        }
                                        value={values.password}
                                        onChange={handleChange}
                                        error={
                                            !!(
                                                errors.password &&
                                                touched.password
                                            )
                                        }
                                        helperText={
                                            (touched.password &&
                                                errors.password) ??
                                            undefined
                                        }
                                        InputProps={{
                                            endAdornment: (
                                                <InputAdornment position="start">
                                                    <IconButton
                                                        onClick={
                                                            handleClickShowPassword
                                                        }
                                                        onMouseDown={
                                                            handleMouseDownPassword
                                                        }
                                                        edge="end"
                                                        sx={{ p: 0.7 }}
                                                    >
                                                        {showPassword ? (
                                                            <VisibilityOff />
                                                        ) : (
                                                            <Visibility />
                                                        )}
                                                    </IconButton>
                                                </InputAdornment>
                                            ),
                                        }}
                                    />
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
                                            'Sign in'
                                        )}
                                    </Button>
                                    <Typography variant="body1">
                                        Don&apos;t have an account?{' '}
                                        <Box
                                            component={RouterLink}
                                            to="/auth/sign-up"
                                        >
                                            Sign up here
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

export default SignIn;
