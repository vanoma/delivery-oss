/* eslint-disable no-param-reassign */
import {
    createSlice,
    createAsyncThunk,
    isAnyOf,
    PayloadAction,
} from '@reduxjs/toolkit';
import { Assignment, PackageStatus, PaymentStatus } from '@vanoma/types';
import {
    SliceState,
    RootState,
    Delivery,
    GetPackagesRequest,
} from '../../types';
import { api } from '../../api';
import { DELIVERIES_TAB } from '../../routeNames';

const SLICE_NAME = 'deliveriesSlice';

export const PAGE_SIZE = 20;

// eslint-disable-next-line no-shadow
export enum QueryType {
    // eslint-disable-next-line no-unused-vars
    PHONE_NUMBER = 'phoneNumber',
    // eslint-disable-next-line no-unused-vars
    TRACKING_NUMBER = 'trackingNumber',
}

type Deliveries = {
    [x: string]: Delivery;
};

export type SearchParams = {
    packageStatus: PackageStatus | null;
    queryType: QueryType | null;
    queryValue: string | null;
};

type PaymentStatusFilter = PaymentStatus.PAID | PaymentStatus.UNPAID | null;

interface GetDeliveriesOutput {
    deliveries: Deliveries;
    totalCount: number;
    currentTab: string;
}

interface DeliveriesState extends SliceState {
    deliveries: Deliveries;
    currentPage: number;
    totalCount: number;
    currentTab: string;
    searchParams: SearchParams | null;
    paymentStatus: PaymentStatusFilter;
}

interface DeliveriesRootState extends RootState {
    deliveries: DeliveriesState;
}

const initialState: DeliveriesState = {
    isLoading: false,
    success: null,
    error: null,
    deliveries: {},
    currentPage: 1,
    totalCount: 0,
    currentTab: DELIVERIES_TAB.ACTIVE,
    searchParams: null,
    paymentStatus: null,
};

const getAssignments = createAsyncThunk<Deliveries | string, string[]>(
    `${SLICE_NAME}/getAssignments`,
    async (assignmentIds, { dispatch, getState, rejectWithValue }) => {
        try {
            const { results } = await dispatch(
                api.endpoints.getAssignments.initiate(
                    {
                        assignmentId: assignmentIds,
                    },
                    {
                        // We can't use caching. Some times components refetches packages and
                        // we should avoid cache.
                        forceRefetch: true,
                    }
                )
            ).unwrap();

            const { deliveries } = (getState() as DeliveriesRootState)
                .deliveries;

            const packageIdToAssignment: { [x: string]: Assignment } = {};
            results.forEach((assignment) => {
                packageIdToAssignment[assignment.packageId] = assignment;
            });

            const merged: Deliveries = {};
            Object.keys(deliveries).forEach((packageId) => {
                merged[packageId] = {
                    ...deliveries[packageId],
                    assignment: packageIdToAssignment[packageId] || null,
                };
            });
            return merged;
        } catch (error) {
            // TODO: Fix auth-api to avoid returning gibberish response when querying for multiple customerIds
            // eslint-disable-next-line no-console
            console.log(error);
            // Error here may not necessary be a string (i.e. auth-api failed response)
            return rejectWithValue(`${error}`);
        }
    }
);

export const getDeliveries = createAsyncThunk<
    GetDeliveriesOutput | string,
    Function | undefined
>(
    `${SLICE_NAME}/getDeliveries`,
    async (callback, { dispatch, getState, rejectWithValue }) => {
        try {
            const { currentTab, currentPage, searchParams, paymentStatus } = (
                getState() as DeliveriesRootState
            ).deliveries;

            const filterParams: GetPackagesRequest = {
                page: currentPage - 1,
                size: PAGE_SIZE,
            };

            if (currentTab && currentTab !== DELIVERIES_TAB.SEARCH) {
                if (currentTab === DELIVERIES_TAB.ACTIVE) {
                    filterParams.status = PackageStatus.PLACED;
                } else if (currentTab === DELIVERIES_TAB.DRAFT) {
                    filterParams.status = PackageStatus.STARTED;
                } else {
                    filterParams.status =
                        currentTab.toUpperCase() as PackageStatus;
                }
            }

            if (searchParams) {
                if (searchParams.packageStatus) {
                    filterParams.status = searchParams.packageStatus;
                }
                if (searchParams.queryType && searchParams.queryValue) {
                    filterParams[searchParams.queryType] =
                        searchParams.queryValue;
                }
            }

            if (paymentStatus) {
                filterParams.paymentStatus = paymentStatus;
            }

            const { results, count: totalCount } = await dispatch(
                api.endpoints.getPackages.initiate(filterParams, {
                    // We can't use caching. Some times components refetches packages and
                    // we should avoid cache.
                    forceRefetch: true,
                })
            ).unwrap();

            const deliveries: Deliveries = {};

            if (results.length > 0) {
                const assignmentIds: string[] = [];

                results.forEach((pkg) => {
                    const { packageId } = pkg;
                    deliveries[packageId] = {
                        package: pkg,
                        assignment: null,
                    };

                    const { assignmentId } = pkg;
                    if (assignmentId != null) {
                        assignmentIds.push(assignmentId);
                    }
                });

                if (assignmentIds.length !== 0) {
                    dispatch(getAssignments(assignmentIds)).then(({ type }) => {
                        if (type.includes('fulfilled') && callback) callback();
                    });
                }
            }

            return { deliveries, totalCount, currentTab };
        } catch (error) {
            return rejectWithValue(error);
        }
    }
);

