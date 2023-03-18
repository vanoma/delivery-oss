import React, { ReactElement, useState } from 'react';
import { Card, Box, Stack, Collapse, IconButton } from '@mui/material';
import moment from 'moment';
import '../../locales/i18n';
import { useTranslation } from 'react-i18next';
import { Package } from '@vanoma/types';
import { AdditionalDetails, CustomSnackBar } from '@vanoma/ui-components';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';
import Statuses from './Statuses';
import TrackingLink from './TrackingLink';
import ActionMenu, { ActionItem } from './Actions/ActionMenu';
import InfoPair from '../InfoPair';
import Actions from './Actions';
import { useGetDriverQuery } from '../../api';
import markerIcon from '../../../public/assets/vanoma-marker.png';
import { DELIVERIES_TAB } from '../../routeNames';

interface Props {
    tab: string;
    delivery: Package;
    actions?: ActionItem[];
    progress: number;
    isFetching: boolean;
}

const DeliveryContainer: React.FC<Props> = ({
    tab,
    delivery,
    actions,
    progress,
    isFetching,
}): ReactElement => {
    const { t, i18n } = useTranslation();
    const [expand, setExpand] = useState(tab === DELIVERIES_TAB.ACTIVE);

    const {
        data,
        isFetching: isFetchingGetDriver,
        error,
        refetch,
    } = useGetDriverQuery(delivery.driverId ?? '', {
        skip: delivery.driverId === null,
    });

    return (
        <Card sx={{ mb: 2, zIndex: 'auto' }}>
            <Box sx={{ p: 2 }}>
                <Box
                    sx={{
                        display: 'flex',
                        flexDirection: 'row',
                        justifyContent: 'space-between',
                    }}
                >
                    <Statuses tab={tab} delivery={delivery} />
                    {actions && <ActionMenu actions={actions} />}
                </Box>
                <Box
                    sx={{
                        display: 'flex',
                        flexDirection: { xs: 'column', sm: 'row' },
                        justifyContent: 'space-between',
                    }}
                >
                    <Stack direction="column" alignItems="baseline">
                        <InfoPair
                            property={t('deliveries.order.placedAt')}
                            value={(() => {
                                moment.locale(i18n.language);
                                return moment(
                                    delivery.deliveryOrder.placedAt
                                ).format(
                                    i18n.language !== 'rw'
                                        ? 'ddd, MMM Do YYYY, h:mm A'
                                        : 'DD/MM/YYYY, h:mm A'
                                );
                            })()}
                        />
                        <TrackingLink delivery={delivery} />
                        <InfoPair
                            property={t('deliveries.order.price')}
                            value={t('deliveries.order.priceWithCurrency', {
                                price: Math.ceil(delivery.totalAmount),
                            })}
                        />
                        {delivery.deliveryOrder.branch && (
                            <InfoPair
                                property={t('deliveries.order.branch')}
                                value={delivery.deliveryOrder.branch.branchName}
                            />
                        )}
                    </Stack>
                    <IconButton
                        sx={{ p: 0.5, marginTop: 'auto', marginLeft: 'auto' }}
                        onClick={() => setExpand(!expand)}
                    >
                        {expand ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                    </IconButton>
                </Box>
            </Box>
            <Collapse in={expand}>
                <AdditionalDetails
                    pkg={delivery}
                    progress={progress}
                    isFetching={isFetching || isFetchingGetDriver}
                    driver={data}
                    fromTitle={t('delivery.stops.from')}
                    toTitle={t('delivery.stops.to')}
                    trackingEventsTitle={t('deliveries.order.trackingEvents')}
                    deliveryRequestedText={t(
                        'deliveries.events.deliveryRequested'
                    )}
                    driverAssignedText={t('deliveries.events.driverAssigned')}
                    goingToPickUpText={t('deliveries.events.goingToPickUp')}
                    pickUpArrivalText={t('deliveries.events.pickUpArrival')}
                    packagePickedUpText={t('deliveries.events.packagePickedUp')}
                    goingToDropOffText={t('deliveries.events.goingToDropOff')}
                    dropOffArrivalText={t('deliveries.events.dropOffArrival')}
                    packageDeliveredText={t(
                        'deliveries.events.packageDelivered'
                    )}
                    packageCancelledText={t(
                        'deliveries.events.packageCancelled'
                    )}
                    googleMapsApiKey={process.env.GOOGLE_API_KEY!}
                    icon={markerIcon}
                />
            </Collapse>
            <CustomSnackBar
                message={error as string}
                severity="error"
                onRetry={refetch}
            />
        </Card>
    );
};

export default DeliveryContainer;
export { Actions };
