import { PackageStatus } from '@vanoma/types';

export const ROOT = '/';
export const OVERVIEW = '/overview';
export const CUSTOMERS = '/customers';
export const CHECK_PRICE = '/check-price';
export const VERIFICATIONS = '/verifications';

export const AUTH = '/auth';
export const SIGN_UP = 'sign-up';
export const SIGN_IN = 'sign-in';

export const BILLING = '/billing';
export const BILLING_TAB = {
    PAY_BALANCE: 'pay-balance',
    PAYMENT_METHODS: 'payment-methods',
};

export const DELIVERIES = '/deliveries';
export const DELIVERIES_TAB = {
    REQUEST: PackageStatus.REQUEST.toLowerCase(),
    DRAFT: 'draft',
    PENDING: PackageStatus.PENDING.toLowerCase(),
    ACTIVE: 'active',
    COMPLETE: PackageStatus.COMPLETE.toLowerCase(),
    SEARCH: 'search',
};
