/* eslint-disable no-nested-ternary */
import React, { ReactElement } from 'react';
import { Address, Contact } from '@vanoma/types';
import { useSelector } from 'react-redux';
import { useTypedDispatch } from '../../helpers/reduxToolkit';
import {
    updateFromContact,
    updateToContact,
    updateFromAddress,
    updateToAddress,
    getLastUsedToNote,
    selectIsLoading,
} from './slice';
import '../../locales/i18n';
import ContactsSelector from '../../components/ContactsSelector';
import AddressSelector from '../../components/AddressSelector';
import SelectedContact from '../../components/SelectedContact';

interface StopProps {
    packageId?: string;
    contact: Contact | null;
    address: Address | null;
    isEditing: boolean;
    resetDelivery?: () => void;
}

const Stop = ({
    packageId,
    contact,
    address,
    isEditing,
    resetDelivery,
}: StopProps): ReactElement => {
    const dispatch = useTypedDispatch();
    const isLoading = useSelector(selectIsLoading);

    const resetAddress = (): void => {
        dispatch(
            packageId === undefined
                ? updateFromAddress(null)
                : updateToAddress({ address: null, packageId })
        );
    };

    const resetContact = (): void => {
        dispatch(
            packageId === undefined
                ? updateFromContact(null)
                : updateToContact({
                      contact: null,
                      packageId,
                  })
        );
        resetAddress();
    };

    return (
        <>
            {contact && (
                <SelectedContact
                    isEditing={isEditing}
                    contact={contact}
                    address={address}
                    disabled={isLoading}
                    resetContact={resetContact}
                    resetAddress={resetAddress}
                />
            )}
            {!contact && (
                <ContactsSelector
                    onContactSelected={(value) =>
                        dispatch(
                            packageId === undefined
                                ? updateFromContact(value)
                                : updateToContact({
                                      contact: value,
                                      packageId,
                                  })
                        )
                    }
                />
            )}
            {contact && !address && (
                <AddressSelector
                    contact={contact}
                    onAddressSelected={(value) => {
                        if (packageId === undefined) {
                            dispatch(updateFromAddress(value));
                        } else {
                            dispatch(
                                updateToAddress({ address: value, packageId })
                            );
                            dispatch(getLastUsedToNote(packageId));
                        }
                    }}
                    resetDelivery={resetDelivery}
                    packageId={packageId}
                />
            )}
        </>
    );
};

export default Stop;
