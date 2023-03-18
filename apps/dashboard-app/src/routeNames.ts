import { PackageStatus } from '@vanoma/types';

export const ROOT = '/';
export const OVERVIEW = '/overview';
export const CUSTOMERS = '/customers';

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
    NEW: 'new',
    REQUEST: PackageStatus.REQUEST.toLowerCase(),
    ACTIVE: 'active',
    COMPLETE: PackageStatus.COMPLETE.toLowerCase(),
};

export const ACCOUNT = '/account';
export const ACCOUNT_TAB = {
    AGENTS: 'agents',
    BRANCHES: 'branches',
};
