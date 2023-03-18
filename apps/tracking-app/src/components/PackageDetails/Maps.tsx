import React, { useRef } from 'react';
import { Typography } from '@mui/material';
import { Marker, InfoWindow } from '@react-google-maps/api';
import { Package, Driver } from '@vanoma/types';
import { MapBase } from '@vanoma/ui-components';
import driverMarkerIcon from '../../../static/driver-marker.png';
import fromMarkerIcon from '../../../static/from-marker.png';
import toMarkerIcon from '../../../static/to-marker.png';

const Maps: React.FC<{ pkg: Package; driver: Driver | undefined }> = ({
    pkg,
    driver,
}) => {
    const driverMaker = useRef(null);

    const fromPosition = {
        lat: pkg.fromAddress.latitude,
        lng: pkg.fromAddress.longitude,
    };

    const toPosition = {
        lat: pkg.toAddress.latitude,
        lng: pkg.toAddress.longitude,
    };

    const driverPosition =
        driver !== undefined && driver.latestLocation !== null
            ? {
                  lat: driver.latestLocation!.latitude,
                  lng: driver.latestLocation!.longitude,
              }
            : null;

    return (
        <MapBase
            showSearchBox={false}
            zoom={13}
            googleMapsApiKey={process.env.GOOGLE_API_KEY!}
            // eslint-disable-next-line no-undef
            onLoadMap={(map: google.maps.Map) => {
                // Fit map viewport between the origin and destination locations
                // https://github.com/JustFly1984/react-google-maps-api/blob/1c5a2a7d24f916000ac09ce07c2cf6edcc9168bb/packages/react-google-maps-api/README.md#migration-from-react-google-maps945
                const bounds = new window.google.maps.LatLngBounds();
                bounds.extend(fromPosition);
                bounds.extend(toPosition);

                if (driverPosition) {
                    bounds.extend(driverPosition);
                }

                map.fitBounds(bounds);
            }}
        >
            <>
                <Marker position={fromPosition} icon={fromMarkerIcon} />
                <Marker position={toPosition} icon={toMarkerIcon} />
                {driverPosition && (
                    <Marker
                        position={driverPosition}
                        icon={driverMarkerIcon}
                        ref={driverMaker}
                    >
                        {driverMaker.current && (
                            <InfoWindow
                                anchor={driverMaker.current}
                                options={{ disableAutoPan: true }}
                            >
                                <Typography
                                    variant="subtitle1"
                                    color="black"
                                    sx={{
                                        lineHeight: 1,
                                    }}
                                >
                                    {driver!.firstName}
                                </Typography>
                            </InfoWindow>
                        )}
                    </Marker>
                )}
            </>
        </MapBase>
    );
};

export default React.memo(Maps);
