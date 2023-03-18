/* eslint-disable no-nested-ternary */
import React, { ReactElement } from 'react';
import { FormikHelpers } from 'formik';
import { Button, FormHelperText, Typography } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import { LoadingIndicator } from '@vanoma/ui-components';
import { prefixNumberWithCountryCode } from '@vanoma/helpers';
import { Contact } from '@vanoma/types';
import { selectCustomerId } from '../redux/slices/authenticationSlice';
import { useCreateContactMutation, useUpdateContactMutation } from '../api';
import '../locales/i18n';
import NewContactForm from './NewContactForm';

interface NewContactFormValues {
    name: string;
    phoneNumber: string;
    // anotherPhoneNumber: string;
}
interface UpdateContactFormValues extends NewContactFormValues {
    contactId: string;
}

interface NewContactProps {
    // eslint-disable-next-line no-unused-vars
    onContactSelected?: (value: Contact) => void;
    handleClose: () => void;
    initialValues?: UpdateContactFormValues;
    phoneNumberPreFill: string;
    // eslint-disable-next-line no-unused-vars
    resetSearch?: () => void;
}

const NewContact = ({
    onContactSelected,
    handleClose,
    initialValues,
    phoneNumberPreFill,
    resetSearch,
}: NewContactProps): ReactElement => {
    const customerId = useSelector(selectCustomerId);
    const { t } = useTranslation();

    const [createContact, { isLoading: isCreateLoading, error: createError }] =
        useCreateContactMutation();

    const [updateContact, { isLoading: isUpdateLoading, error: updateError }] =
        useUpdateContactMutation();

    const handleSubmit = (
        values: NewContactFormValues,
        { resetForm }: FormikHelpers<NewContactFormValues>
    ): void => {
        if (initialValues) {
            updateContact({
                contactId: initialValues.contactId,
                name: values.name.trim() !== '' ? values.name.trim() : null,
                phoneNumberOne: prefixNumberWithCountryCode(values.phoneNumber),
            })
                .unwrap()
                .then(() => {
                    handleClose();
                    resetForm();
                });
        } else {
            if (resetSearch) {
                resetSearch();
            }

            createContact({
                customerId: customerId!,
                name: values.name.trim() !== '' ? values.name.trim() : null,
                phoneNumberOne: prefixNumberWithCountryCode(values.phoneNumber),
            })
                .unwrap()
                .then((contact) => {
                    if (onContactSelected) {
                        onContactSelected(contact);
                    }
                    handleClose();
                    resetForm();
                });
        }
    };

    return (
        <>
            <Typography variant="h5">
                {initialValues
                    ? t('customers.editContactModal.editContact')
                    : t('delivery.newContact.addNewContact')}
            </Typography>
            <NewContactForm
                initialValues={
                    initialValues || {
                        name: '',
                        phoneNumber: phoneNumberPreFill.match(/^\d+$/)
                            ? phoneNumberPreFill
                            : '',
                        // anotherPhoneNumber: '',
                    }
                }
                onSubmit={handleSubmit}
            >
                {() => (
                    <Button
                        type="submit"
                        sx={{ height: 40 }}
                        fullWidth
                        disabled={isCreateLoading || isUpdateLoading}
                    >
                        {isCreateLoading || isUpdateLoading ? (
                            <LoadingIndicator />
                        ) : initialValues ? (
                            t('delivery.newContact.save')
                        ) : (
                            t('delivery.newContact.add')
                        )}
                    </Button>
                )}
            </NewContactForm>
            {(createError || updateError) && (
                <FormHelperText sx={{ mt: 2 }} error>
                    {createError ?? updateError}
                </FormHelperText>
            )}
        </>
    );
};

export default NewContact;
