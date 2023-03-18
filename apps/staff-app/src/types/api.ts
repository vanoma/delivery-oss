/**
 * Types used as request or response of the API calls. These are different from types
 * declared in {@link ./data.ts} in a sense that types defined here are customized to
 * match the expected input or output on an edpoint. Types defined in {@link ./data.ts}
 * are on the other hand more generic; an endpoint for example can return a type from
 * this file which wraps a type from {@link ./data.ts}. A prime example is {@link GetContactsResponse}
 * */

import {
    PaymentMethod,
    PaymentStatus,
    PackageStatus,
    Coordinates,
    DriverStatus,
} from '@vanoma/types';

export interface SendOTPRequest {
    phoneNumber: string;
}

export interface SendOTPResponse {
    otpId: string;
}

export interface SignInRequest {
    phoneNumber: string;
    password: string;
}

export interface SignInResponse {
    accessToken: string;
    userId: string;
    staffId: string;
}

export interface SignUpRequest {
    otpCode: string;
    otpId: string;
    phoneNumber: string;
    firstName: string;
    lastName: string;
    password: string;
}

export interface SignUpResponse {
    accessToken: string;
    userId: string;
    staffId: string;
}

export interface GetPackagesRequest {
    page: number;
    size: number;
    status?: PackageStatus | PackageStatus[];
    phoneNumber?: string;
    trackingNumber?: string;
    paymentStatus?: PaymentStatus.PAID | PaymentStatus.UNPAID;
}

export interface InvokeDeliveryOrderPaymentRequest {
    deliveryOrderId: string;
    paymentMethodId: string;
}

export interface InvokeDeliveryOrderPaymentResponse {
    paymentRequestId: string;
}

export interface GetPaymentStatusResponse {
    paymentStatus: PaymentStatus;
}

export interface GetDeliverySpendingRequest {
    customerId: string;
    endAt?: string;
}

export interface CancelPackageRequest {
    packageId: string;
    note: string;
}

export type CreatePaymentMethodRequest<M> = Pick<
    PaymentMethod<M>,
    'extra' | 'type'
> &
    Partial<Pick<PaymentMethod, 'isDefault'>> & { customerId: string };

export interface CancelDeliveryRequest {
    packageId: string;
    staffNote: string;
}

export interface UpdatePackageRequest {
    packageId: string;
    staffNote?: string;
    pickUpStart?: string;
    isAssignable?: boolean;
    pickUpChangeNote?: string;
    fromNote?: string;
    toNote?: string;
    enableNotifications?: boolean;
}

export interface GetDeliveryPricingRequest {
    packages: {
        volume: number;
        origin: Coordinates;
        destination: Coordinates;
    }[];
}

export interface GetDeliveryPricingResponse {
    totalAmount: number;
}

export interface ConfirmPaymentRequest {
    deliveryOrderId: string;
    paymentMethodId: string;
    totalAmount: number;
    operatorTransactionId: string;
    description: string;
    paymentTime: string;
}

export interface CreateAssignmentRequest {
    driverId: string;
    packageId: string;
}

export interface DuplicateDeliveryOrderRequest {
    deliveryOrderId: string;
    pickUpStart: string;
}

export interface GetDriversRequest {
    status: DriverStatus[];
    sort?: string;
}

export interface GetAssignmentsRequest {
    assignmentId?: string[];
    packageId?: string;
}
export interface GetCustomersRequest {
    page: number;
    size: number;
    sort: string;
    businessName?: string;
    phoneNumber?: string;
}
export interface UpdateCustomerRequest {
    customerId: string;
    weightingFactor: number;
    billingInterval: number;
    billingGracePeriod: number;
    postpaidExpiry: string | null;
    fixedPriceAmount: number | null;
    fixedPriceExpiry: string | null;
}
