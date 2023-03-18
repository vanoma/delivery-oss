import React, { useEffect, useRef, useState } from 'react';
import { Box, FormHelperText, IconButton } from '@mui/material';
import { MapBase } from '@vanoma/ui-components';
import { Driver, DriverStatus } from '@vanoma/types';
import { DirectionsRenderer, TrafficLayer } from '@react-google-maps/api';
import { shallowEqual, useSelector } from 'react-redux';
import VisibilityIcon from '@mui/icons-material/Visibility';
import TrafficIcon from '@mui/icons-material/Traffic';
import { useGetDriverQuery, useGetDriversQuery } from '../../api';
import DriverMarker from './DriverMarker';
import {
    selectCurrentTab,
    selectDeliveries,
} from '../../redux/slices/deliveriesSlice';
import PickupMarker from './PickupMarker';
import { Delivery } from '../../types';
import DropOffMarker from './DropOffMarker';
import AssignButton from './AssignButton';
import { DELIVERIES_TAB } from '../../routeNames';

// eslint-disable-next-line no-shadow, no-unused-vars
enum RouteType {
    // eslint-disable-next-line no-unused-vars
    DRIVER_PICKUP,
    // eslint-disable-next-line no-unused-vars
    NEW_DELIVERY,
    // eslint-disable-next-line no-unused-vars
    ONGOING_DELIVERY,
}

