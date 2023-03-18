/* eslint-disable consistent-return */
/* eslint-disable no-param-reassign */
import {
    PayloadAction,
    createAsyncThunk,
    createSlice,
    isAnyOf,
} from '@reduxjs/toolkit';
import moment from 'moment';
import {
    Contact,
    Address,
    DeliveryOrder,
    Package,
    PackageSize,
    DeliveryOrderPricing,
    Branch,
} from '@vanoma/types';
import { api } from '../../api';
import {
    SliceState,
    InvokeDeliveryOrderPaymentResponse,
    RootState,
    AuthenticationRootState,
} from '../../types';

interface FieldErrors {
    pickUpStart: string | null;
}

interface PickUpStop {
    contact: Contact | null;
    address: Address | null;
    note: string | null;
}

interface PackageMap {
    [key: string]: Package<Contact | null, Address | null>;
}

interface LastUsedNote {
    lastNote: string | null;
    packageId: string;
}

interface LastUsedNoteMap {
    [key: string]: string | null;
}

interface NewDeliveryState extends SliceState {
    deliveryOrderId: string | null;
    pricing: DeliveryOrderPricing | null;
    paymentRequestId: string | null;
    fieldErrors: FieldErrors;
    pickUpStart: string | null;
    pickUpStop: PickUpStop;
    packages: PackageMap;
    lastUsedNotes: LastUsedNoteMap;
}

interface NewDeliveryRootState extends RootState {
    newDelivery: NewDeliveryState;
    authentication: AuthenticationRootState;
}

const DEFAULT_PICKUP_START = 'NOW';

const SLICE_NAME = 'newDelivery';

const initialState: NewDeliveryState = {
    // Make sure to update resetState reducer too when you modify/add a field
    isLoading: false,
    error: null,
    deliveryOrderId: null,
    pricing: null,
    paymentRequestId: null,
    fieldErrors: { pickUpStart: null },
    pickUpStart: DEFAULT_PICKUP_START,
    pickUpStop: { contact: null, address: null, note: null },
    packages: {},
    lastUsedNotes: {},
};

const normalizePickUpStart = (pickUpStart: string | null): string | null =>
    pickUpStart !== DEFAULT_PICKUP_START ? pickUpStart : null;

/* Adhoc action creators. They only update the state but never call the API. */

const validatePackages = createAsyncThunk<FieldErrors | void, void>(
    `${SLICE_NAME}/validatePackages`,
    (_arg, { getState, rejectWithValue }) => {
        const { pickUpStart } = (getState() as NewDeliveryRootState)
            .newDelivery;
        if (
            normalizePickUpStart(pickUpStart) !== null &&
            moment(pickUpStart).isBefore(moment.now())
        ) {
            window.scrollTo({ top: 0, behavior: 'smooth' });
            return rejectWithValue({
                pickUpStart: "Pickup time can't be in the past",
            });
        }
    }
);

/* API action creators. Called either by components or through "Utility action creators". */

const getLastUsedToNote = createAsyncThunk<LastUsedNote | string, string>(
    `${SLICE_NAME}/getLastUsedToNote`,
    async (packageId, { dispatch, getState, rejectWithValue }) => {
        try {
            const { toContact, toAddress } = (
                getState() as NewDeliveryRootState
            ).newDelivery.packages[packageId];

            const { lastNote } = await dispatch(
                api.endpoints.getLastUsedNote.initiate(
                    {
                        contactId: toContact!.contactId,
                        addressId: toAddress!.addressId,
                    },
                    { forceRefetch: true }
                )
            ).unwrap();

            return { lastNote, packageId };
        } catch (error) {
            return rejectWithValue(error);
        }
    }
);

const createDeliveryOrder = createAsyncThunk<DeliveryOrder | string, void>(
    `${SLICE_NAME}/createDeliveryOrder`,
    async (_arg, { dispatch, getState, rejectWithValue }) => {
        try {
            const { customer, agent } = (getState() as AuthenticationRootState)
                .authentication.defaultData!;

            const deliveryOrder = await dispatch(
                api.endpoints.createDeliveryOrder.initiate({
                    customerId: customer.customerId,
                    agentId: agent.agentId,
                })
            ).unwrap();

            return deliveryOrder;
        } catch (error) {
            return rejectWithValue(error);
        }
    }
);

