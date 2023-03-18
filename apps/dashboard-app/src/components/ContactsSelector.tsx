import React, { ReactElement, useState, useEffect } from 'react';
import { styled } from '@mui/material/styles';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import { localizePhoneNumber, removeSpaces } from '@vanoma/helpers';
import { CustomModal } from '@vanoma/ui-components';
import { Box, Button } from '@mui/material';
import PersonOutlineOutlinedIcon from '@mui/icons-material/PersonOutlineOutlined';
import { Contact } from '@vanoma/types';
import Label from './Label';
import { useGetContactsQuery } from '../api';
import {
    selectContactId,
    selectCustomerId,
} from '../redux/slices/authenticationSlice';
import ContactListItemText from './ContactItem';
import ItemsSelector from './ItemsSelector';
import NewContact from './NewContact';
import CustomSnackBar from './CustomSnackBar';

const MePlaceHolder = styled('div')(() => ({
    width: 32.4,
}));

interface ContactsSelectorProps {
    // eslint-disable-next-line no-unused-vars
    onContactSelected: (value: Contact) => void;
}

const ContactsSelector = ({
    onContactSelected,
}: ContactsSelectorProps): ReactElement => {
    const defaultContactId = useSelector(selectContactId);
    const [filteredContacts, setFilteredContacts] = useState<Contact[]>([]);
    const [searchValue, setSearchValue] = useState('');
    const [openContact, setOpenContact] = React.useState(false);
    const { t } = useTranslation();
    const customerId = useSelector(selectCustomerId);
    const { data, error, isLoading, refetch } = useGetContactsQuery({
        customerId: customerId!,
    });

    const handleContactOpen = (): void => setOpenContact(true);
    const handleContactClose = (): void => setOpenContact(false);

    useEffect(() => {
        if (data) {
            setFilteredContacts(data.contacts);
        }
    }, [data]);

    const handleContactsSearch = (
        e: React.ChangeEvent<HTMLInputElement>
    ): void => {
        setSearchValue(e.target.value);
        if (data) {
            const { contacts } = data;
            const newFilteredContacts = contacts.filter(
                (contact) =>
                    contact.name
                        ?.toLowerCase()
                        .startsWith(e.target.value.toLowerCase()) ||
                    localizePhoneNumber(contact.phoneNumberOne).startsWith(
                        removeSpaces(e.target.value)
                    )
            );
            setFilteredContacts(newFilteredContacts);
        }
    };

    return (
        <Box sx={{ py: 2 }}>
            <ItemsSelector<Contact>
                data={filteredContacts}
                keyExtractor={({ contactId }) => contactId}
                renderItem={(contact) => (
                    <>
                        <ContactListItemText
                            contact={contact}
                            sx={{ width: '50%' }}
                        />
                        {contact.contactId === defaultContactId ? (
                            <Label color="primary">
                                {t('delivery.contactSelector.me')}
                            </Label>
                        ) : (
                            <MePlaceHolder />
                        )}
                        <Button size="small" variant="outlined" sx={{ ml: 1 }}>
                            {t('delivery.contactSelector.use')}
                        </Button>
                    </>
                )}
                sx={{ px: 1.87 }}
                inputLabel={`${t(
                    'delivery.contactSelector.searchContacts'
                )}...`}
                inputValue={searchValue}
                onInputChange={handleContactsSearch}
                onItemClick={onContactSelected}
                notFoundText={t('delivery.contactSelector.contactNotFound')}
                newItemButton={
                    <Button
                        size="small"
                        variant="outlined"
                        startIcon={<PersonOutlineOutlinedIcon />}
                        onClick={handleContactOpen}
                        sx={{ mt: 1, mb: 2 }}
                    >
                        {t('delivery.contactSelector.newContact')}
                    </Button>
                }
                isLoading={isLoading}
            />
            <CustomModal open={openContact} handleClose={handleContactClose}>
                <NewContact
                    handleClose={handleContactClose}
                    phoneNumberPreFill={searchValue}
                    onContactSelected={onContactSelected}
                />
            </CustomModal>
            <CustomSnackBar
                message={error as string}
                severity="error"
                onRetry={refetch}
            />
        </Box>
    );
};

export default ContactsSelector;
