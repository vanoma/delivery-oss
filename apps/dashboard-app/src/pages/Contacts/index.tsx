import React, { ReactElement, useEffect, useState } from 'react';
import {
    Container,
    Typography,
    Box,
    Button,
    TextField,
    Pagination,
    Skeleton,
    Card,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import { CustomModal } from '@vanoma/ui-components';
import { localizePhoneNumber } from '@vanoma/helpers';
import PersonOutlineOutlinedIcon from '@mui/icons-material/PersonOutlineOutlined';
import { Contact } from '@vanoma/types';
import NewContact from '../../components/NewContact';
import ContactView from './Contact';
import '../../locales/i18n';
import DeleteAddressModal from '../../components/DeleteAddressModal';
import DeleteContactModal from '../../components/DeleteContactModal';
import {
    useDeleteAddressMutation,
    useDeleteContactMutation,
    useGetContactsQuery,
} from '../../api';
import { selectCustomerId } from '../../redux/slices/authenticationSlice';
import CustomSnackBar from '../../components/CustomSnackBar';

const limit = 10;

const Contacts = (): ReactElement => {
    const customerId = useSelector(selectCustomerId);
    const { data, isFetching, error, refetch } = useGetContactsQuery({
        customerId: customerId!,
    });
    const [
        deleteContact,
        { isLoading: isDeleteContactLoading, error: deleteContactError },
    ] = useDeleteContactMutation();
    const [
        deleteAddress,
        { isLoading: isDeleteAddressLoading, error: deleteAddressError },
    ] = useDeleteAddressMutation();

    const { t } = useTranslation();
    const [openContact, setOpenContact] = React.useState(false);
    const [openDeleteAddress, setOpenDeleteAddress] = React.useState(false);
    const [openDeleteContact, setOpenDeleteContact] = React.useState<{
        open: boolean;
        contactId: string | null;
    }>({
        open: false,
        contactId: null,
    });
    const [selected, setSelected] = useState<Contact | null>(null);
    const [addressToBeDeleted, setAddressToBeDeleted] = useState<{
        contactId: string;
        addressId: string;
    } | null>(null);
    const [filteredContacts, setFilteredContacts] = useState<Contact[]>([]);
    const [filteredTotalCount, setFilteredTotalCount] = useState(0);
    const [searchValue, setSearchValue] = useState('');
    const [currentPage, setCurrentPage] = useState(1);

    const selectContact = (contactId: string): void => {
        setSelected(
            data!.contacts.find((contact) => contact.contactId === contactId)!
        );
    };

    const handleDeleteAddressClose = (): void => {
        setOpenDeleteAddress(false);
        setAddressToBeDeleted(null);
    };
    const handleDeleteAddressOpen = (contactIdAndAddressId: {
        contactId: string;
        addressId: string;
    }): void => {
        setAddressToBeDeleted(contactIdAndAddressId);
        setOpenDeleteAddress(true);
    };

    const handleDeleteContactOpen = (contactId: string): void => {
        setOpenDeleteContact({ open: true, contactId });
    };
    const handleDeleteContactClose = (): void => {
        setOpenDeleteContact({ open: false, contactId: null });
    };

    const handleContactsSearch = (value: string): void => {
        setSearchValue(value);
        const newFilteredContacts = data!.contacts.filter(
            (contact) =>
                contact.name?.toLowerCase().startsWith(value.toLowerCase()) ||
                localizePhoneNumber(contact.phoneNumberOne).startsWith(value)
        );
        setFilteredContacts(newFilteredContacts);
        setFilteredTotalCount(newFilteredContacts.length);
        setCurrentPage(1);
    };

    const handleContactOpen = (contactId: string): void => {
        selectContact(contactId);
        setOpenContact(true);
    };
    const handleContactClose = (): void => {
        setOpenContact(false);
        setTimeout(() => {
            setSelected(null);
        }, 250);
    };

    const resetSearch = (): void => {
        setSearchValue('');
        handleContactsSearch('');
    };

    useEffect(() => {
        if (data) {
            setFilteredContacts(data.contacts);
            setFilteredTotalCount(data.totalCount);
        }
    }, [data]);

    const paginatedContacts = (
        contactsToPaginate: Contact[],
        page: number
    ): Contact[] =>
        contactsToPaginate.filter(
            (contact, index) =>
                limit * (page - 1) <= index && index < limit * page
        );

    const count = (totalCounting: number): number =>
        Math.ceil(totalCounting / limit);

    return (
        <Container>
            <Typography sx={{ mt: 1.5, mb: 3 }} variant="h4">
                {t('customers.contacts.customers')}
            </Typography>
            <Box
                display="flex"
                justifyContent="space-between"
                flexDirection={{ xs: 'column-reverse', sm: 'row' }}
                mb={2}
                gap={2}
            >
                <TextField
                    label={`${t('customers.contacts.searchContacts')}...`}
                    value={searchValue}
                    onChange={(e) => handleContactsSearch(e.target.value)}
                    size="small"
                    fullWidth
                    sx={{ maxWidth: 400 }}
                />
                <Button
                    size="small"
                    variant="contained"
                    startIcon={<PersonOutlineOutlinedIcon />}
                    onClick={() => setOpenContact(true)}
                    sx={{ px: 2, height: 40 }}
                >
                    {t('customers.contacts.newCustomer')}
                </Button>
            </Box>
            {isFetching &&
                paginatedContacts(filteredContacts, currentPage).length === 0 &&
                [...new Array(10)].map((e, index) => (
                    // eslint-disable-next-line react/no-array-index-key
                    <Card sx={{ mb: 2 }} key={index}>
                        <Skeleton
                            variant="rectangular"
                            animation="wave"
                            height={74}
                            sx={{ borderRadius: 0.5 }}
                        />
                    </Card>
                ))}
            {data &&
                paginatedContacts(filteredContacts, currentPage).map(
                    (filteredContact) => (
                        <ContactView
                            key={filteredContact.contactId}
                            contact={filteredContact}
                            handleContactOpen={() =>
                                handleContactOpen(filteredContact.contactId)
                            }
                            handleDeleteAddressOpen={handleDeleteAddressOpen}
                            handleDeleteContactOpen={handleDeleteContactOpen}
                        />
                    )
                )}
            {!isFetching &&
                paginatedContacts(filteredContacts, currentPage).length ===
                    0 && (
                    <Typography sx={{ pb: 4, pt: 2 }} align="center">
                        {t('customers.contacts.contactNotFound')}
                    </Typography>
                )}
            {data && data.contacts.length !== 0 && (
                <CustomModal
                    open={openContact}
                    handleClose={() => handleContactClose()}
                >
                    <NewContact
                        handleClose={() => handleContactClose()}
                        initialValues={
                            selected !== null
                                ? {
                                      name: selected.name ?? '',
                                      phoneNumber: localizePhoneNumber(
                                          selected.phoneNumberOne
                                      ),
                                      contactId: selected.contactId,
                                  }
                                : undefined
                        }
                        phoneNumberPreFill={searchValue}
                        resetSearch={resetSearch}
                    />
                </CustomModal>
            )}
            <DeleteAddressModal
                openDeleteAddress={openDeleteAddress}
                handleDeleteAddressClose={handleDeleteAddressClose}
                isLoading={isDeleteAddressLoading}
                onYesClick={() => {
                    deleteAddress({
                        contactId: addressToBeDeleted!.contactId,
                        addressId: addressToBeDeleted!.addressId,
                    })
                        .unwrap()
                        .then(() => {
                            handleDeleteAddressClose();
                            setSearchValue('');
                        });
                }}
                error={deleteAddressError as string}
            />
            <DeleteContactModal
                openDeleteContact={openDeleteContact.open}
                handleDeleteContactClose={handleDeleteContactClose}
                isLoading={isDeleteContactLoading}
                onYesClick={() => {
                    deleteContact(openDeleteContact.contactId!)
                        .unwrap()
                        .then(() => {
                            handleDeleteContactClose();
                            setSearchValue('');
                        });
                }}
                error={deleteContactError as string}
            />
            <Pagination
                count={count(filteredTotalCount)}
                page={currentPage}
                variant="outlined"
                color="primary"
                onChange={(e, value) => {
                    setCurrentPage(value);
                }}
                disabled={isFetching}
            />
            <CustomSnackBar
                message={error as string}
                severity="error"
                onRetry={refetch}
            />
        </Container>
    );
};

export default Contacts;
