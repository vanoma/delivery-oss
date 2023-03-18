import {
    Button,
    LinearProgress,
    ListItem,
    ListItemText,
    Typography,
} from '@mui/material';
import React from 'react';
import LocationOnOutlinedIcon from '@mui/icons-material/LocationOnOutlined';
import { useTranslation } from 'react-i18next';
import { useGetAddressesQuery } from '../../api';
import { houseNumberAndStreetName } from '../../helpers/address';

const Addresses: React.FC<{
    contactId: string;
    // eslint-disable-next-line no-unused-vars
    handleDeleteAddressOpen: (contactIdAndAddressId: {
        contactId: string;
        addressId: string;
    }) => void;
}> = ({ contactId, handleDeleteAddressOpen }) => {
    const { t } = useTranslation();
    const { data, isFetching } = useGetAddressesQuery({
        contactId,
    });

    return (
        <>
            {isFetching && <LinearProgress />}
            {data && (
                <>
                    {data.addresses.length === 0 ? (
                        <Typography color="text.secondary" p={2}>
                            {t('customers.contact.noAddress')}
                        </Typography>
                    ) : (
                        <>
                            {data.addresses.map((address, index) => (
                                <ListItem
                                    divider={index < data.addresses.length - 1}
                                    key={address.addressId}
                                >
                                    <LocationOnOutlinedIcon sx={{ mr: 2 }} />
                                    <ListItemText
                                        primary={address.addressName}
                                        secondary={houseNumberAndStreetName(
                                            address
                                        )}
                                    />
                                    <Button
                                        variant="outlined"
                                        size="small"
                                        onClick={() =>
                                            handleDeleteAddressOpen({
                                                contactId,
                                                addressId: address.addressId,
                                            })
                                        }
                                    >
                                        {t('customers.contact.delete')}
                                    </Button>
                                </ListItem>
                            ))}
                        </>
                    )}
                </>
            )}
        </>
    );
};

export default Addresses;
