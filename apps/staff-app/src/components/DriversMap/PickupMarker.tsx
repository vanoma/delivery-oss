/* eslint-disable no-nested-ternary */
import { InfoWindow, Marker } from '@react-google-maps/api';
import React, { useRef } from 'react';
import { Typography } from '@mui/material';
import { localizePhoneNumber } from '@vanoma/helpers';
import { Delivery } from '../../types';
import markerIcon from '../../../public/assets/pickup-marker.png';
import assignedMarkerIcon from '../../../public/assets/assigned-pickup-marker.png';
import confirmedAssignedMarkerIcon from '../../../public/assets/confirmed-assigned-pickup-marker.png';

const PickupMarker: React.FC<{
    delivery: Delivery;
    showExtraInfo: boolean;
    handleClick?: () => void;
    handleRightClick?: () => void;
}> = ({ delivery, showExtraInfo, handleClick, handleRightClick }) => {
    const ref = useRef(null);

    return (
        <Marker
            position={{
                lat: delivery.package.fromAddress.latitude,
                lng: delivery.package.fromAddress.longitude,
            }}
            icon={
                delivery.assignment !== null
                    ? delivery.assignment.confirmedAt !== null
                        ? confirmedAssignedMarkerIcon
                        : assignedMarkerIcon
                    : markerIcon
            }
            ref={ref}
            onClick={handleClick}
            onRightClick={handleRightClick}
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
                            {delivery.package.fromContact.name}
                        </Typography>
                        {showExtraInfo && (
                            <Typography
                                color="black"
                                sx={{
                                    lineHeight: 1,
                                    mt: 0.5,
                                }}
                            >
                                {localizePhoneNumber(
                                    delivery.package.fromContact.phoneNumberOne
                                )}
                            </Typography>
                        )}
                    </>
                </InfoWindow>
            )}
        </Marker>
    );
};

export default PickupMarker;
