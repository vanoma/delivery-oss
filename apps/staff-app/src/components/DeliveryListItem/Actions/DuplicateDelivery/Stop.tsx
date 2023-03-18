import React from 'react';
import { Box, Typography } from '@mui/material';
import {
    formatAddressForPrivateView,
    formatContactForPrivateView,
} from '@vanoma/helpers';
import { LocationButton } from '@vanoma/ui-components';
import { Address, Contact } from '@vanoma/types';
import markerIcon from '../../../../../public/assets/driver-marker.png';

const Stop: React.FC<{ contact: Contact; address: Address }> = ({
    contact,
    address,
}) => {
    return (
        <Box
            display="flex"
            justifyContent="space-between"
            alignItems="center"
            mb={2}
            borderRadius={0.5}
            p={2}
            sx={(theme) => ({
                border: `1px solid ${theme.palette.primary.light}`,
            })}
        >
            <Box>
                <Typography variant="body1">
                    {formatContactForPrivateView(contact)}
                </Typography>
                <Typography color="text.secondary">
                    {formatAddressForPrivateView(address)}
                </Typography>
            </Box>
            <LocationButton
                location={{
                    lat: address.latitude,
                    lng: address.longitude,
                }}
                googleMapsApiKey={process.env.GOOGLE_API_KEY!}
                icon={markerIcon}
            />
        </Box>
    );
};

export default Stop;
