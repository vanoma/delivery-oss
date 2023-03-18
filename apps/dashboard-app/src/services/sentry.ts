/* eslint-disable no-console */
import {
    Action,
    PreloadedState,
    Reducer,
    SerializedError,
    StoreEnhancerStoreCreator,
} from '@reduxjs/toolkit';
import * as Sentry from '@sentry/react';
import { BrowserTracing } from '@sentry/tracing';

export const initializeSentry = (): void => {
    console.log('Initializing Sentry');
    Sentry.init({
        dsn: process.env.SENTRY_DSN,
        environment: process.env.SENTRY_ENVIRONMENT,
        allowUrls: ['https://dashboard.vanoma.com'], // Send errors for production environment only
        integrations: [new BrowserTracing()],
        tracesSampleRate: 0.0,
    });
};

export const setUserIdentifier = ({
    customerId,
    businessName,
    phoneNumber,
}: {
    customerId: string;
    businessName: string;
    phoneNumber: string;
}): void => {
    // We are not removing user info after they logout because we would rather have their previous info
    // than having unknown until they login in again with the same or different account and override the current one
    Sentry.setUser({
        id: customerId ?? 'N/A',
        username: `${businessName} (${phoneNumber ?? ''})`,
    });
};

interface PayloadAction extends Action<string> {
    error: SerializedError;
}

export const createReduxEnhancer = (): any => {
    // Adapted from https://github.com/getsentry/sentry-javascript/blob/8cbcff25235b8d67043820032dba7a9e9cae3a2a/packages/react/src/redux.ts#L83
    return (next: StoreEnhancerStoreCreator): StoreEnhancerStoreCreator =>
        <S = any, A extends Action = PayloadAction>(
            reducer: Reducer<S, A>,
            initialState?: PreloadedState<S>
        ) => {
            const sentryReducer: Reducer<S, A> = (state, action): S => {
                const newState = reducer(state, action);

                Sentry.addBreadcrumb({
                    category: 'redux.action',
                    data: action,
                    type: 'info',
                });

                return newState;
            };

            return next(sentryReducer, initialState);
        };
};
