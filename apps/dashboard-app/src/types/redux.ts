/**
 *  Types that are used in redux state and related logic.
 */

import { Customer, Contact, Address, Agent, Branch } from '@vanoma/types';
import { PaymentMethod } from './data';

export interface RootState {}

export interface SliceState {
    isLoading: boolean;
    error: string | null;
}

export interface OTPData {
    phoneNumber: string;
    otpId: string;
}

export interface DefaultData {
    customer: Customer;
    paymentMethod: PaymentMethod;
    contact: Contact | null; // Can be null for newly signed up users
    address: Address | null; // Can be null for newly signed up users
    agent: Agent;
    branches: Branch[];
}

export interface AuthenticationState extends SliceState {
    isAuthenticated: boolean | null;
    defaultData: DefaultData | null;
    otpData: OTPData | null;
    accounts: Account[];
}

export interface AuthenticationRootState extends RootState {
    authentication: AuthenticationState;
}

export interface Account {
    agentId: string;
    customerId: string;
}