const listDeliveriesSlice = createSlice({
    name: SLICE_NAME,
    initialState,
    reducers: {
        resetError: (state) => {
            state.error = null;
        },
        setCurrentTab: (state, { payload }: PayloadAction<string>) => {
            state.deliveries = {};
            state.totalCount = 0;
            state.currentTab = payload;
        },
        setCurrentPage: (state, { payload }: PayloadAction<number>) => {
            state.deliveries = {};
            state.totalCount = 0;
            state.currentPage = payload;
        },
        setSearchParams: (
            state,
            { payload }: PayloadAction<SearchParams | null>
        ) => {
            state.deliveries = {};
            state.totalCount = 0;
            state.searchParams = payload;
        },
        setPaymentStatus: (
            state,
            { payload }: PayloadAction<PaymentStatusFilter>
        ) => {
            state.deliveries = {};
            state.totalCount = 0;
            state.paymentStatus = payload;
        },
    },
    extraReducers: (builder) => {
        builder.addCase(getDeliveries.pending, (state) => {
            state.isLoading = true;
            state.error = null; // Always reset previous error if any
        });
        builder.addCase(getDeliveries.fulfilled, (state, { payload }) => {
            const { deliveries, totalCount, currentTab } =
                payload as GetDeliveriesOutput;
            if (currentTab === state.currentTab) {
                state.deliveries = deliveries;
                state.totalCount = totalCount;
                state.isLoading = false;
            }
        });
        builder.addCase(getAssignments.fulfilled, (state, { payload }) => {
            state.deliveries = payload as Deliveries;
            state.isLoading = false;
        });
        builder.addMatcher(
            isAnyOf(getDeliveries.rejected, getAssignments.rejected),
            (state, { payload }) => {
                state.error = payload as string;
                state.isLoading = false;
            }
        );
    },
});

export const { resetError } = listDeliveriesSlice.actions;

export const changeCurrentTab = createAsyncThunk<void, string>(
    `${SLICE_NAME}/changeCurrentTab`,
    (currentTab, { dispatch }) => {
        dispatch(listDeliveriesSlice.actions.setCurrentTab(currentTab));
        dispatch(listDeliveriesSlice.actions.setCurrentPage(1));
        dispatch(listDeliveriesSlice.actions.setPaymentStatus(null));
        dispatch(listDeliveriesSlice.actions.setSearchParams(null));
        if (currentTab !== DELIVERIES_TAB.SEARCH) {
            dispatch(getDeliveries());
        }
    }
);

export const changeCurrentPage = createAsyncThunk<void, number>(
    `${SLICE_NAME}/changeCurrentPage`,
    (currentPage, { dispatch }) => {
        dispatch(listDeliveriesSlice.actions.setCurrentPage(currentPage));
        dispatch(getDeliveries());
    }
);

export const searchDeliveries = createAsyncThunk<void, SearchParams>(
    `${SLICE_NAME}/searchDeliveries`,
    (searchParams, { dispatch }) => {
        dispatch(listDeliveriesSlice.actions.setSearchParams(searchParams));
        dispatch(listDeliveriesSlice.actions.setCurrentPage(1));
        dispatch(getDeliveries());
    }
);

export const changePaymentStatus = createAsyncThunk<void, PaymentStatusFilter>(
    `${SLICE_NAME}/changePaymentStatus`,
    (filter, { dispatch }) => {
        dispatch(listDeliveriesSlice.actions.setPaymentStatus(filter));
        dispatch(getDeliveries());
    }
);

export const selectIsLoading = (state: DeliveriesRootState): boolean =>
    state.deliveries.isLoading;

export const selectError = (state: DeliveriesRootState): string | null =>
    state.deliveries.error;

export const selectDeliveries = (state: DeliveriesRootState): Delivery[] =>
    Object.values(state.deliveries.deliveries);

export const selectCurrentPage = (state: DeliveriesRootState): number =>
    state.deliveries.currentPage;

export const selectCurrentTab = (state: DeliveriesRootState): string =>
    state.deliveries.currentTab;

export const selectTotalCount = (state: DeliveriesRootState): number =>
    state.deliveries.totalCount;
export const selectPaymentStatus = (
    state: DeliveriesRootState
): PaymentStatusFilter => state.deliveries.paymentStatus;

export default listDeliveriesSlice.reducer;
