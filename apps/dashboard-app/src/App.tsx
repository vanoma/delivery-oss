import React, { FC, Suspense, useEffect } from 'react';
import { useRoutes, useNavigate, useLocation } from 'react-router-dom';
import { ThemeConfig } from '@vanoma/ui-theme';
import { useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Typography, Stack, Box, Button } from '@mui/material';
import * as Sentry from '@sentry/react';
import LoadingPage from './components/LoadingPage';
import { updateSWRegistrations } from './helpers/serviceWorker';
import {
    getDefaultData,
    selectIsAuthenticated,
} from './redux/slices/authenticationSlice';
import routes from './routes';
import { initializeOnesignal } from './services/oneSignal';
import { initializeSentry } from './services/sentry';
import { useTypedDispatch } from './helpers/reduxToolkit';

const App: FC = () => {
    const isAuthenticated = useSelector(selectIsAuthenticated);
    const navigate = useNavigate();
    const location = useLocation();
    const { t } = useTranslation();
    const dispatch = useTypedDispatch();

    useEffect(() => {
        initializeOnesignal(navigate);
        // getDefaultData will make http calls and may potentially fail. However, we can't show it's error
        // in this component because authenticationSlice is a bit global; we'd end up seeing all errors
        // happening for example while signing in or signing up. And that's not the desired behaviors.
        dispatch(getDefaultData());
        initializeSentry();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    useEffect(() => {
        updateSWRegistrations();
    }, [location]);

    return (
        <ThemeConfig>
            <Sentry.ErrorBoundary
                fallback={
                    <Box justifyContent="center" sx={{ display: 'flex' }}>
                        <Stack py={32} px={2} spacing={4} maxWidth={400}>
                            <Typography variant="h6" align="center">
                                {t('application.exception.message')}
                            </Typography>
                            <Typography variant="body1" align="center">
                                {t('application.exception.callout')}
                            </Typography>
                            <Button
                                size="medium"
                                variant="contained"
                                sx={{ alignSelf: 'center' }}
                                onClick={() => window.location.reload()}
                            >
                                {t('application.exception.refresh')}
                            </Button>
                        </Stack>
                    </Box>
                }
            >
                <Suspense fallback={<LoadingPage />}>
                    {useRoutes(routes(isAuthenticated))}
                </Suspense>
            </Sentry.ErrorBoundary>
        </ThemeConfig>
    );
};

export default App;
