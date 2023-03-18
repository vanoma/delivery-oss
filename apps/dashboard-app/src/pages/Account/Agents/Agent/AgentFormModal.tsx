/* eslint-disable no-nested-ternary */
import React from 'react';
import { Form, Formik, FormikHelpers, FormikProps } from 'formik';
import * as Yup from 'yup';
import {
    Button,
    FormHelperText,
    Stack,
    TextField,
    Typography,
} from '@mui/material';
import { CustomModal, LoadingIndicator } from '@vanoma/ui-components';
import { useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { prefixNumberWithCountryCode, removeSpaces } from '@vanoma/helpers';
import { Agent } from '@vanoma/types';
import {
    useCreateAgentMutation,
    useUpdateAgentMutation,
} from '../../../../api';
import {
    selectBranches,
    selectCustomerId,
} from '../../../../redux/slices/authenticationSlice';
import { phoneNumberSchema } from '../../../../helpers/yupSchema';
import BranchSelector from '../../../../components/BranchSelector';

interface NewAgentFormValues {
    fullName: string;
    phoneNumber: string;
    branchId: string;
}

const AgentFormModal: React.FC<{
    handleClose: () => void;
    agent?: Agent;
    open: boolean;
}> = ({ handleClose, agent, open }) => {
    const { t } = useTranslation();
    const customerId = useSelector(selectCustomerId);
    const branches = useSelector(selectBranches);

    const agentValidationSchema = Yup.object().shape({
        fullName: Yup.string().required(
            t('alertAndValidationMessages.agentNameRequired')
        ),
        phoneNumber: phoneNumberSchema(t, undefined, true),
    });
    const agentWithBranchValidationSchema = Yup.object().shape({
        fullName: Yup.string().required(
            t('alertAndValidationMessages.agentNameRequired')
        ),
        phoneNumber: phoneNumberSchema(t, undefined, true),
        branchId: Yup.string().required(
            t('alertAndValidationMessages.branchRequired')
        ),
    });

    const [
        createAgent,
        { isLoading: isLoadingCreateAgent, error: createError },
    ] = useCreateAgentMutation();

    const [
        updateAgent,
        { isLoading: isLoadingUpdateAgent, error: updateError },
    ] = useUpdateAgentMutation();

    const handleSubmit = (
        values: NewAgentFormValues,
        { resetForm }: FormikHelpers<NewAgentFormValues>
    ): void => {
        if (agent) {
            updateAgent({
                agentId: agent.agentId,
                fullName: values.fullName,
                branchId: values.branchId ? values.branchId : null,
            })
                .unwrap()
                .then(() => {
                    resetForm();
                    handleClose();
                });
        } else {
            createAgent({
                customerId: customerId!,
                phoneNumber: prefixNumberWithCountryCode(values.phoneNumber),
                branchId: values.branchId ? values.branchId : null,
                fullName: values.fullName,
            })
                .unwrap()
                .then(() => {
                    resetForm();
                    handleClose();
                });
        }
    };

    return (
        <CustomModal open={open} handleClose={handleClose} sx={{ width: 500 }}>
            <Formik
                initialValues={{
                    fullName: agent?.fullName ?? '',
                    phoneNumber: agent?.phoneNumber ?? '',
                    branchId: agent?.branch?.branchId ?? '',
                }}
                validationSchema={
                    branches.length > 0
                        ? agentWithBranchValidationSchema
                        : agentValidationSchema
                }
                onSubmit={handleSubmit}
            >
                {(formikProps: FormikProps<NewAgentFormValues>) => {
                    const {
                        values,
                        handleChange,
                        setFieldValue,
                        errors,
                        touched,
                    } = formikProps;
                    return (
                        <Form>
                            <Stack spacing={2}>
                                <Typography variant="h5" mb={2}>
                                    {agent
                                        ? t('account.newAgentModal.editAgent')
                                        : t(
                                              'account.newAgentModal.addNewAgent'
                                          )}
                                </Typography>
                                <TextField
                                    label={t('account.newAgentModal.fullName')}
                                    name="fullName"
                                    value={values.fullName}
                                    onChange={handleChange}
                                    error={
                                        !!(errors.fullName && touched.fullName)
                                    }
                                    helperText={
                                        (touched.fullName && errors.fullName) ??
                                        undefined
                                    }
                                    size="small"
                                    fullWidth
                                />
                                {!agent && (
                                    <TextField
                                        label={t(
                                            'account.newAgentModal.phoneNumber'
                                        )}
                                        name="phoneNumber"
                                        value={values.phoneNumber}
                                        onChange={(e) => {
                                            setFieldValue(
                                                'phoneNumber',
                                                removeSpaces(e.target.value)
                                            );
                                        }}
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
                                        size="small"
                                        fullWidth
                                        disabled={agent !== undefined}
                                    />
                                )}
                                <BranchSelector
                                    value={values.branchId}
                                    onChange={(value) =>
                                        setFieldValue('branchId', value)
                                    }
                                    touched={touched.branchId}
                                    error={errors.branchId}
                                />
                                <Button
                                    type="submit"
                                    size="medium"
                                    disabled={
                                        isLoadingCreateAgent ||
                                        isLoadingUpdateAgent
                                    }
                                    fullWidth
                                >
                                    {isLoadingCreateAgent ||
                                    isLoadingUpdateAgent ? (
                                        <LoadingIndicator />
                                    ) : agent ? (
                                        t('account.newAgentModal.edit')
                                    ) : (
                                        t('account.newAgentModal.add')
                                    )}
                                </Button>
                                {(createError || updateError) && (
                                    <FormHelperText error>
                                        {createError || updateError}
                                    </FormHelperText>
                                )}
                            </Stack>
                        </Form>
                    );
                }}
            </Formik>
        </CustomModal>
    );
};

export default AgentFormModal;
