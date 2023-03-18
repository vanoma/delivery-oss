import React from 'react';
import {
    Button,
    FormHelperText,
    Stack,
    TextField,
    Typography,
} from '@mui/material';
import { Formik, Form, FormikProps } from 'formik';
import { CustomModal, LoadingIndicator } from '@vanoma/ui-components';
import { SerializedError } from '@reduxjs/toolkit';

interface Props {
    open: boolean;
    title: string;
    inputLabel: string;
    inputName: string;
    initialValue?: string;
    buttonText: string;
    isLoading: boolean;
    error?: string | SerializedError;
    handleClose: () => void;
    // eslint-disable-next-line no-unused-vars
    handleSubmit: (value: string) => void;
    validationSchema?: any;
}

const TextFieldModal: React.FC<Props> = ({
    open,
    title,
    inputLabel,
    inputName,
    initialValue,
    buttonText,
    isLoading,
    error,
    handleClose,
    handleSubmit,
    validationSchema,
}) => {
    return (
        <CustomModal open={open} handleClose={handleClose}>
            <Formik
                initialValues={{ [inputName]: initialValue ?? '' }}
                validationSchema={validationSchema}
                onSubmit={(values) => handleSubmit(values[inputName])}
            >
                {({
                    values,
                    handleChange,
                    errors,
                    touched,
                }: FormikProps<{ [inputName: string]: string }>) => (
                    <Form>
                        <Typography variant="h5">{title}</Typography>
                        <Stack spacing={2} sx={{ alignItems: 'center', mt: 3 }}>
                            <TextField
                                label={inputLabel}
                                name={inputName}
                                value={values[inputName]}
                                onChange={handleChange}
                                error={
                                    !!(errors[inputName] && touched[inputName])
                                }
                                helperText={
                                    (touched[inputName] && errors[inputName]) ??
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
                                {isLoading ? <LoadingIndicator /> : buttonText}
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

export default TextFieldModal;
