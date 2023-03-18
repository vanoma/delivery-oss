import { createApi } from '@reduxjs/toolkit/query/react';
import {
    Contact,
    Address,
    Customer,
    Package,
    DeliveryOrder,
    BusinessHour,
    ReverseGeocode,
    Coordinates,
    Bill,
    Driver,
    DeliveryPricing,
    DeliveryOrderPricing,
    Branch,
    Agent,
} from '@vanoma/types';
import {
    GetDeliveryPricingRequest,
    CreateContactRequest,
    UpdateContactRequest,
    CreateAddressRequest,
    GetPaymentMethodsRequest,
    CreatePaymentMethodRequest,
    ListOf,
    SendOTPRequest,
    SendOTPResponse,
    SignInRequest,
    SignInResponse,
    SignUpRequest,
    GetContactsRequest,
    GetContactsResponse,
    GetAddressesRequest,
    GetAddressesResponse,
    NewPackage,
    GetPackagesRequest,
    InvokeDeliveryOrderPaymentRequest,
    InvokeDeliveryOrderPaymentResponse,
    GetPaymentStatusResponse,
    DefaultData,
    GetDeliverySpendingRequest,
    CancelPackageRequest,
    DeleteAddressRequest,
    InvokeBulkPaymentResponse,
    InvokeBulkPaymentRequest,
    CreateDeliveryLinkRequest,
    CreateDeliveryLinkResponse,
    GetLastUsedNoteResponse,
    GetLastUsedNoteRequest,
    CreateBranchRequest,
    GetDefaultDataRequest,
    CreateDeliveryOrderRequest,
    CreateAgentRequest,
    UpdateAgentRequest,
    UpdateBranchRequest,
    GetBillingStatusResponse,
    PaymentMethod,
} from '../types';
import { reauthBaseQuery } from './baseQueries';

const TAGS = {
    ADDRESS: 'ADDRESS',
    CONTACT: 'CONTACT',
    PAYMENT_METHOD: 'PAYMENT_METHOD',
    PACKAGE: 'PACKAGE',
    AGENT: 'AGENT',
    DEFAULT_DATA: 'DEFAULT_DATA',
};

const queryParams = (params: { [x: string]: any }): string =>
    Object.keys(params)
        .filter((key) => !!params[key])
        .map((key) => {
            const value = params[key];
            if (Array.isArray(value)) {
                return `${key}=${value.join(',')}`;
            }
            return `${key}=${value}`;
        })
        .join('&');

const jsonPatchBody = (data: { [x: string]: any }): { [x: string]: any } =>
    Object.entries(data).map(([key, value]) => ({
        path: `/${key}`,
        op: 'replace',
        value,
    }));