const createPackage = createAsyncThunk<Package | string, void>(
    `${SLICE_NAME}/createPackage`,
    async (_arg, { dispatch, getState, rejectWithValue }) => {
        try {
            const {
                pickUpStart,
                pickUpStop: pickupStop,
                deliveryOrderId,
            } = (getState() as NewDeliveryRootState).newDelivery;
            let deliveryOrderIdToUse = deliveryOrderId;

            if (!deliveryOrderIdToUse) {
                const deliveryOrder = await dispatch(
                    createDeliveryOrder()
                ).unwrap();
                deliveryOrderIdToUse = (deliveryOrder as DeliveryOrder)
                    .deliveryOrderId;
            }

            const pkg = await dispatch(
                api.endpoints.createPackage.initiate({
                    deliveryOrderId: deliveryOrderIdToUse!,
                    pickUpStart: normalizePickUpStart(pickUpStart),
                    fromContact: pickupStop.contact,
                    fromAddress: pickupStop.address,
                    fromNote: pickupStop.note,
                    size: null,
                    packageId: null,
                    toContact: null,
                    toAddress: null,
                    toNote: null,
                })
            ).unwrap();

            return pkg;
        } catch (error) {
            return rejectWithValue(error);
        }
    }
);

const updatePackage = createAsyncThunk<Package | string, string>(
    `${SLICE_NAME}/updatePackage`,
    async (packageId, { dispatch, getState, rejectWithValue }) => {
        try {
            const { packages } = (getState() as NewDeliveryRootState)
                .newDelivery;

            const pkg = await dispatch(
                api.endpoints.updatePackage.initiate({
                    ...packages[packageId],
                })
            ).unwrap();

            return pkg;
        } catch (error) {
            return rejectWithValue(error);
        }
    }
);

const deletePackage = createAsyncThunk<PackageMap | string, string>(
    `${SLICE_NAME}/deletePackage`,
    async (packageId, { dispatch, getState, rejectWithValue }) => {
        try {
            const { packages } = (getState() as NewDeliveryRootState)
                .newDelivery;

            await dispatch(
                api.endpoints.deletePackage.initiate(packageId)
            ).unwrap();

            const updatedPackages: PackageMap = {};
            Object.keys(packages).forEach((pkgId) => {
                if (pkgId !== packageId) {
                    updatedPackages[pkgId] = packages[pkgId];
                }
            });

            return updatedPackages;
        } catch (error) {
            return rejectWithValue(error);
        }
    }
);

const getDeliveryOrderPricing = createAsyncThunk<
    DeliveryOrderPricing | string,
    void
>(
    `${SLICE_NAME}/getDeliveryOrderPricing`,
    async (_arg, { dispatch, getState, rejectWithValue }) => {
        try {
            const { deliveryOrderId } = (getState() as NewDeliveryRootState)
                .newDelivery;

            const price = await dispatch(
                api.endpoints.invokeDeliveryOrderPricing.initiate(
                    deliveryOrderId!
                )
            ).unwrap();

            return price;
        } catch (error) {
            return rejectWithValue(error);
        }
    }
);

const payDeliveryOrder = createAsyncThunk<
    InvokeDeliveryOrderPaymentResponse | string,
    string
>(
    `${SLICE_NAME}/payDeliveryOrder`,
    async (paymentMethodId, { dispatch, getState, rejectWithValue }) => {
        try {
            const { deliveryOrderId } = (getState() as NewDeliveryRootState)
                .newDelivery;

            const response = await dispatch(
                api.endpoints.invokeDeliveryOrderPayment.initiate({
                    deliveryOrderId: deliveryOrderId!,
                    paymentMethodId,
                })
            ).unwrap();

            return response;
        } catch (error) {
            return rejectWithValue(error);
        }
    }
);

const placeDeliveryOrder = createAsyncThunk<DeliveryOrder | string, void>(
    `${SLICE_NAME}/placeDeliveryOrder`,
    async (_arg, { dispatch, getState, rejectWithValue }) => {
        try {
            const { deliveryOrderId } = (getState() as NewDeliveryRootState)
                .newDelivery;

            const deliveryOrder = await dispatch(
                api.endpoints.placeDeliveryOrder.initiate(deliveryOrderId!)
            ).unwrap();

            return deliveryOrder;
        } catch (error) {
            return rejectWithValue(error);
        }
    }
);

/* Redux slice */

