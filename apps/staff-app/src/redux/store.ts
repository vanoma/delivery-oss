import { configureStore } from '@reduxjs/toolkit';
import authenticationSlice from './slices/authenticationSlice';
import deliveriesSlice from './slices/deliveriesSlice';
import { api } from '../api';
import notificationsSlice from './slices/notificationsSlice';

export const store = configureStore({
    reducer: {
        authentication: authenticationSlice,
        deliveries: deliveriesSlice,
        notifications: notificationsSlice,
        [api.reducerPath]: api.reducer,
    },
    middleware: (getDefaultMiddleware) =>
        getDefaultMiddleware().concat(api.middleware),
});

export type RootState = ReturnType<typeof store.getState>;

export type AppDispatch = typeof store.dispatch;