export const api = createApi({
    reducerPath: 'api',
    baseQuery: reauthBaseQuery({ baseUrl: process.env.API_URL! }),
    tagTypes: Object.values(TAGS),
    endpoints: (builder) => ({
        /* Acount endpoints */

        sendOtp: builder.mutation<SendOTPResponse, SendOTPRequest>({
            query: (data) => ({
                url: '/otp',
                method: 'POST',
                data,
            }),
        }),
        signIn: builder.mutation<SignInResponse, SignInRequest>({
            query: (data) => ({
                url: '/sign-in',
                method: 'POST',
                data,
            }),
        }),
        signUp: builder.mutation<Customer, SignUpRequest>({
            query: (data) => ({
                url: '/customers',
                method: 'POST',
                data,
            }),
        }),
        signOut: builder.mutation<void, string>({
            query: (userId) => ({
                url: `/sign-out`,
                method: 'POST',
                data: { userId },
            }),
        }),
        getDefaultData: builder.query<DefaultData, GetDefaultDataRequest>({
            queryFn: async (
                { customerId, agentId },
                queryApi,
                extraOptions,
                baseQuery
            ) => {
                try {
                    const result = await Promise.all([
                        baseQuery(`/customers/${customerId}`),
                        baseQuery(
                            `/v1/users/${customerId}/payment-methods?isDefault=true`
                        ),
                        baseQuery(
                            `/customers/${customerId}/contacts?isDefault=true`
                        ),
                        baseQuery(
                            `/customers/${customerId}/addresses?isDefault=true`
                        ),
                        baseQuery(`/agents/${agentId}`),
                        baseQuery(`/customers/${customerId}/branches`),
                    ]);

                    if (result.some((item) => item.error)) {
                        // Because baseQuery does not reject promises, the code in our catch clause
                        // below may never get executed when the API returns non-200 response. We have
                        // to manually inspect the results to make sure we don't have an error.
                        return {
                            error: result.filter((item) => item.error)[0]
                                .error as string,
                        };
                    }

                    return {
                        data: {
                            customer: result[0].data as Customer,
                            paymentMethod: (
                                result[1].data as ListOf<PaymentMethod>
                            ).results[0],
                            contact:
                                (result[2].data as GetContactsResponse)
                                    .contacts[0] ?? null,
                            address:
                                (result[3].data as GetAddressesResponse)
                                    .addresses[0] ?? null,
                            agent: result[4].data as Agent,
                            branches: (result[5].data as ListOf<Branch>)
                                .results,
                        },
                    };
                } catch (error) {
                    return { error: error as string };
                }
            },
            providesTags: [TAGS.DEFAULT_DATA],
        }),

        getCustomers: builder.query<ListOf<Customer>, string[]>({
            query: (customerIds) =>
                `/customers?${queryParams({ customerId: customerIds })}`,
        }),

        /* Pricing endpoints */

        invokeDeliveryPricing: builder.mutation<
            DeliveryPricing,
            GetDeliveryPricingRequest
        >({
            query: (data) => ({
                url: '/delivery-pricing',
                method: 'POST',
                data,
            }),
        }),
        invokeDeliveryOrderPricing: builder.mutation<
            DeliveryOrderPricing,
            string
        >({
            query: (deliveryOrderId) => ({
                url: `/delivery-orders/${deliveryOrderId}/pricing`,
                method: 'POST',
            }),
        }),

        /* Contact endpoints */

        getContacts: builder.query<GetContactsResponse, GetContactsRequest>({
            query: ({ customerId, ...others }) =>
                `/customers/${customerId}/contacts?${queryParams(others)}`,
            providesTags: [TAGS.CONTACT],
        }),
        createContact: builder.mutation<Contact, CreateContactRequest>({
            query: ({ customerId, ...data }) => ({
                url: `/customers/${customerId}/contacts`,
                method: 'POST',
                data,
            }),
            invalidatesTags: [TAGS.CONTACT],
        }),
        updateContact: builder.mutation<Contact, UpdateContactRequest>({
            query: ({ contactId, ...data }) => ({
                url: `/contacts/${contactId}`,
                method: 'PATCH',
                data: jsonPatchBody(data),
                headers: { 'Content-Type': 'application/json-patch+json' },
            }),
            invalidatesTags: [TAGS.CONTACT],
        }),
        deleteContact: builder.mutation<void, string>({
            query: (contactId) => ({
                url: `/contacts/${contactId}/removal`,
                method: 'POST',
                data: { contactId },
            }),
            invalidatesTags: [TAGS.CONTACT],
        }),

        /* Address endpoints */

        getAddresses: builder.query<GetAddressesResponse, GetAddressesRequest>({
            query: ({ contactId, ...others }) =>
                `/contacts/${contactId}/addresses?${queryParams(others)}`,
            providesTags: (_result, _error, arg) => [
                { type: TAGS.ADDRESS, id: arg.contactId },
            ],
        }),
        createAddress: builder.mutation<Address, CreateAddressRequest>({
            query: ({ contactId, ...data }) => ({
                url: `/contacts/${contactId}/addresses`,
                method: 'POST',
                data,
            }),
            invalidatesTags: (_result, _error, arg) => [
                { type: TAGS.ADDRESS, id: arg.contactId },
            ],
        }),
        deleteAddress: builder.mutation<void, DeleteAddressRequest>({
            query: ({ contactId, addressId }) => ({
                url: `/addresses/${addressId}/removal`,
                method: 'POST',
                data: { contactId },
            }),
            invalidatesTags: (_result, _error, arg) => [
                { type: TAGS.ADDRESS, id: arg.contactId },
            ],
        }),
        reverseGeocode: builder.mutation<ReverseGeocode, Coordinates>({
            query: (data) => ({
                url: '/maps/reverse-geocode',
                method: 'POST',
                data,
            }),
        }),

        /* Payment method endpoints */

        getPaymentMethods: builder.query<
            ListOf<PaymentMethod>,
            GetPaymentMethodsRequest
        >({
            query: ({ customerId }) =>
                `/v1/users/${customerId}/payment-methods?size=1000`,
            providesTags: [TAGS.PAYMENT_METHOD],
        }),
        createPaymentMethod: builder.mutation<
            PaymentMethod,
            CreatePaymentMethodRequest
        >({
            query: ({ customerId, ...data }) => ({
                url: `/v1/users/${customerId}/payment-methods`,
                method: 'POST',
                data,
            }),
            invalidatesTags: [TAGS.PAYMENT_METHOD],
        }),
        deletePaymentMethod: builder.mutation<void, string>({
            query: (paymentMethodId) => ({
                url: `/v1/payment-methods/${paymentMethodId}`,
                method: 'DELETE',
            }),
            invalidatesTags: [TAGS.PAYMENT_METHOD],
        }),

        /* Delivery order endpoints */

        createDeliveryOrder: builder.mutation<
            DeliveryOrder,
            CreateDeliveryOrderRequest
        >({
            query: ({ customerId, ...data }) => ({
                url: `/customers/${customerId}/delivery-orders`,
                method: 'POST',
                data,
            }),
        }),
        placeDeliveryOrder: builder.mutation<DeliveryOrder, string>({
            query: (deliveryOrderId) => ({
                url: `/delivery-orders/${deliveryOrderId}/placement`,
                method: 'POST',
            }),
            invalidatesTags: [TAGS.PACKAGE],
        }),
        createDeliveryLink: builder.mutation<
            CreateDeliveryLinkResponse,
            CreateDeliveryLinkRequest
        >({
            query: ({ customerId, ...data }) => ({
                url: `/customers/${customerId}/delivery-requests`,
                method: 'POST',
                data,
            }),
        }),

        /* Package endpoints */

        getPackages: builder.query<ListOf<Package>, GetPackagesRequest>({
            query: ({ customerId, ...others }) =>
                `/customers/${customerId}/packages?${queryParams(others)}`,
            providesTags: [TAGS.PACKAGE],
        }),
        createPackage: builder.mutation<Package, NewPackage>({
            query: ({ deliveryOrderId, ...data }) => ({
                url: `/delivery-orders/${deliveryOrderId}/packages`,
                method: 'POST',
                data,
            }),
        }),
        updatePackage: builder.mutation<Package, NewPackage>({
            query: ({ packageId, ...data }) => ({
                url: `/packages/${packageId}`,
                method: 'PATCH',
                data,
            }),
        }),
        deletePackage: builder.mutation<void, string>({
            query: (packageId) => ({
                url: `/packages/${packageId}`,
                method: 'DELETE',
            }),
        }),
        cancelPackage: builder.mutation<void, CancelPackageRequest>({
            query: ({ packageId, note }) => ({
                url: `/packages/${packageId}/cancellation`,
                method: 'POST',
                data: { note },
            }),
            invalidatesTags: [TAGS.PACKAGE],
        }),

        /* Payment endpoints */

        invokeDeliveryOrderPayment: builder.mutation<
            InvokeDeliveryOrderPaymentResponse,
            InvokeDeliveryOrderPaymentRequest
        >({
            query: ({ deliveryOrderId, ...data }) => ({
                url: `/delivery-orders/${deliveryOrderId}/payment-requests`,
                method: 'POST',
                data,
            }),
        }),
        getPaymentStatus: builder.query<GetPaymentStatusResponse, string>({
            query: (paymentRequestId) =>
                `/delivery-payment-requests/${paymentRequestId}/payment-status`,
        }),
        getDeliverySpending: builder.query<Bill, GetDeliverySpendingRequest>({
            query: ({ customerId, ...others }) =>
                `/customers/${customerId}/delivery-spending?${queryParams(
                    others
                )}`,
        }),
        invokeBulkPayment: builder.mutation<
            InvokeBulkPaymentResponse,
            InvokeBulkPaymentRequest
        >({
            query: ({ customerId, ...data }) => ({
                url: `/customers/${customerId}/delivery-payment-requests`,
                method: 'POST',
                data,
            }),
        }),
        getBillingStatus: builder.query<GetBillingStatusResponse, string>({
            query: (customerId) => `/customers/${customerId}/billing-status`,
        }),

        /* Miscellaneous endpoints */

        getBusinessHours: builder.query<BusinessHour[], void>({
            query: () => '/business-hours',
        }),
        getLastUsedNote: builder.query<
            GetLastUsedNoteResponse,
            GetLastUsedNoteRequest
        >({
            query: ({ contactId, addressId }) =>
                `/contact-addresses?contactId=${contactId}&addressId=${addressId}`,
        }),
        getDriver: builder.query<Driver, string>({
            query: (driverId) => `/drivers2/${driverId}`,
        }),

        /* Branch endpoints */

        createBranch: builder.mutation<Branch, CreateBranchRequest>({
            query: ({ customerId, ...data }) => ({
                url: `/customers/${customerId}/branches`,
                method: 'POST',
                data,
            }),
            invalidatesTags: [TAGS.AGENT, TAGS.DEFAULT_DATA],
        }),
        updateBranch: builder.mutation<Branch, UpdateBranchRequest>({
            query: ({ branchId, ...data }) => ({
                url: `/branches/${branchId}`,
                method: 'PATCH',
                data,
            }),
            invalidatesTags: [TAGS.AGENT, TAGS.DEFAULT_DATA],
        }),
        deleteBranch: builder.mutation<void, string>({
            query: (branchId) => ({
                url: `/branches/${branchId}`,
                method: 'DELETE',
            }),
            invalidatesTags: [TAGS.AGENT, TAGS.DEFAULT_DATA],
        }),

        /* Agents endpoints */

        getAgents: builder.query<ListOf<Agent>, string>({
            query: (customerId) => `/customers/${customerId}/agents`,
            providesTags: [TAGS.AGENT],
        }),
        createAgent: builder.mutation<Agent, CreateAgentRequest>({
            query: ({ customerId, ...data }) => ({
                url: `/customers/${customerId}/agents`,
                method: 'POST',
                data,
            }),
            invalidatesTags: [TAGS.AGENT],
        }),
        updateAgent: builder.mutation<Agent, UpdateAgentRequest>({
            query: ({ agentId, ...data }) => ({
                url: `/agents/${agentId}`,
                method: 'PATCH',
                data,
            }),
            invalidatesTags: [TAGS.AGENT, TAGS.DEFAULT_DATA],
        }),
        deleteAgent: builder.mutation<void, string>({
            query: (agentId) => ({
                url: `/agents/${agentId}`,
                method: 'DELETE',
            }),
            invalidatesTags: [TAGS.AGENT],
        }),
    }),
});

export const {
    useInvokeDeliveryPricingMutation,
    useGetPaymentMethodsQuery,
    useCreatePaymentMethodMutation,
    useDeletePaymentMethodMutation,
    useGetBusinessHoursQuery,
    useGetContactsQuery,
    useCreateContactMutation,
    useUpdateContactMutation,
    useGetAddressesQuery,
    useCreateAddressMutation,
    useReverseGeocodeMutation,
    useGetPaymentStatusQuery,
    useGetDeliverySpendingQuery,
    useCancelPackageMutation,
    useDeleteContactMutation,
    useDeleteAddressMutation,
    useInvokeBulkPaymentMutation,
    useCreateDeliveryLinkMutation,
    useInvokeDeliveryOrderPaymentMutation,
    useGetDriverQuery,
    useGetPackagesQuery,
    useCreateBranchMutation,
    useUpdateBranchMutation,
    useDeleteBranchMutation,
    useGetAgentsQuery,
    useCreateAgentMutation,
    useUpdateAgentMutation,
    useDeleteAgentMutation,
    useGetCustomersQuery,
    useGetBillingStatusQuery,
} = api;
