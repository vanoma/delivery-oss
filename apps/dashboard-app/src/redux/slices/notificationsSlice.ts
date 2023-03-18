/* eslint-disable no-param-reassign */
import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { EventNotification, RootState } from '../../types';

const sliceName = 'notifications';

const sortNotificationsAndRemoveDuplicate = (
    notifications: EventNotification[]
): EventNotification[] => {
    const sortedNotifications = [...notifications].sort((a, b) => {
        if (new Date(a.createdAt) < new Date(b.createdAt)) {
            return 1;
        }
        if (new Date(a.createdAt) > new Date(b.createdAt)) {
            return -1;
        }
        return 0;
    });
    const sortedUniqueNotifications = sortedNotifications.filter(
        (notification, index) =>
            sortedNotifications.findIndex(
                (notification1) =>
                    notification1.packageEventId === notification.packageEventId
            ) === index
    );
    return sortedUniqueNotifications;
};

interface NotificationsState {
    unreadNotifications: EventNotification[];
    readNotifications: EventNotification[];
}

interface NotificationsRootState extends RootState {
    notifications: NotificationsState;
}

const initialState: NotificationsState = {
    unreadNotifications: [],
    readNotifications: [],
};
const notificationsSlice = createSlice({
    name: sliceName,
    initialState,
    reducers: {
        addNotification: (state, action: PayloadAction<EventNotification>) => {
            if (action.payload.eventName === 'PACKAGE_DELIVERED') {
                state.readNotifications = state.readNotifications.filter(
                    (notification) =>
                        notification.packageId !== action.payload.packageId
                );
                state.unreadNotifications = state.unreadNotifications.filter(
                    (notification) =>
                        notification.packageId !== action.payload.packageId
                );
            }
            state.unreadNotifications = sortNotificationsAndRemoveDuplicate([
                ...state.unreadNotifications,
                {
                    ...action.payload,
                    isRead: false,
                    clickCount: 0,
                },
            ]);
            const index = state.readNotifications.findIndex(
                (notification) =>
                    notification.packageEventId ===
                    action.payload.packageEventId
            );
            if (index !== -1) {
                state.readNotifications.splice(index, 1);
            }
        },
        readNotification: (state, action: PayloadAction<string>) => {
            const index = state.unreadNotifications.findIndex(
                (notification) => notification.packageEventId === action.payload
            );
            state.readNotifications = sortNotificationsAndRemoveDuplicate([
                ...state.readNotifications,
                {
                    ...state.unreadNotifications[index],
                    isRead: true,
                    clickCount: 1,
                },
            ]);

            state.unreadNotifications.splice(index, 1);
        },
        markAllNotificationsAsRead: (state) => {
            state.readNotifications = sortNotificationsAndRemoveDuplicate(
                state.readNotifications.concat(
                    state.unreadNotifications.map((notification) => ({
                        ...notification,
                        isRead: true,
                        clickCount: 1,
                    }))
                )
            );
            state.unreadNotifications = [];
        },
        inclementClickCount: (state, action: PayloadAction<string>) => {
            const index = state.readNotifications.findIndex(
                (notification) => notification.packageEventId === action.payload
            );
            state.readNotifications = sortNotificationsAndRemoveDuplicate([
                {
                    ...state.readNotifications[index],
                    clickCount: state.readNotifications[index].clickCount + 1,
                },
                ...state.readNotifications,
            ]);
        },
    },
});

export const {
    addNotification,
    readNotification,
    markAllNotificationsAsRead,
    inclementClickCount,
} = notificationsSlice.actions;

export const selectUnreadNotifications = (
    state: NotificationsRootState
): EventNotification[] => state.notifications.unreadNotifications;

export const selectReadNotifications = (
    state: NotificationsRootState
): EventNotification[] => state.notifications.readNotifications;

export default notificationsSlice.reducer;