const newDeliverySlice = createSlice({
    name: SLICE_NAME,
    initialState,
    reducers: {
        updatePickUpStart: (
            state,
            { payload }: PayloadAction<string | null>
        ) => {
            state.pickUpStart = payload;
            Object.keys(state.packages).forEach((packageId) => {
                // value stored on packages must be normalized as we are storing package data as returned from the API
                state.packages[packageId].pickUpStart =
                    normalizePickUpStart(payload);
            });
        },
        updateSize: (
            state,
            {
                payload: { size, packageId },
            }: PayloadAction<{ size: PackageSize; packageId: string }>
        ) => {
            state.packages[packageId].size = size;
        },
        updateFromContact: (
            state,
            { payload }: PayloadAction<Contact | null>
        ) => {
            state.pickUpStop.contact = payload;
            Object.keys(state.packages).forEach((packageId) => {
                state.packages[packageId].fromContact = payload;
            });
        },
        updateFromAddress: (
            state,
            { payload }: PayloadAction<Address | null>
        ) => {
            state.pickUpStop.address = payload;
            Object.keys(state.packages).forEach((packageId) => {
                state.packages[packageId].fromAddress = payload;
            });
        },
        updateToContact: (
            state,
            {
                payload: { contact, packageId },
            }: PayloadAction<{ contact: Contact | null; packageId: string }>
        ) => {
            state.packages[packageId].toContact = contact;
        },
        updateToAddress: (
            state,
            {
                payload: { address, packageId },
            }: PayloadAction<{ address: Address | null; packageId: string }>
        ) => {
            state.packages[packageId].toAddress = address;
        },
        updateFromNote: (state, { payload }: PayloadAction<string | null>) => {
            state.pickUpStop.note = payload;
            Object.keys(state.packages).forEach((packageId) => {
                state.packages[packageId].fromNote = payload;
            });
        },
        updateToNote: (
            state,
            {
                payload: { toNote, packageId },
            }: PayloadAction<{ toNote: string | null; packageId: string }>
        ) => {
            state.packages[packageId].toNote = toNote;
        },
        updatePaymentRequestId: (
            state,
            { payload }: PayloadAction<string | null>
        ) => {
            state.paymentRequestId = payload;
        },
        resetError: (state) => {
            state.error = null;
        },
        resetState: (
            state,
            {
                payload: { defaultContact, defaultAddress, agentBranch },
            }: PayloadAction<{
                defaultContact: Contact | null;
                defaultAddress: Address | null;
                agentBranch: Branch | null;
            }>
        ) => {
            // Doing "state = {... initialState}" does not work so resetting each field individually
            state.isLoading = false;
            state.error = null;
            state.deliveryOrderId = null;
            state.pricing = null;
            state.paymentRequestId = null;
            state.fieldErrors = { pickUpStart: null };
            state.pickUpStart = DEFAULT_PICKUP_START;
            state.pickUpStop.note = null;
            if (agentBranch) {
                state.pickUpStop.contact = agentBranch.contact;
                state.pickUpStop.address = agentBranch.address;
            } else {
                state.pickUpStop.contact = defaultContact;
                state.pickUpStop.address = defaultAddress;
            }
            state.packages = {};
        },
    },
    extraReducers: (builder) => {
        builder.addCase(getLastUsedToNote.fulfilled, (state, { payload }) => {
            const { lastNote, packageId } = payload as LastUsedNote;
            state.lastUsedNotes[packageId] = lastNote;
        });
        builder.addCase(createDeliveryOrder.fulfilled, (state, { payload }) => {
            const deliveryOrder = payload as DeliveryOrder;
            state.deliveryOrderId = deliveryOrder.deliveryOrderId;
        });
        builder.addCase(createPackage.fulfilled, (state, { payload }) => {
            const newPackage = payload as Package;
            state.packages[newPackage.packageId] = newPackage;
            state.isLoading = false;
        });
        builder.addCase(updatePackage.fulfilled, (state) => {
            state.isLoading = false;
        });
        builder.addCase(deletePackage.fulfilled, (state, { payload }) => {
            state.packages = payload as PackageMap;
            state.isLoading = false;
        });
        builder.addCase(validatePackages.pending, (state) => {
            state.isLoading = true;
        });
        builder.addCase(validatePackages.fulfilled, (state) => {
            state.isLoading = false;
        });
        builder.addCase(validatePackages.rejected, (state, { payload }) => {
            state.fieldErrors = payload as FieldErrors;
        });
        builder.addCase(
            getDeliveryOrderPricing.fulfilled,
            (state, { payload }) => {
                state.pricing = payload as DeliveryOrderPricing;
                state.isLoading = false;
            }
        );
        builder.addCase(payDeliveryOrder.fulfilled, (state, { payload }) => {
            const { paymentRequestId } =
                payload as InvokeDeliveryOrderPaymentResponse;

            state.paymentRequestId = paymentRequestId;
            state.isLoading = false;
        });
        builder.addCase(placeDeliveryOrder.fulfilled, (state) => {
            state.isLoading = false;
        });
        builder.addMatcher(
            isAnyOf(
                createDeliveryOrder.pending,
                createPackage.pending,
                updatePackage.pending,
                deletePackage.pending,
                getDeliveryOrderPricing.pending,
                payDeliveryOrder.pending,
                placeDeliveryOrder.pending
            ),
            (state) => {
                state.isLoading = true;
                state.error = null; // Always reset previous error if any
            }
        );
        builder.addMatcher(
            isAnyOf(
                createDeliveryOrder.rejected,
                createPackage.rejected,
                updatePackage.rejected,
                deletePackage.rejected,
                getDeliveryOrderPricing.rejected,
                payDeliveryOrder.rejected,
                placeDeliveryOrder.rejected
            ),
            (state, { payload }) => {
                state.isLoading = false;
                state.error = payload as string;
            }
        );
        builder.addMatcher(
            api.endpoints.getDefaultData.matchFulfilled,
            (state, { payload }) => {
                const { contact, address, agent } = payload;
                if (agent.branch) {
                    state.pickUpStop.contact = agent.branch.contact;
                    state.pickUpStop.address = agent.branch.address;
                } else {
                    state.pickUpStop.contact = contact;
                    state.pickUpStop.address = address;
                }
            }
        );
    },
});

