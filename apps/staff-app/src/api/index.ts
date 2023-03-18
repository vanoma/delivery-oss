import { createApi } from '@reduxjs/toolkit/query/react';
import {
    BusinessHour,
    DeliveryOrder,
    Package,
    Driver,
    Customer,
    PaymentMethod,
    ListOf,
    OTP,
    Assignment,
    Staff,
    CurrentStop,
} from '@vanoma/types';
import {
    NewMobileMoney,
    SignInRequest,
    SignInResponse,
    GetPackagesRequest,
    CancelPackageRequest,
    CreatePaymentMethodRequest,
    SendOTPResponse,
    SendOTPRequest,
    SignUpResponse,
    SignUpRequest,
    GetCustomersRequest,
    UpdatePackageRequest,
    GetDeliveryPricingResponse,
    GetDeliveryPricingRequest,
    ConfirmPaymentRequest,
    CreateAssignmentRequest,
    DuplicateDeliveryOrderRequest,
    GetDriversRequest,
    GetAssignmentsRequest,
    UpdateCustomerRequest,
} from '../types';
import { reauthBaseQuery } from './baseQueries';

const TAGS = {
    PAYMENT_METHOD: 'PAYMENT_METHOD',
    CUSTOMER: 'CUSTOMER',
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

export const api = createApi({
    reducerPath: 'api',
    baseQuery: reauthBaseQuery({ baseUrl: process.env.API_URL! }),
    tagTypes: Object.values(TAGS),
    endpoints: (builder) => ({
        /* Account endpoints */

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
        signUp: builder.mutation<SignUpResponse, SignUpRequest>({
            query: (data) => ({
                url: '/staff',
                method: 'POST',
                data,
            }),
        }),
        signOut: builder.mutation<void, string>({
            query: (userId) => ({
                url: '/sign-out',
                method: 'POST',
                data: { userId },
            }),
        }),
        getOtpList: builder.query<ListOf<OTP>, { sort: string }>({
            query: (params) => `/otp?${queryParams(params)}`,
        }),
        getStaff: builder.query<Staff, string>({
            query: (staffId) => `/staff/${staffId}`,
        }),

        /* Payment method endpoints */

        getPaymentMethods: builder.query<ListOf<PaymentMethod>, string>({
            query: (customerId) =>
                `/users/${customerId}/payment-methods?limit=1000`,
            providesTags: [TAGS.PAYMENT_METHOD],
        }),
        createPaymentMethod: builder.mutation<
            PaymentMethod,
            CreatePaymentMethodRequest<NewMobileMoney>
        >({
            query: ({ customerId, ...data }) => ({
                url: `/users/${customerId}/payment-methods`,
                method: 'POST',
                data,
            }),
            invalidatesTags: [TAGS.PAYMENT_METHOD],
        }),

        /* Delivery order endpoints */

        placeDeliveryOrder: builder.mutation<DeliveryOrder, string>({
            query: (deliveryOrderId) => ({
                url: `/delivery-orders/${deliveryOrderId}/placement`,
                method: 'POST',
            }),
        }),
        confirmPayment: builder.mutation<void, ConfirmPaymentRequest>({
            query: ({ deliveryOrderId, ...data }) => ({
                url: `/delivery-orders/${deliveryOrderId}/payment-confirmations`,
                method: 'POST',
                data,
            }),
        }),

        getDeliveryOrderPackages: builder.query<ListOf<Package>, string>({
            query: (deliveryOrderId) =>
                `delivery-orders/${deliveryOrderId}/packages`,
        }),

        /* Package endpoints */

        getPackages: builder.query<ListOf<Package>, GetPackagesRequest>({
            query: (params) => `/packages?${queryParams(params)}`,
        }),
        updatePackage: builder.mutation<Package, UpdatePackageRequest>({
            query: ({ packageId, ...data }) => ({
                url: `/packages/${packageId}`,
                method: 'PATCH',
                data,
            }),
        }),
        cancelPackage: builder.mutation<void, CancelPackageRequest>({
            query: ({ packageId, note }) => ({
                url: `/packages/${packageId}/cancellation`,
                method: 'POST',
                data: { note },
            }),
        }),

        /* Driver endpoints */

        getDrivers: builder.query<ListOf<Driver>, GetDriversRequest>({
            query: (params) => `/drivers2?${queryParams(params)}`,
        }),
        getDriver: builder.query<Driver, string>({
            query: (driverId) => `/drivers2/${driverId}`,
        }),
        getStops: builder.query<ListOf<CurrentStop>, string>({
            query: (driverId) => `/drivers2/${driverId}/current-stops`,
        }),

        /* Assignment endpoints */

        getAssignments: builder.query<
            ListOf<Assignment>,
            GetAssignmentsRequest
        >({
            query: (params) => `/assignments?${queryParams(params)}`,
        }),
        createAssignment: builder.mutation<void, CreateAssignmentRequest>({
            query: (data) => ({
                url: '/assignments',
                method: 'POST',
                data,
            }),
        }),
        cancelAssignment: builder.mutation<void, string>({
            query: (assignmentId) => ({
                url: `/current-assignments/${assignmentId}/cancellation`,
                method: 'POST',
            }),
        }),

        /* Customer endpoints */

        getCustomers: builder.query<ListOf<Customer>, GetCustomersRequest>({
            query: (params) => `/customers?${queryParams(params)}`,
            providesTags: [TAGS.CUSTOMER],
        }),
        updateCustomer: builder.mutation<Customer, UpdateCustomerRequest>({
            query: ({ customerId, ...data }) => ({
                url: `/customers/${customerId}`,
                method: 'PATCH',
                data,
            }),
            invalidatesTags: [TAGS.CUSTOMER],
        }),

        /* Pricing endpoints */

        getDeliveryPricing: builder.mutation<
            GetDeliveryPricingResponse,
            GetDeliveryPricingRequest
        >({
            query: (data) => ({
                url: '/delivery-pricing',
                method: 'POST',
                data,
            }),
        }),

        /* Miscellaneous endpoints */

        getBusinessHours: builder.query<BusinessHour[], void>({
            query: () => '/business-hours',
        }),
        duplicateDeliveryOrder: builder.mutation<
            void,
            DuplicateDeliveryOrderRequest
        >({
            query: ({ deliveryOrderId, ...data }) => ({
                url: `/delivery-orders/${deliveryOrderId}/duplication`,
                method: 'POST',
                data,
            }),
        }),
    }),
});

export const {
    useGetPaymentMethodsQuery,
    useCreatePaymentMethodMutation,
    usePlaceDeliveryOrderMutation,
    useConfirmPaymentMutation,
    useUpdatePackageMutation,
    useCancelPackageMutation,
    useGetDriversQuery,
    useGetDriverQuery,
    useGetDeliveryPricingMutation,
    useGetOtpListQuery,
    useGetBusinessHoursQuery,
    useCreateAssignmentMutation,
    useCancelAssignmentMutation,
    useDuplicateDeliveryOrderMutation,
    useGetStopsQuery,
    useGetDeliveryOrderPackagesQuery,
    useGetCustomersQuery,
    useUpdateCustomerMutation,
    useGetAssignmentsQuery,
} = api;
