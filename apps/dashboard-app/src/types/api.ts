/**
 * Types used as request or response of the API calls. These are different from types
 * declared in {@link ./data.ts} in a sense that types defined here are customized to
 * match the expected input or output on an edpoint. Types defined in {@link ./data.ts}
 * are on the other hand more generic; an endpoint for example can return a type from
 * this file which wraps a type from {@link ./data.ts}. A prime example is {@link GetContactsResponse}
 * */

import {
    PackageStatus,
    Contact,
    Address,
    Package,
    Coordinates,
    PaymentStatus,
    Agent,
    Branch,
} from '@vanoma/types';
import { PaymentMethod } from './data';
import { DeliveryLinkPackage } from './derivatives';
import { Account } from './redux';

export interface SendOTPRequest {
    phoneNumber: string;
}

export interface SendOTPResponse {
    otpId: string;
}

export interface SignInRequest {
    verificationCode: string;
    verificationId: string;
    phoneNumber: string;
}

export interface SignInResponse {
    accessToken: string;
    userId: string;
    accounts: Account[];
}

export interface SignUpRequest {
    otpCode: string;
    otpId: string;
    phoneNumber: string;
    businessName: string;
}

export interface GetDeliveryPricingRequest {
    packages: {
        volume: number;
        origin: Coordinates;
        destination: Coordinates;
    }[];
}

export interface GetContactsRequest {
    customerId: string;
    isDefault?: boolean;
}

export interface GetContactsResponse {
    totalCount: number;
    contacts: Array<Contact>;
}

export type CreateContactRequest = Pick<
    Contact,
    'customerId' | 'phoneNumberOne'
> &
    Partial<Omit<Contact, 'contactId' | 'customerId' | 'phoneNumberOne'>>;

export type UpdateContactRequest = Pick<Contact, 'contactId'> &
    Partial<Omit<Contact, 'customerId' | 'contactId'>>;

export interface GetAddressesRequest {
    contactId: string;
    isDefault?: boolean;
}

export interface GetAddressesResponse {
    totalCount: number;
    addresses: Array<Address>;
}

export type CreateAddressRequest = Omit<Address, 'addressId' | 'isDefault'> &
    Partial<Pick<Address, 'isDefault'>> & { contactId: string };

export type GetPaymentMethodsRequest = {
    customerId: string;
};

export type CreatePaymentMethodRequest = Pick<
    PaymentMethod,
    'phoneNumber' | 'type'
> &
    Partial<Pick<PaymentMethod, 'isDefault' | 'shortCode'>> & {
        customerId: string;
    };

export interface ListOf<T> {
    count: number;
    next: string | null;
    previous: string | null;
    results: Array<T>;
}

export interface GetPackagesRequest {
    customerId: string;
    status: PackageStatus | PackageStatus[];
    page: number;
    size: number;
    branchId?: string;
}
export interface GetPackagesResponse {
    results: ListOf<Package>;
    count: number;
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
    branchId?: string;
}
export interface CancelPackageRequest {
    packageId: string;
    note: string;
}

export interface DeleteAddressRequest {
    contactId: string;
    addressId: string;
}

export interface InvokeBulkPaymentRequest {
    customerId: string;
    paymentMethodId: string;
    totalAmount: string;
    endAt?: string;
    branchId?: string;
}

export interface InvokeBulkPaymentResponse {
    paymentRequestId: string;
}

export interface CreateDeliveryLinkRequest {
    customerId: string;
    isCustomerPaying: boolean;
    // TODO: We should use NewPackage here since the endpoint will create a regulard delivery order anywhere.
    // TODO: We need to figure out a way the "delivery link" components can be synchronized with "new delivery" components.
    packages: DeliveryLinkPackage[];
    agentId: string;
}

export interface CreateDeliveryLinkResponse {
    deliveryOrderId: string;
    deliveryLink: string;
}

export interface GetLastUsedNoteResponse {
    addressId: string;
    lastNote: string | null;
}

export interface GetLastUsedNoteRequest {
    contactId: string;
    addressId: string;
}

export interface CreateBranchRequest {
    customerId: string;
    branchName: string;
    contactId: string;
    addressId: string;
}

export interface GetDefaultDataRequest {
    customerId: string;
    agentId: string;
}

export interface CreateDeliveryOrderRequest {
    customerId: string;
    agentId: string;
}

export interface CreateAgentRequest {
    customerId: string;
    fullName: string;
    phoneNumber: string;
    branchId: string | null;
}

export type UpdateAgentRequest = Pick<Agent, 'agentId' | 'fullName'> & {
    branchId: string | null;
};

export type UpdateBranchRequest = Pick<Branch, 'branchId' | 'branchName'> & {
    contactId: string;
    addressId: string;
};

export interface GetBillingStatusResponse {
    isBillDue: boolean;
    gracePeriod: number;
}
