/* eslint-disable no-param-reassign */
import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { EventNotification, RootState } from '../../types';

const sliceName = 'notifications';

const sortNotificationsAndRemoveDuplicate = (
    notifications: EventNotification[]
): EventNotification[] => {
    const sortedNotifications = [...notifications].sort((a, b) => {
        if (new Date(a.data.createdAt) < new Date(b.data.createdAt)) {
            return 1;
        }
        if (new Date(a.data.createdAt) > new Date(b.data.createdAt)) {
            return -1;
        }
        return 0;
    });
    const sortedUniqueNotifications = sortedNotifications.filter(
        (notification, index) =>
            sortedNotifications.findIndex(
                (notification1) =>
                    notification1.data.packageId === notification.data.packageId
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
            state.unreadNotifications = sortNotificationsAndRemoveDuplicate([
                ...state.unreadNotifications,
                {
                    ...action.payload,
                    data: {
                        ...action.payload.data,
                        isRead: false,
                        clickCount: 0,
                    },
                },
            ]);
            const index = state.readNotifications.findIndex(
                (notification) =>
                    notification.data.packageId ===
                    action.payload.data.packageId
            );
            if (index !== -1) {
                state.readNotifications.splice(index, 1);
            }
        },
        readNotification: (state, action: PayloadAction<string>) => {
            const index = state.unreadNotifications.findIndex(
                (notification) => notification.data.packageId === action.payload
            );
            state.readNotifications = sortNotificationsAndRemoveDuplicate([
                ...state.readNotifications,
                {
                    ...state.unreadNotifications[index],
                    data: {
                        ...state.unreadNotifications[index].data,
                        isRead: true,
                        clickCount: 1,
                    },
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
                (notification) => notification.data.packageId === action.payload
            );
            state.readNotifications = sortNotificationsAndRemoveDuplicate([
                {
                    ...state.readNotifications[index],
                    data: {
                        ...state.readNotifications[index].data,
                        clickCount:
                            state.readNotifications[index].data.clickCount + 1,
                    },
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
