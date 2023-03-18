import { TextField } from '@mui/material';
import { FormikProps } from 'formik';
import React from 'react';
import { useTranslation } from 'react-i18next';
import { removeSpaces } from '@vanoma/helpers';
import { AuthStep } from '../types';

interface FormValues {
    phoneNumber?: string;
    verificationCode?: string;
}

const CustomTextField: React.FC<{
    formikProps: FormikProps<FormValues>;
    steps: AuthStep<FormValues>[];
    step: number;
}> = ({ formikProps, steps, step }) => {
    const { values, errors, touched, handleChange, setFieldValue } =
        formikProps;
    const { t } = useTranslation();

    return (
        <TextField
            name={steps[step].inputName}
            label={t(steps[step].body.inputLabel)}
            value={values[steps[step].inputName] ?? ''}
            onChange={
                step === 0
                    ? (e) => {
                          setFieldValue(
                              'phoneNumber',
                              removeSpaces(e.target.value)
                          );
                      }
                    : handleChange
            }
            error={
                !!(
                    errors[steps[step].inputName] &&
                    touched[steps[step].inputName]
                )
            }
            helperText={
                (touched[steps[step].inputName] &&
                    errors[steps[step].inputName]) ??
                undefined
            }
        />
    );
};

export default CustomTextField;
