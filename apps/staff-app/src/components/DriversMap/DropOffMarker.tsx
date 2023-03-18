import { InfoWindow, Marker } from '@react-google-maps/api';
import React, { useRef } from 'react';
import { Typography } from '@mui/material';
import { localizePhoneNumber } from '@vanoma/helpers';
import { Delivery } from '../../types';
import markerIcon from '../../../public/assets/drop-off-marker.png';

const DropOffMarker: React.FC<{ delivery: Delivery }> = ({ delivery }) => {
    const ref = useRef(null);
    return (
        <Marker
            position={{
                lat: delivery.package.toAddress.latitude,
                lng: delivery.package.toAddress.longitude,
            }}
            icon={markerIcon}
            ref={ref}
        >
            {ref.current && (
                <InfoWindow
                    anchor={ref.current}
                    options={{
                        disableAutoPan: true,
                    }}
                >
                    <>
                        <Typography
                            variant="subtitle1"
                            color="primary.dark"
                            sx={{
                                lineHeight: 1,
                            }}
                        >
                            {delivery.package.toContact.name}
                        </Typography>
                        <Typography
                            color="black"
                            sx={{
                                lineHeight: 1,
                                mt: 0.5,
                            }}
                        >
                            {localizePhoneNumber(
                                delivery.package.toContact.phoneNumberOne
                            )}
                        </Typography>
                    </>
                </InfoWindow>
            )}
        </Marker>
    );
};

export default DropOffMarker;
