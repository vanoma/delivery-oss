import React, { ReactChild, ReactElement, useState } from 'react';
import {
    Card,
    Box,
    Stack,
    Collapse,
    IconButton,
    Typography,
    Divider,
} from '@mui/material';
import {
    AdditionalDetails,
    DeliveryRequestProgress,
} from '@vanoma/ui-components';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';
import { useSelector } from 'react-redux';
import { Delivery } from '../../types';
import Statuses from './Statuses';
import Actions from './Actions';
import markerIcon from '../../../public/assets/driver-marker.png';
import DeliveryLinearIndicator from './DeliveryLinearIndicator';
import { DELIVERIES_TAB } from '../../routeNames';
import { selectCurrentTab } from '../../redux/slices/deliveriesSlice';

interface Props {
    delivery: Delivery;
    expandAll: boolean;
    children: ReactChild;
    isRequest?: boolean;
}

const DeliveryListItem: React.FC<Props> = ({
    delivery,
    expandAll,
    children,
    isRequest,
}): ReactElement => {
    const [expand, setExpand] = useState(isRequest);
    const currentTab = useSelector(selectCurrentTab);

    return (
        <Card sx={{ mb: 2, zIndex: 'auto' }}>
            {currentTab === DELIVERIES_TAB.ACTIVE && (
                <DeliveryLinearIndicator delivery={delivery} />
            )}
            <Box p={2} pt={1.625}>
                <Box
                    sx={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'start',
                    }}
                >
                    <Statuses delivery={delivery} />
                    <Actions delivery={delivery} />
                </Box>
                <Box
                    sx={{
                        display: 'flex',
                        flexDirection: { xs: 'column', sm: 'row' },
                        justifyContent: 'space-between',
                    }}
                >
                    <Stack direction="column" alignItems="baseline">
                        {children}
                    </Stack>
                    <IconButton
                        sx={{ p: 0.5, marginTop: 'auto', marginLeft: 'auto' }}
                        onClick={() => setExpand(!expand)}
                        disabled={expandAll}
                    >
                        {expand || expandAll ? (
                            <ExpandLessIcon />
                        ) : (
                            <ExpandMoreIcon />
                        )}
                    </IconButton>
                </Box>
            </Box>
            <Collapse in={expand || expandAll}>
                {isRequest ? (
                    <>
                        <Divider />
                        <DeliveryRequestProgress
                            pkg={delivery.package}
                            renderPaymentContent={() => (
                                <Typography
                                    variant="body2"
                                    color="text.secondary"
                                >
                                    {`Waiting for the business to pay Rwf ${Math.ceil(
                                        delivery.package.totalAmount
                                    )}.`}
                                </Typography>
                            )}
                            sentText="Sent"
                            deliveryLinkSentText="SMS with delivery link sent."
                            openedText="Opened"
                            yetToBeOpenedText="The buyer is yet to open the delivery link."
                            buyerOpenedLinkText="The buyer opened the delivery link."
                            addressText="Address"
                            yetToProvideAddressText="The buyer is yet to provide the delivery address."
                            buyerProvidedAddressText="The buyer provided the delivery address."
                            paymentText="Payment"
                            yetToBePaidText="The delivery fee is yet to be paid."
                            dispatchedText="Dispatched"
                            yetToBeDispatchedText="Delivery driver is yet to be dispatched."
                            waitingForBuyerToPayText={`Waiting for the buyer to pay Rwf ${Math.ceil(
                                delivery.package.totalAmount
                            )}.`}
                            isStaff
                            googleMapsApiKey={process.env.GOOGLE_API_KEY!}
                            locationIcon={markerIcon}
                            pickUpText="Pick-up"
                            dropOffText="Drop-off"
                        />
                    </>
                ) : (
                    <AdditionalDetails
                        pkg={delivery.package}
                        progress={0}
                        isFetching={false}
                        driver={delivery.assignment?.driver}
                        fromTitle="From"
                        toTitle="To"
                        trackingEventsTitle="Delivery events"
                        deliveryRequestedText="Delivery requested"
                        driverAssignedText="Driver assigned"
                        goingToPickUpText="Going to pick-up"
                        pickUpArrivalText="Arrived at pick-up"
                        packagePickedUpText="Package picked Up"
                        goingToDropOffText="Going to drop-off"
                        dropOffArrivalText="Arrived at drop-off"
                        packageDeliveredText="Package delivered"
                        packageCancelledText="Delivery has been cancelled"
                        isStaff
                        googleMapsApiKey={process.env.GOOGLE_API_KEY!}
                        icon={markerIcon}
                    />
                )}
            </Collapse>
        </Card>
    );
};

export default DeliveryListItem;

export { default as Properties } from './Properties';
