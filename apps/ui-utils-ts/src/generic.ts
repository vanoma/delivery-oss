/* eslint-disable no-unused-vars */
/* eslint-disable no-shadow */

import { PackageEvent } from './data';

/**
 * Generic types that are used across apps.
 * */

export interface Coordinates {
    latitude: number;
    longitude: number;
}

export enum PackageSize {
    SMALL = 'SMALL',
    MEDIUM = 'MEDIUM',
    LARGE = 'LARGE',
}

export enum DeliveryOrderStatus {
    REQUEST = 'REQUEST',
    PENDING = 'PENDING',
    STARTED = 'STARTED',
    PLACED = 'PLACED',
    COMPLETE = 'COMPLETE',
}

export enum PackageStatus {
    REQUEST = 'REQUEST',
    PENDING = 'PENDING',
    STARTED = 'STARTED',
    PLACED = 'PLACED',
    COMPLETE = 'COMPLETE',
    CANCELED = 'CANCELED',
    INCOMPLETE = 'INCOMPLETE', // Packages cancelled prior to being placed.
}

export enum PaymentStatus {
    NO_CHARGE = 'NO_CHARGE',
    UNPAID = 'UNPAID',
    PARTIAL = 'PARTIAL',
    PAID = 'PAID',
}

export enum PaymentMethodType {
    MOBILE_MONEY = 'MOBILE_MONEY',
}

export enum DriverStatus {
    ACTIVE = 'ACTIVE',
    PENDING = 'PENDING',
    INACTIVE = 'INACTIVE',
}

export enum PackageEventName {
    ORDER_PLACED = 'ORDER_PLACED',
    DRIVER_ASSIGNED = 'DRIVER_ASSIGNED',
    DRIVER_CONFIRMED = 'DRIVER_CONFIRMED',
    DRIVER_DEPARTING_PICK_UP = 'DRIVER_DEPARTING_PICK_UP',
    DRIVER_ARRIVED_PICK_UP = 'DRIVER_ARRIVED_PICK_UP',
    PACKAGE_PICKED_UP = 'PACKAGE_PICKED_UP',
    DRIVER_DEPARTING_DROP_OFF = 'DRIVER_DEPARTING_DROP_OFF',
    DRIVER_ARRIVED_DROP_OFF = 'DRIVER_ARRIVED_DROP_OFF',
    PACKAGE_DELIVERED = 'PACKAGE_DELIVERED',
    PACKAGE_CANCELLED = 'PACKAGE_CANCELLED',
}

export enum DeliveryRequestStep {
    SMS_SENT = 'SMS_SENT',
    SMS_OPENED = 'SMS_OPENED',
    ADDRESS = 'ADDRESS',
    PAYMENT = 'PAYMENT',
    DRIVER = 'DRIVER',
}

export interface ListOf<T> {
    count: number;
    next: string | null;
    previous: string | null;
    results: Array<T>;
}

export enum AssignmentStatus {
    PENDING = 'PENDING',
    CONFIRMED = 'CONFIRMED',
    COMPLETED = 'COMPLETED',
    EXPIRED = 'EXPIRED',
    CANCELED = 'CANCELED',
}

export enum ClientType {
    API = 'API',
    WEB_APP = 'WEB_APP',
    DELIVERY_LINK = 'DELIVERY_LINK',
}

export enum StaffStatus {
    ACTIVE = 'ACTIVE',
    PENDING = 'PENDING',
    INACTIVE = 'INACTIVE',
}
export type PackageEventsMap = {
    // eslint-disable-next-line no-unused-vars
    [x in PackageEventName]?: PackageEvent;
};

export enum TaskType {
    PICK_UP = 'PICK_UP',
    DROP_OFF = 'DROP_OFF',
}