export const {
    updatePickUpStart,
    updateSize,
    updateFromContact,
    updateFromAddress,
    updateToContact,
    updateToAddress,
    updateFromNote,
    updateToNote,
    updatePaymentRequestId,
    resetState,
    resetError,
} = newDeliverySlice.actions;

export {
    DEFAULT_PICKUP_START,
    getLastUsedToNote,
    validatePackages,
    payDeliveryOrder,
    placeDeliveryOrder,
    createPackage,
    deletePackage,
};

/* Utility action creators. They don't update redux state directly but rather call other action creators instead. */

export const updateAllPackages = createAsyncThunk<string | void, void>(
    `${SLICE_NAME}/updateAllPackages`,
    async (_arg, { dispatch, getState, rejectWithValue }) => {
        try {
            const { packages } = (getState() as NewDeliveryRootState)
                .newDelivery;

            await Promise.all(
                Object.keys(packages).map((pkgId) =>
                    dispatch(updatePackage(pkgId)).unwrap()
                )
            );
        } catch (error) {
            return rejectWithValue(error);
        }
    }
);

export const updateAllPackagesAndGetDeliveryOrderPrice = createAsyncThunk<
    string | void,
    void
>(
    `${SLICE_NAME}/updateAllPackagesAndGetDeliveryOrderPrice`,
    async (_arg, { dispatch, rejectWithValue }) => {
        try {
            await dispatch(updateAllPackages()).unwrap();
            await dispatch(getDeliveryOrderPricing()).unwrap();
        } catch (error) {
            return rejectWithValue(error);
        }
    }
);

/* Redux selectors */

export const selectIsLoading = (state: NewDeliveryRootState): boolean =>
    state.newDelivery.isLoading;

export const selectIsValid = (state: NewDeliveryRootState): boolean =>
    Object.values(state.newDelivery.fieldErrors).filter(
        (value) => value !== null
    ).length === 0;

export const selectError = (state: NewDeliveryRootState): string | null =>
    state.newDelivery.error;

export const selectPickUpStart = (
    state: NewDeliveryRootState
): string | null => {
    return state.newDelivery.pickUpStart;
};

export const selectNormalizedPickUpStart = (
    state: NewDeliveryRootState
): string | null => {
    return normalizePickUpStart(selectPickUpStart(state));
};

export const selectPickUpStartError = (
    state: NewDeliveryRootState
): string | null => state.newDelivery.fieldErrors.pickUpStart;

export const selectFromContact = (
    state: NewDeliveryRootState
): Contact | null => {
    return state.newDelivery.pickUpStop.contact;
};

export const selectFromAddress = (
    state: NewDeliveryRootState
): Address | null => {
    return state.newDelivery.pickUpStop.address;
};

export const selectFromNote = (state: NewDeliveryRootState): string | null => {
    return state.newDelivery.pickUpStop.note;
};

export const selectPackages = (
    state: NewDeliveryRootState
): Package<Contact | null, Address | null>[] =>
    Object.values(state.newDelivery.packages);

export const selectPackageCount = (state: NewDeliveryRootState): number =>
    Object.keys(state.newDelivery.packages).length;

export const selectPricing = (
    state: NewDeliveryRootState
): DeliveryOrderPricing | null => state.newDelivery.pricing;

export const selectPaymentRequestId = (
    state: NewDeliveryRootState
): string | null => state.newDelivery.paymentRequestId;

export const selectDeliveryOrderId = (
    state: NewDeliveryRootState
): string | null => state.newDelivery.deliveryOrderId;

export const selectLastUsedNotes = (
    state: NewDeliveryRootState
): LastUsedNoteMap => state.newDelivery.lastUsedNotes;

export default newDeliverySlice.reducer;
