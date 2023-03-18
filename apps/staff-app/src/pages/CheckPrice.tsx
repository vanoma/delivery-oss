import React, { ReactElement } from 'react';
import { Container, Typography } from '@mui/material';
import { DeliveryPrice, CustomSnackBar } from '@vanoma/ui-components';
import { useGetDeliveryPricingMutation } from '../api';
import vanomaMarker from '../../public/assets/driver-marker.png';

const CheckPrice = (): ReactElement => {
    const [getDeliveryPricing, { isLoading, error, data }] =
        useGetDeliveryPricingMutation();

    return (
        <Container>
            <Typography sx={{ mt: 1.5, mb: 3 }} variant="h4">
                Check price
            </Typography>
            <DeliveryPrice
                price={data ? data.totalAmount : null}
                isLoading={isLoading}
                markerIcon={vanomaMarker}
                pickUpTitle="Origin"
                dropOffTitle="Destination"
                priceButtonText="Check Price"
                googleMapsApiKey={process.env.GOOGLE_API_KEY!}
                resetPrice={() => {}}
                getPrice={({ origin, destination }) =>
                    getDeliveryPricing({
                        // Default package volume for checking price
                        packages: [{ volume: 0.025, origin, destination }],
                    })
                }
            />
            <CustomSnackBar message={error as string} severity="error" />
        </Container>
    );
};

export default CheckPrice;
