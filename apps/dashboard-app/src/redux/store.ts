import { configureStore } from '@reduxjs/toolkit';
import notificationsSlice from './slices/notificationsSlice';
import newDeliverySlice from '../features/NewDelivery/slice';
import authenticationSlice from './slices/authenticationSlice';
import { api } from '../api';
import { createReduxEnhancer } from '../services/sentry';

const sentryReduxEnhancer = createReduxEnhancer();

export const store = configureStore({
    reducer: {
        authentication: authenticationSlice,
        notifications: notificationsSlice,
        newDelivery: newDeliverySlice,
        [api.reducerPath]: api.reducer,
    },
    enhancers: [sentryReduxEnhancer],
    middleware: (getDefaultMiddleware) =>
        getDefaultMiddleware().concat(api.middleware),
});

export type AppDispatch = typeof store.dispatch;
