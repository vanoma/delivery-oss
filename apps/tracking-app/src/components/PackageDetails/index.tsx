import React from 'react';
import { Box } from '@mui/material';
import { styled } from '@mui/material/styles';
import { Driver, Package } from '@vanoma/types';
import { AdditionalDetails } from '@vanoma/ui-components';
import { useTranslation } from 'react-i18next';
import Maps from './Maps';

const PackageContainer = styled('div')(({ theme }) => ({
    border: `1px solid ${theme.palette.primary.light}`,
    borderRadius: theme.spacing(2),
    margin: theme.spacing(2),
}));

const PackageDetails: React.FC<{
    pkg: Package;
    driver: Driver | undefined;
    responsive?: boolean;
}> = ({ pkg, driver, children, responsive = true }) => {
    const { status } = pkg;
    const { t } = useTranslation();

    return (
        <PackageContainer>
            {(status === 'PLACED' || status === 'ASSIGNED') && (
                <Box
                    sx={{
                        height: children === undefined ? 400 : undefined,
                    }}
                >
                    {children === undefined ? (
                        <Maps pkg={pkg!} driver={driver} />
                    ) : (
                        children
                    )}
                </Box>
            )}
            <AdditionalDetails
                pkg={pkg}
                progress={0}
                isFetching={false}
                driver={driver}
                fromTitle={t('from')}
                toTitle={t('to')}
                trackingEventsTitle={t('deliveryEvents')}
                deliveryRequestedText={t('deliveryRequested')}
                driverAssignedText={t('driverAssigned')}
                goingToPickUpText={t('goingToPickUp')}
                pickUpArrivalText={t('pickUpArrival')}
                packagePickedUpText={t('packagePickedUp')}
                goingToDropOffText={t('goingToDropOff')}
                dropOffArrivalText={t('dropOffArrival')}
                packageDeliveredText={t('packageDelivered')}
                packageCancelledText={t('deliveryCancelled')}
                isTracking
                responsive={responsive}
            />
        </PackageContainer>
    );
};

export default PackageDetails;