const DriversMap: React.FC<{
    handleOpen?: () => void;
    handleClose?: () => void;
}> = ({ handleOpen, handleClose }) => {
    const currentTab = useSelector(selectCurrentTab);
    const deliveries = useSelector(selectDeliveries, shallowEqual);

    const [selectedDriver, setSelectedDriver] = useState<Driver | null>(null);
    const [selectedDelivery, setSelectedDelivery] = useState<Delivery | null>(
        null
    );
    const [driverOngoingDelivery, setDriverOngoingDelivery] =
        useState<Delivery | null>(null);
    const [showTraffic, setShowTraffic] = useState(false);
    const [deliveriesCopy, setDeliveriesCopy] = useState(
        currentTab === DELIVERIES_TAB.ACTIVE ? deliveries : []
    );
    const [directionsResponseDriverPickup, setDirectionsResponseDriverPickup] =
        // eslint-disable-next-line no-undef
        useState<google.maps.DirectionsResult | null>(null);
    const [directionsResponseDelivery, setDirectionsResponseDelivery] =
        // eslint-disable-next-line no-undef
        useState<google.maps.DirectionsResult | null>(null);
    const [
        directionsResponseOngoingDelivery,
        setDirectionsResponseOngoingDelivery,
    ] =
        // eslint-disable-next-line no-undef
        useState<google.maps.DirectionsResult | null>(null);
    // eslint-disable-next-line no-undef
    const mapRef = useRef<google.maps.Map>();

    const { data: driversData, error: driversError } = useGetDriversQuery(
        {
            status: [DriverStatus.ACTIVE, DriverStatus.PENDING],
        },
        {
            pollingInterval: 1000,
            skip: selectedDriver !== null,
        }
    );
    const { data: driverData, error: driverError } = useGetDriverQuery(
        selectedDriver?.driverId ?? '',
        {
            pollingInterval: 1000,
            skip: selectedDriver == null,
        }
    );

    const [driversCopy, setDriversCopy] = useState<Driver[]>([]);

    const showRoute = async (
        // eslint-disable-next-line no-undef
        origin: google.maps.LatLngLiteral,
        // eslint-disable-next-line no-undef
        destination: google.maps.LatLngLiteral,
        route: RouteType
    ): Promise<void> => {
        // eslint-disable-next-line no-undef
        const directionsService = new google.maps.DirectionsService();

        try {
            const results = await directionsService.route({
                origin,
                destination,
                // eslint-disable-next-line no-undef
                travelMode: google.maps.TravelMode.DRIVING,
            });
            if (route === RouteType.DRIVER_PICKUP) {
                setDirectionsResponseDriverPickup(results);
            } else if (route === RouteType.NEW_DELIVERY) {
                setDirectionsResponseDelivery(results);
            } else if (route === RouteType.ONGOING_DELIVERY) {
                setDirectionsResponseOngoingDelivery(results);
            }
            // eslint-disable-next-line no-empty
        } catch (_) {}
    };

    const resetEverything = (): void => {
        setSelectedDriver(null);
        setSelectedDelivery(null);
        setDriverOngoingDelivery(null);
        setDirectionsResponseDriverPickup(null);
        setDirectionsResponseDelivery(null);
        setDirectionsResponseOngoingDelivery(null);
        setDeliveriesCopy(
            currentTab === DELIVERIES_TAB.ACTIVE ? deliveries : []
        );
        if (driversData) {
            setDriversCopy([...driversData.results]);
        }
        if (mapRef.current) {
            mapRef.current.panTo({ lat: -1.953593, lng: 30.092391 });
            mapRef.current.setZoom(14);
        }
    };

    useEffect(() => {
        if (selectedDelivery) {
            showRoute(
                {
                    lat: selectedDelivery.package.fromAddress.latitude,
                    lng: selectedDelivery.package.fromAddress.longitude,
                },
                {
                    lat: selectedDelivery.package.toAddress.latitude,
                    lng: selectedDelivery.package.toAddress.longitude,
                },
                RouteType.NEW_DELIVERY
            );
        }
    }, [selectedDelivery]);

    useEffect(() => {
        if (selectedDriver) {
            if (selectedDelivery) {
                showRoute(
                    {
                        lat: selectedDriver.latestLocation!.latitude,
                        lng: selectedDriver.latestLocation!.longitude,
                    },
                    {
                        lat: selectedDelivery.package.fromAddress.latitude,
                        lng: selectedDelivery.package.fromAddress.longitude,
                    },
                    RouteType.DRIVER_PICKUP
                );
            }
            if (selectedDriver.assignmentCount !== 0) {
                const delivery = deliveriesCopy.find(
                    (del) =>
                        del.assignment !== null &&
                        del.assignment.driver.driverId ===
                            selectedDriver.driverId
                );
                if (delivery) {
                    showRoute(
                        {
                            lat: delivery.package.fromAddress.latitude,
                            lng: delivery.package.fromAddress.longitude,
                        },
                        {
                            lat: delivery.package.toAddress.latitude,
                            lng: delivery.package.toAddress.longitude,
                        },
                        RouteType.ONGOING_DELIVERY
                    );
                    setDriverOngoingDelivery(delivery);
                }
            }
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [selectedDriver, selectedDelivery]);

    useEffect(() => {
        if (driversData) {
            if (
                driversCopy.length === 0 ||
                driversData.results.length === driversCopy.length
            ) {
                setDriversCopy([...driversData.results]);
            } else {
                const filteredList = driversData.results.filter(
                    (driver) =>
                        driversCopy.findIndex(
                            (dr) => dr.driverId === driver.driverId
                        ) !== -1
                );
                setDriversCopy([...filteredList]);
            }
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [driversData]);

    return (
        <>
            <MapBase
                showSearchBox={false}
                zoom={14}
                googleMapsApiKey={process.env.GOOGLE_API_KEY!}
                onLoadMap={(map) => {
                    mapRef.current = map;
                }}
            >
                <>
                    <Box
                        display="flex"
                        justifyContent="space-between"
                        alignItems="start"
                        p={1.25}
                    >
                        <Box display="flex" flexDirection="column">
                            <IconButton
                                sx={{
                                    backgroundColor: showTraffic
                                        ? '#53D61B'
                                        : '#b4b4b4',
                                    mb: 2,
                                }}
                                onClick={() => setShowTraffic(!showTraffic)}
                            >
                                <TrafficIcon />
                            </IconButton>
                            {(selectedDriver ||
                                selectedDelivery ||
                                deliveries.length !== deliveriesCopy.length ||
                                (driversData &&
                                    driversData.results.length !==
                                        driversCopy.length)) && (
                                <IconButton
                                    sx={{ backgroundColor: '#b4b4b4' }}
                                    onClick={resetEverything}
                                >
                                    <VisibilityIcon />
                                </IconButton>
                            )}
                        </Box>
                        {selectedDriver && selectedDelivery && (
                            <AssignButton
                                driver={selectedDriver}
                                delivery={selectedDelivery}
                                handleOpen={handleOpen}
                                handleClose={handleClose}
                            />
                        )}
                    </Box>
                    {selectedDriver ? (
                        <DriverMarker
                            key={selectedDriver.driverId}
                            driver={driverData ?? selectedDriver}
                            handleClick={() => {}}
                            selected
                        />
                    ) : (
                        driversCopy
                            .filter((driver) => driver.latestLocation !== null)
                            .map((driver) => (
                                <DriverMarker
                                    key={driver.driverId}
                                    driver={driver}
                                    handleClick={() =>
                                        setSelectedDriver(driver)
                                    }
                                    handleRightClick={() =>
                                        setDriversCopy(
                                            driversCopy.filter(
                                                (drv) =>
                                                    drv.driverId !==
                                                    driver.driverId
                                            )
                                        )
                                    }
                                />
                            ))
                    )}
                    {selectedDelivery ? (
                        <>
                            <PickupMarker
                                key={selectedDelivery.package.packageId}
                                delivery={selectedDelivery}
                                showExtraInfo={selectedDelivery !== null}
                                handleClick={() => {}}
                            />
                            <DropOffMarker
                                key={
                                    selectedDelivery.package.toContact.contactId
                                }
                                delivery={selectedDelivery}
                            />
                        </>
                    ) : (
                        deliveriesCopy
                            .filter(
                                (delivery) =>
                                    delivery.assignment === null ||
                                    delivery.assignment.confirmedAt === null
                            )
                            .map((delivery) => (
                                <PickupMarker
                                    key={delivery.package.packageId}
                                    delivery={delivery}
                                    showExtraInfo={selectedDelivery !== null}
                                    handleClick={() =>
                                        setSelectedDelivery(delivery)
                                    }
                                    handleRightClick={() =>
                                        setDeliveriesCopy(
                                            deliveriesCopy.filter(
                                                (deliv) =>
                                                    deliv.package.packageId !==
                                                    delivery.package.packageId
                                            )
                                        )
                                    }
                                />
                            ))
                    )}
                    {driverOngoingDelivery && (
                        <>
                            <PickupMarker
                                key={driverOngoingDelivery.package.packageId}
                                delivery={driverOngoingDelivery}
                                showExtraInfo={driverOngoingDelivery !== null}
                                handleClick={() => {}}
                            />
                            <DropOffMarker
                                key={
                                    driverOngoingDelivery.package.toContact
                                        .contactId
                                }
                                delivery={driverOngoingDelivery}
                            />
                        </>
                    )}
                    {directionsResponseDriverPickup && (
                        <DirectionsRenderer
                            directions={directionsResponseDriverPickup}
                            options={{
                                suppressMarkers: true,
                                polylineOptions: {
                                    strokeColor: '#FFC107',
                                    strokeWeight: 8,
                                },
                            }}
                        />
                    )}
                    {directionsResponseDelivery && (
                        <DirectionsRenderer
                            directions={directionsResponseDelivery}
                            options={{
                                suppressMarkers: true,
                                polylineOptions: {
                                    strokeColor: '#d61b53',
                                    strokeWeight: 8,
                                },
                            }}
                        />
                    )}
                    {directionsResponseOngoingDelivery && (
                        <DirectionsRenderer
                            directions={directionsResponseOngoingDelivery}
                            options={{
                                suppressMarkers: true,
                                polylineOptions: {
                                    strokeColor: '#53D61B',
                                    strokeWeight: 8,
                                },
                            }}
                        />
                    )}
                    {showTraffic && <TrafficLayer />}
                </>
            </MapBase>
            {(driversError || driverError) && (
                <FormHelperText sx={{ mt: 2 }} error>
                    {driversError || driverError}
                </FormHelperText>
            )}
        </>
    );
};

export default DriversMap;
