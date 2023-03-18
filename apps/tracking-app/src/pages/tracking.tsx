import React, { useState } from 'react';
import {
    Typography,
    Container,
    Card,
    Box,
    Snackbar,
    Alert,
} from '@mui/material';
import { useQueryParam, StringParam } from 'use-query-params';
import { Driver, Package } from '@vanoma/types';
import { graphql, PageProps } from 'gatsby';
import { useTranslation } from 'react-i18next';
import SEO from '../components/SEO';
import PackageDetails from '../components/PackageDetails';
import TrackingNumberForm from '../components/TrackingNumberForm';
import Layout from '../components/Layout';
import httpClient from '../helpers/httpService';
import LanguagePopover from '../components/LanguagePopover';

const Tracking: React.FC<PageProps> = (pageProps) => {
    const [pkg, setPkg] = useState<Package | null>(null);
    const [driver, setDriver] = useState<Driver>();
    const [error, setError] = useState<string | null>(null);
    // Start in loading mode to avoid flashing UI between both states of missing (not found) & available tracking number.
    const [isLoading, setIsLoading] = useState<boolean>(true);

    const [trackingNumber, setTrackingNumber] = useQueryParam(
        'tn',
        StringParam
    );
    const { t } = useTranslation();

    const fetchPackageData = (pkgDriverId: string | null = null): void => {
        Promise.all([
            httpClient.get<Package>(`/package-tracking/${trackingNumber}`),
            pkgDriverId !== null
                ? httpClient.get<Driver>(`/driver-tracking/${pkgDriverId}`)
                : Promise.resolve(null),
        ])
            .then(([packageResponse, driverResponse]) => {
                setPkg(packageResponse.data);
                setDriver(
                    driverResponse !== null ? driverResponse.data : undefined
                );

                // Stop loading after initial data. Subsequent API calls with load data silently
                setIsLoading(false);

                // Continuously poll for driver location if the package is assigned
                const { status, driverId } = packageResponse.data;
                if (status === 'PLACED' || status === 'ASSIGNED') {
                    setTimeout(() => fetchPackageData(driverId), 5000);
                }
            })
            .catch((err: { message: string }) => {
                setError(err.message);
                setIsLoading(false);
            });
    };

    const changeTrackingNumber = (NewTrackingNumber: string): void => {
        if (trackingNumber !== NewTrackingNumber) {
            setPkg(null);
            setTrackingNumber(NewTrackingNumber);
        }
    };

    const handleCloseSnackBar = (): void => {
        setError(null);
    };

    React.useEffect(() => {
        if (trackingNumber) {
            setIsLoading(true);
            fetchPackageData();
        } else {
            setIsLoading(false);
        }

        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [trackingNumber]);

    return (
        <>
            <SEO
                title="Tracking"
                pageProps={pageProps}
                description="Follow the progress of a delivery in real-time on a map and estimate the duration of a delivery."
            />
            <Layout pageProps={pageProps} showLanguagesModal>
                <Container sx={{ pt: 5 }}>
                    <Box display="flex" justifyContent="flex-end">
                        <LanguagePopover pageProps={pageProps} />
                    </Box>
                </Container>
                <Container sx={{ pt: 10 }}>
                    <Typography
                        variant="h2"
                        align="center"
                        marginBottom={3}
                        component="h1"
                    >
                        {t('deliveryTracking')}
                    </Typography>
                    <Typography
                        align="center"
                        maxWidth={700}
                        marginX="auto"
                        marginBottom={10}
                    >
                        {t('deliveryTrackingSubtitle')}
                    </Typography>
                    <TrackingNumberForm
                        trackingNumber={trackingNumber}
                        handleTrackingNumberChange={changeTrackingNumber}
                        isLoading={isLoading}
                        marginBottom={5}
                    />
                    {trackingNumber && !pkg && !isLoading && (
                        <Typography color="primary" align="center">
                            {t('deliveryNotFound')}
                        </Typography>
                    )}
                    {pkg && (
                        <Card>
                            <PackageDetails pkg={pkg} driver={driver} />
                        </Card>
                    )}
                    <Box marginTop={15} />
                    <Snackbar
                        open={error !== null}
                        autoHideDuration={6000}
                        onClose={handleCloseSnackBar}
                        anchorOrigin={{
                            vertical: 'bottom',
                            horizontal: 'center',
                        }}
                    >
                        <Alert
                            onClose={handleCloseSnackBar}
                            severity="error"
                            variant="filled"
                        >
                            {error}
                        </Alert>
                    </Snackbar>
                </Container>
            </Layout>
        </>
    );
};

export default Tracking;

export const query = graphql`
    query ($language: String!) {
        locales: allLocale(
            filter: { language: { eq: $language }, ns: { in: ["tracking"] } }
        ) {
            edges {
                node {
                    ns
                    data
                    language
                }
            }
        }
    }
`;
