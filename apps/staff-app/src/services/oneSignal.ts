/* eslint-disable no-console */
import { DeliveryOrderStatus } from '@vanoma/types';
import OneSignal from 'react-onesignal';
import { NavigateFunction } from 'react-router-dom';
import { addNotification } from '../redux/slices/notificationsSlice';
import { store } from '../redux/store';
import { DELIVERIES } from '../routeNames';
import { EventNotification } from '../types';

export const setExternalUserId = (): void => {
    OneSignal.setExternalUserId(process.env.EXTERNAL_USER_ID);
    OneSignal.sendTag('user_type', 'operator');
};

const handleEventNotification = (notification: EventNotification): void => {
    if (notification.data.deliveryOrderId) {
        store.dispatch(addNotification(notification));
    }
};

export const initializeOnesignal = (navigate: NavigateFunction): void => {
    console.log('Initializing OneSignal');
    OneSignal.init({
        appId: process.env.ONESIGNAL_APP_ID!,
    });
    OneSignal.on('notificationDisplay', (event: EventNotification) => {
        console.log('Handling notification display', event);
        // event can contain anything which might not have data. (e.g. test notifications sent from
        // OneSignal dashboard). Notifications from the api will contain the data attribute.
        if (event.data) {
            handleEventNotification(event);
        }
    });
    OneSignal.on('notificationPermissionChange', (permissionChange: any) => {
        console.log(
            'Handling notification permission change',
            permissionChange
        );
        const currentPermission = permissionChange.to;
        if (currentPermission === 'granted') {
            setExternalUserId();
        }
    });
    let count = 0;
    const scrollToEvent = (notification: EventNotification): void => {
        const element = document.getElementById(notification.data.packageId);
        if (element) {
            element.scrollIntoView({ behavior: 'smooth' });
        } else {
            if (count < 10) {
                setTimeout(() => scrollToEvent(notification), 1000);
            }
            count += 1;
        }
    };
    OneSignal.addListenerForNotificationOpened((event: any) => {
        const notification = event as EventNotification;
        console.log('Handling notification opened', event);
        // event can contain anything which might not have data.
        // (e.g. test notifications sent from OneSignal dashboard).
        if (event.data) {
            navigate(
                `${DELIVERIES}/${DeliveryOrderStatus.REQUEST.toLowerCase()}#${
                    notification.data.packageId
                }`
            );
            handleEventNotification(notification);
            scrollToEvent(event);
        }
    });
};
