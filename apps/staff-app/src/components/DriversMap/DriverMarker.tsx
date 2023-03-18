import React, { useRef } from 'react';
import { TimelineDot } from '@mui/lab';
import { Stack, Typography } from '@mui/material';
import { Marker, InfoWindow } from '@react-google-maps/api';
import { Driver } from '@vanoma/types';
import LocationOffIcon from '@mui/icons-material/LocationOff';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import { formatDistanceToNowStrict } from 'date-fns';
import moment from 'moment';
import markerIcon from '../../../public/assets/driver-marker.png';
import { getDriverStatusColor } from '../../helpers/driver';

export default function DriverMarker({
    driver,
    handleClick,
    handleRightClick,
    selected = false,
}: {
    driver: Driver;
    handleClick?: () => void;
    handleRightClick?: () => void;
    selected?: boolean;
}): JSX.Element {
    const ref = useRef(null);

    const isLocationAccessible =
        driver.latestLocation &&
        driver.latestLocation.isLocationServiceEnabled &&
        driver.latestLocation.locationAccessStatus === 'ALLOWED_ALWAYS';

    return driver.latestLocation ? (
        <Marker
            position={{
                lat: driver.latestLocation.latitude,
                lng: driver.latestLocation.longitude,
            }}
            icon={markerIcon}
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
                        <Stack
                            direction="row"
                            sx={{
                                height: 16,
                            }}
                            spacing={1}
                        >
                            <TimelineDot
                                color={getDriverStatusColor(driver)}
                                sx={{ mt: 0.1 }}
                            />
                            <Typography
                                variant="subtitle1"
                                color="black"
                                sx={{
                                    lineHeight: 1,
                                }}
                            >
                                {`${driver.firstName} ${driver.lastName[0]}`}
                            </Typography>
                        </Stack>
                        {!isLocationAccessible && (
                            <Stack
                                direction="row"
                                spacing={0.25}
                                alignItems="center"
                            >
                                <LocationOffIcon
                                    color="error"
                                    fontSize="small"
                                />
                                <Typography color="black">
                                    {!driver.latestLocation
                                        .isLocationServiceEnabled
                                        ? 'Location disabled'
                                        : driver.latestLocation.locationAccessStatus
                                              .split('_')
                                              .join(' ')
                                              .toLowerCase()}
                                </Typography>
                            </Stack>
                        )}
                        {selected && (
                            <Stack
                                direction="row"
                                alignItems="center"
                                justifyContent="space-between"
                                spacing={2}
                            >
                                <Typography color="black">{`${Math.fround(
                                    driver.latestLocation.batteryLevel * 100
                                )}%`}</Typography>
                                <Typography variant="caption" color="black">
                                    {formatDistanceToNowStrict(
                                        new Date(
                                            driver.latestLocation.updatedAt
                                        )
                                    )}
                                </Typography>
                            </Stack>
                        )}
                        {isLocationAccessible &&
                            driver.isAvailable &&
                            // It's been longer than 10 min since last location update
                            Math.abs(
                                moment
                                    .utc()
                                    .diff(
                                        moment(driver.latestLocation.updatedAt)
                                    )
                            ) >
                                10 * 60 * 1000 && (
                                <Stack
                                    direction="row"
                                    spacing={0.25}
                                    alignItems="center"
                                >
                                    <LocationOnIcon
                                        color="warning"
                                        fontSize="small"
                                    />
                                    <Typography color="black" variant="caption">
                                        {formatDistanceToNowStrict(
                                            new Date(
                                                driver.latestLocation.updatedAt
                                            )
                                        )}
                                    </Typography>
                                </Stack>
                            )}
                    </>
                </InfoWindow>
            )}
        </Marker>
    ) : (
        <></>
    );
}
