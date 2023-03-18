/* eslint-disable no-console */
import OneSignal from 'react-onesignal';
import { NavigateFunction } from 'react-router-dom';
import { DeliveryOrderStatus } from '@vanoma/types';
import { addNotification } from '../redux/slices/notificationsSlice';
import { store } from '../redux/store';
import { DELIVERIES } from '../routeNames';
import { EventNotification } from '../types';
import storage from './storage';

export const setExternalUserId = (customerId: string | null): void => {
    OneSignal.setExternalUserId(customerId);
    OneSignal.sendTag('user_type', 'customer');
};

const handleEventNotification = (notification: EventNotification): void => {
    if (notification.deliveryOrderId) {
        store.dispatch(addNotification(notification));
    }
};

export const initializeOnesignal = (navigate: NavigateFunction): void => {
    console.log('Initializing OneSignal');
    OneSignal.init({
        appId: process.env.ONESIGNAL_APP_ID!,
    });
    OneSignal.on(
        'notificationDisplay',
        (event: { data: EventNotification }) => {
            console.log('Handling notification display', event);
            // event can contain anything which might not have data. (e.g. test notifications sent from
            // OneSignal dashboard). Notifications from the api will contain the data attribute.
            if (event.data) {
                handleEventNotification(event.data);
            }
        }
    );
    OneSignal.on('notificationPermissionChange', (permissionChange: any) => {
        console.log(
            'Handling notification permission change',
            permissionChange
        );
        const currentPermission = permissionChange.to;
        if (currentPermission === 'granted') {
            const customerId = storage.getItem('customerId');
            setExternalUserId(customerId);
        }
    });
    let count = 0;
    const scrollToEvent = (notification: EventNotification): void => {
        const element = document.getElementById(notification.packageEventId);
        if (element) {
            element.scrollIntoView({ behavior: 'smooth' });
        } else {
            if (count < 10) {
                setTimeout(() => scrollToEvent(notification), 1000);
            }
            count += 1;
        }
    };
    OneSignal.addListenerForNotificationOpened(
        (event: { data: EventNotification }) => {
            console.log('Handling notification opened', event);
            // event can contain anything which might not have data.
            // (e.g. test notifications sent from OneSignal dashboard).
            if (event.data) {
                navigate(
                    `${DELIVERIES}${
                        event.data.eventName !== 'PACKAGE_DELIVERED'
                            ? DeliveryOrderStatus.PLACED.toLowerCase()
                            : DeliveryOrderStatus.COMPLETE.toLowerCase()
                    }#${event.data.packageEventId}`
                );
                handleEventNotification(event.data);
                scrollToEvent(event.data);
            }
        }
    );
};

export const subscribeUser = (): void => {
    OneSignal.showSlidedownPrompt();
};
