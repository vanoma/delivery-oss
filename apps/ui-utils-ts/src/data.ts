/**
 * API Data types. Types in this file must conform to the responses returned from the API.
 * */

import {
    PackageSize,
    PackageStatus,
    DeliveryOrderStatus,
    PaymentStatus,
    PaymentMethodType,
    DriverStatus,
    PackageEventName,
    AssignmentStatus,
    ClientType,
    StaffStatus,
    TaskType,
} from './generic';

export interface Customer {
    customerId: string;
    businessName: string;
    phoneNumber: string;
    weightingFactor: number;
    isPrepaid: boolean;
    billingInterval: number;
    billingGracePeriod: number;
    postpaidExpiry: string | null;
    fixedPriceAmount: number | null;
    fixedPriceExpiry: string | null;
    hasFixedPrice: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface Contact {
    contactId: string;
    customerId: string;
    phoneNumberOne: string;
    isDefault: boolean;
    phoneNumberTwo: string | null;
    name: string | null;
}

export interface Address {
    addressId: string;
    latitude: number;
    longitude: number;
    addressName: string;
    district: string;
    isDefault: boolean;
    streetName: string | null;
    houseNumber: string | null;
    placeName: string | null;
}

export interface MobileMoney {
    phoneNumber: string;
    shortCode: string | null;
}

export interface PaymentMethod<T = MobileMoney> {
    paymentMethodId: string;
    type: PaymentMethodType;
    isDefault: boolean;
    extra: T;
}

export interface PackageEvent {
    packageEventId: string;
    textEN: string;
    textFR: string;
    textRW: string;
    createdAt: string;
    eventName: PackageEventName;
}

// NOTE: Parameterizing contact and address to retain flexibility on which attributes of either type to include.
export interface Package<C = Contact, A = Address> {
    packageId: string;
    size: PackageSize;
    status: PackageStatus;
    fromContact: C;
    toContact: C;
    fromAddress: A;
    toAddress: A;
    fromNote: string | null;
    toNote: string | null;
    pickUpStart: string | null;
    pickUpEnd: string | null;
    trackingNumber: string;
    trackingLink: string;
    driverId: string | null;
    assignmentId: string | null;
    paymentStatus: PaymentStatus;
    totalAmount: number;
    deliveryOrder: DeliveryOrder;
    createdAt: string;
    updatedAt: string;
    events: PackageEvent[];
    staffNote: string;
    isAssignable: boolean;
    enableNotifications: boolean;
    isExpress: boolean;
}

export interface Branch {
    branchId: string;
    branchName: string;
    contact: Contact;
    address: Address;
    createdAt: string;
    updatedAt: string;
}

export interface DeliveryOrder {
    deliveryOrderId: string;
    status: DeliveryOrderStatus;
    placedAt: string;
    linkOpenedAt: string | null;
    deliveryLink: string;
    isCustomerPaying: boolean;
    customerId: string;
    customer: Customer;
    branch: Branch | null;
    agent: Agent | null;
    clientType: ClientType;
}

export interface BusinessHour {
    weekDay: number;
    isDayOff: boolean;
    openAt: string;
    closeAt: string;
}

export type ReverseGeocode = Omit<
    Address,
    'addressId' | 'isDefault' | 'addressName'
>;

export interface Bill {
    transactionFee: number;
    transactionAmount: number;
    totalAmount: number;
    deliverOrders: DeliveryOrder[];
    totalCount: number;
}

export interface DriverLocation {
    locationId: string;
    isAssigned: boolean;
    latitude: number;
    longitude: number;
    createdAt: string;
    updatedAt: string;
    batteryLevel: number;
    isGpsEnabled: boolean;
    isLocationServiceEnabled: boolean;
    locationAccessStatus: 'ALLOWED_ALWAYS' | 'ALLOWED_WHEN_IN_USE' | 'DENIED';
}

export interface Driver {
    driverId: string;
    firstName: string;
    lastName: string;
    phoneNumber: string;
    secondPhoneNumber: string;
    latestLocation?: DriverLocation;
    status: DriverStatus;
    isAvailable: boolean;
    assignmentCount: number;
}

export interface DeliveryPricing {
    isPrepaid: boolean;
    totalAmount: number;
}

export interface DeliveryOrderPricing {
    isPrepaid: boolean;
    totalAmount: number;
}

export interface OTP {
    otpId: string;
    otpCode: string;
    phoneNumber: string;
    createdAt: string;
    updatedAt: string;
}

export interface Staff {
    staffId: string;
    firstName: string;
    lastName: string;
    phoneNumber: string;
    status: StaffStatus;
    createdAt: string;
    updatedAt: string;
}

export interface Task {
    taskId: string;
    type: TaskType;
    stop: Stop;
    completedAt: string | null;
    createdAt: string;
    updatedAt: string;
}
export type CurrentTask = Omit<Task, 'stop'> & { package: Package };
export interface Stop {
    stopId: string;
    completedAt: string | null;
    departBy: string | null;
    departedAt: string | null;
    arriveBy: string | null;
    arrivedAt: string | null;
}
export type CurrentStop = Stop & { currentTasks: CurrentTask[] };

export interface Assignment {
    assignmentId: string;
    driver: Driver;
    confirmationLocation: DriverLocation;
    packageId: string;
    status: AssignmentStatus;
    tasks: Task[];
    confirmedAt: string;
    createdAt: string;
    updatedAt: string;
}

export interface Agent {
    agentId: string;
    fullName: string;
    phoneNumber: string;
    branch: Branch | null;
    isRoot: boolean;
    createdAt: string;
    updatedAt: string;
}
