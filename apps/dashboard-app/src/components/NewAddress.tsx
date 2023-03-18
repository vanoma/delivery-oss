import React, { ReactElement } from 'react';
import {
    Button,
    FormHelperText,
    Stack,
    TextField,
    Typography,
} from '@mui/material';
import { Formik, Form, FormikProps } from 'formik';
import * as Yup from 'yup';
import { useTranslation } from 'react-i18next';
import {
    LoadingIndicator,
    MapBasePosition,
    MapBaseSuggestion,
} from '@vanoma/ui-components';
import { Address, ReverseGeocode } from '@vanoma/types';
import { useCreateAddressMutation } from '../api';

export interface SelectedLocation {
    position: MapBasePosition;
    address?: Address;
    suggestion?: MapBaseSuggestion;
}

interface NewAddressFormValues {
    addressName: string;
    houseNumber: string;
}

interface NewAddressProps {
    contactId: string;
    reverseGeocode: ReverseGeocode;
    selectedLocation: SelectedLocation;
    // eslint-disable-next-line no-unused-vars
    onAddressSelected: (value: Address) => void;
    // Only to be used when creating default address on sign up
    isDefault?: boolean;
}

const NewAddress = ({
    contactId,
    reverseGeocode,
    selectedLocation,
    onAddressSelected,
    isDefault = false,
}: NewAddressProps): ReactElement => {
    const { t } = useTranslation();
    const [createAddress, { isLoading, error }] = useCreateAddressMutation();

    const addressValidationSchema: Yup.SchemaOf<
        Pick<NewAddressFormValues, 'addressName'>
    > = Yup.object().shape({
        houseNumber: Yup.string().test(
            'len',
            t('alertAndValidationMessages.houseNumberNotValid'),
            (value) => value === undefined || value.length < 10
        ),
        addressName: Yup.string().required(
            t('alertAndValidationMessages.addressNameRequired')
        ),
    });

    const handleAddressSave = (values: NewAddressFormValues): void => {
        const { position } = selectedLocation;
        createAddress({
            ...reverseGeocode,
            ...values,
            contactId,
            latitude: position.lat,
            longitude: position.lng,
            isDefault,
        })
            .unwrap()
            .then(onAddressSelected);
    };

    return (
        <Formik
            initialValues={{
                addressName: selectedLocation.suggestion?.description ?? '',
                houseNumber: reverseGeocode.houseNumber ?? '',
            }}
            validationSchema={addressValidationSchema}
            onSubmit={handleAddressSave}
            enableReinitialize
        >
            {({
                values,
                handleChange,
                errors,
                touched,
            }: FormikProps<NewAddressFormValues>) => (
                <Form>
                    <Typography id="transition-modal-title" variant="h5">
                        {t('delivery.addressSelector.saveAddressForFutureUse')}
                    </Typography>
                    <Stack spacing={2} sx={{ alignItems: 'center', mt: 2 }}>
                        {!reverseGeocode.houseNumber && (
                            <TextField
                                label={t('delivery.newAddress.houseNumber')}
                                name="houseNumber"
                                value={values.houseNumber}
                                onChange={handleChange}
                                error={
                                    !!(
                                        errors.houseNumber &&
                                        touched.houseNumber
                                    )
                                }
                                helperText={
                                    (touched.houseNumber &&
                                        errors.houseNumber) ??
                                    undefined
                                }
                                size="small"
                                fullWidth
                                sx={{ flexGrow: 0.3 }}
                            />
                        )}
                        <TextField
                            label={t('delivery.newAddress.addressName')}
                            name="addressName"
                            value={values.addressName}
                            onChange={handleChange}
                            error={
                                !!(errors.addressName && touched.addressName)
                            }
                            helperText={
                                (touched.addressName && errors.addressName) ??
                                undefined
                            }
                            size="small"
                            fullWidth
                            sx={{ flexGrow: 0.3 }}
                        />
                        <Button
                            type="submit"
                            sx={{ height: 40 }}
                            fullWidth
                            disabled={isLoading}
                        >
                            {isLoading ? (
                                <LoadingIndicator />
                            ) : (
                                t('delivery.newAddress.save')
                            )}
                        </Button>
                    </Stack>
                    {error && (
                        <FormHelperText sx={{ mt: 2 }} error>
                            {error}
                        </FormHelperText>
                    )}
                </Form>
            )}
        </Formik>
    );
};

export default NewAddress;
