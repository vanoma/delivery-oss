/* eslint-disable no-nested-ternary */
import {
    Box,
    Button,
    FormHelperText,
    Stack,
    TextField,
    Typography,
} from '@mui/material';
import { CustomModal, LoadingIndicator } from '@vanoma/ui-components';
import { Form, Formik, FormikHelpers, FormikProps } from 'formik';
import React, { useState } from 'react';
import * as Yup from 'yup';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import { Address, Branch, Contact } from '@vanoma/types';
import {
    useCreateBranchMutation,
    useUpdateBranchMutation,
} from '../../../../api';
import { selectCustomerId } from '../../../../redux/slices/authenticationSlice';
import ContactsSelector from '../../../../components/ContactsSelector';
import AddressSelector from '../../../../components/AddressSelector';
import SelectedContact from '../../../../components/SelectedContact';

interface NewBranchFormValues {
    branchName: string;
    contactId: string;
    addressId: string;
}

const BranchFormModal: React.FC<{
    handleClose: () => void;
    branch?: Branch;
    open: boolean;
}> = ({ handleClose, branch, open }) => {
    const { t } = useTranslation();
    const [contact, setContact] = useState<Contact | null>(
        branch ? branch.contact : null
    );
    const [address, setAddress] = useState<Address | null>(
        branch ? branch.address : null
    );
    const [
        createBranch,
        { isLoading: isLoadingCreateBranch, error: createError },
    ] = useCreateBranchMutation();
    const [
        updateBranch,
        { isLoading: isLoadingUpdateBranch, error: updateError },
    ] = useUpdateBranchMutation();

    const customerId = useSelector(selectCustomerId);

    const branchValidationSchema = Yup.object().shape({
        branchName: Yup.string().required(
            t('alertAndValidationMessages.branchNameRequired')
        ),
        contactId: Yup.string().required(
            t('alertAndValidationMessages.branchContactRequired')
        ),
        addressId: Yup.string().required(
            t('alertAndValidationMessages.branchAddressRequired')
        ),
    });

    const handleSubmit = (
        values: NewBranchFormValues,
        { resetForm }: FormikHelpers<NewBranchFormValues>
    ): void => {
        if (branch) {
            updateBranch({
                branchId: branch.branchId,
                branchName: values.branchName,
                contactId: values.contactId,
                addressId: values.addressId,
            })
                .unwrap()
                .then(() => {
                    resetForm();
                    handleClose();
                });
        } else {
            createBranch({
                customerId: customerId!,
                ...values,
            })
                .unwrap()
                .then(() => {
                    resetForm();
                    handleClose();
                });
        }
    };

    const isLoading = isLoadingCreateBranch || isLoadingUpdateBranch;

    return (
        <CustomModal
            open={open}
            handleClose={handleClose}
            sx={{ p: 0, width: 700 }}
        >
            <Formik
                initialValues={{
                    branchName: branch?.branchName ?? '',
                    contactId: branch?.contact.contactId ?? '',
                    addressId: branch?.address.addressId ?? '',
                }}
                validationSchema={branchValidationSchema}
                onSubmit={handleSubmit}
            >
                {(formikProps: FormikProps<NewBranchFormValues>) => {
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
                                <Box px={2} pt={2}>
                                    <Typography variant="h5" mb={2}>
                                        {branch
                                            ? t(
                                                  'account.newBranchModal.editBranch'
                                              )
                                            : t(
                                                  'account.newBranchModal.addNewBranch'
                                              )}
                                    </Typography>
                                    <TextField
                                        label={t(
                                            'account.newBranchModal.branchName'
                                        )}
                                        name="branchName"
                                        value={values.branchName}
                                        onChange={handleChange}
                                        error={
                                            !!(
                                                errors.branchName &&
                                                touched.branchName
                                            )
                                        }
                                        helperText={
                                            (touched.branchName &&
                                                errors.branchName) ??
                                            undefined
                                        }
                                        size="small"
                                        fullWidth
                                    />
                                </Box>
                                {contact && (
                                    <SelectedContact
                                        contact={contact}
                                        address={address}
                                        resetContact={() => {
                                            setFieldValue('contactId', '');
                                            setFieldValue('addressId', '');
                                            setContact(null);
                                            setAddress(null);
                                        }}
                                        resetAddress={() => {
                                            setFieldValue('addressId', '');
                                            setAddress(null);
                                        }}
                                        disabled={isLoading}
                                        isEditing
                                    />
                                )}
                                {!contact && (
                                    <Box>
                                        <ContactsSelector
                                            onContactSelected={(value) => {
                                                setFieldValue(
                                                    'contactId',
                                                    value.contactId
                                                );
                                                setContact(value);
                                            }}
                                        />
                                        {touched.contactId && errors.contactId && (
                                            <FormHelperText
                                                sx={{ mt: 2, ml: 2 }}
                                                error
                                            >
                                                {errors.contactId}
                                            </FormHelperText>
                                        )}
                                    </Box>
                                )}
                                {contact && !values.addressId && (
                                    <Box>
                                        <AddressSelector
                                            contact={contact}
                                            onAddressSelected={(value) => {
                                                setFieldValue(
                                                    'addressId',
                                                    value.addressId
                                                );
                                                setAddress(value);
                                            }}
                                        />
                                        {touched.addressId && errors.addressId && (
                                            <FormHelperText
                                                sx={{ mt: 2, ml: 2 }}
                                                error
                                            >
                                                {errors.addressId}
                                            </FormHelperText>
                                        )}
                                    </Box>
                                )}
                                {values.addressId && (
                                    <Box px={2} pb={2}>
                                        <Button
                                            type="submit"
                                            size="medium"
                                            disabled={isLoading}
                                            fullWidth
                                        >
                                            {isLoading ? (
                                                <LoadingIndicator />
                                            ) : branch ? (
                                                t('account.newBranchModal.edit')
                                            ) : (
                                                t('account.newBranchModal.add')
                                            )}
                                        </Button>
                                        {(createError || updateError) && (
                                            <FormHelperText
                                                sx={{ mt: 2 }}
                                                error
                                            >
                                                {createError || updateError}
                                            </FormHelperText>
                                        )}
                                    </Box>
                                )}
                            </Stack>
                        </Form>
                    );
                }}
            </Formik>
        </CustomModal>
    );
};

export default BranchFormModal;
