/* eslint-disable no-param-reassign */
import { createSlice, createAsyncThunk, isAnyOf } from '@reduxjs/toolkit';
import { Contact, Address, Agent, Branch } from '@vanoma/types';
import {
    SignInRequest,
    SignUpRequest,
    AuthenticationState,
    AuthenticationRootState,
    SendOTPRequest,
    OTPData,
    DefaultData,
    Account,
    PaymentMethod,
} from '../../types';
import { api } from '../../api';
import storage from '../../services/storage';
import { setUserIdentifier } from '../../services/sentry';
import { setExternalUserId } from '../../services/oneSignal';

const SLICE_NAME = 'authentication';

const initialState: AuthenticationState = {
    isLoading: true, // Start in loading mode since we need to attempt to fetch customer
    error: null,
    isAuthenticated: null,
    defaultData: null,
    otpData: null,
    accounts: [],
};

const sendOtp = createAsyncThunk<OTPData, SendOTPRequest, {}>(
    `${SLICE_NAME}/sendOtp`,
    async ({ phoneNumber }, { dispatch, rejectWithValue }) => {
        try {
            const { otpId } = await dispatch(
                api.endpoints.sendOtp.initiate({ phoneNumber })
            ).unwrap();

            return { otpId, phoneNumber };
        } catch (error) {
            return rejectWithValue(error);
        }
    }
);

const signIn = createAsyncThunk<Account[] | string, SignInRequest, {}>(
    `${SLICE_NAME}/signIn`,
    async (body, { dispatch, rejectWithValue }) => {
        try {
            const { accessToken, userId, accounts } = await dispatch(
                api.endpoints.signIn.initiate(body)
            ).unwrap();

            // Save initial data in local storage
            storage.setItem('accessToken', accessToken);
            storage.setItem('userId', userId);

            return accounts;
        } catch (error) {
            return rejectWithValue(error);
        }
    }
);

const signUp = createAsyncThunk<DefaultData | string, SignUpRequest, {}>(
    `${SLICE_NAME}/signUp`,
    async (body, { dispatch, rejectWithValue }) => {
        try {
            const { customerId } = await dispatch(
                api.endpoints.signUp.initiate(body)
            ).unwrap();
            const { accessToken, userId, accounts } = await dispatch(
                api.endpoints.signIn.initiate({
                    verificationCode: body.otpCode,
                    verificationId: body.otpId,
                    phoneNumber: body.phoneNumber,
                })
            ).unwrap();

            // Find agent corresponding to the newly created account
            const { agentId } = accounts.find(
                (a) => a.customerId === customerId
            )!;

            // Save data in local storage
            storage.setItem('accessToken', accessToken);
            storage.setItem('customerId', customerId);
            storage.setItem('userId', userId);
            storage.setItem('agentId', agentId);

            // Fetch customer information
            const result = await dispatch(
                api.endpoints.getDefaultData.initiate({ customerId, agentId })
            ).unwrap();

            setExternalUserId(customerId);
            setUserIdentifier({
                customerId,
                businessName: body.businessName,
                phoneNumber: body.phoneNumber,
            });

            return result;
        } catch (error) {
            return rejectWithValue(error);
        }
    }
);

const signOut = createAsyncThunk<void, void, {}>(
    `${SLICE_NAME}/signOut`,
    async (arg, { dispatch, rejectWithValue }) => {
        try {
            const userId = storage.getItem('userId');
            await dispatch(api.endpoints.signOut.initiate(userId!)).unwrap();
            storage.clear();
            window.location.reload(); // To reset all redux states
            return Promise.resolve();
        } catch (error) {
            return rejectWithValue(error);
        }
    }
);

const saveAccount = createAsyncThunk<DefaultData | string, Account, {}>(
    `${SLICE_NAME}/saveAccount`,
    async ({ customerId, agentId }, { dispatch, rejectWithValue }) => {
        try {
            // Save additional data in local storage
            storage.setItem('customerId', customerId);
            storage.setItem('agentId', agentId);

            // Fetch customer information
            const result = await dispatch(
                api.endpoints.getDefaultData.initiate({ customerId, agentId })
            ).unwrap();

            setExternalUserId(customerId);
            setUserIdentifier({
                customerId,
                businessName: result.customer.businessName,
                phoneNumber: result.customer.phoneNumber,
            });

            return result;
        } catch (error) {
            return rejectWithValue(error);
        }
    }
);

const getDefaultData = createAsyncThunk<DefaultData | string, void, {}>(
    `${SLICE_NAME}/getDefaultData`,
    async (arg, { dispatch, rejectWithValue }) => {
        const accessToken = storage.getItem('accessToken');
        const customerId = storage.getItem('customerId');
        const agentId = storage.getItem('agentId');

        if (!accessToken || !customerId || !agentId) {
            // TODO: translation here required. Although we don't show this error in the UI.
            return rejectWithValue('You must log in');
        }

        try {
            const result = await dispatch(
                api.endpoints.getDefaultData.initiate(
                    { customerId, agentId },
                    {
                        // Since it won't be called many times, it shouldn't use cached data
                        // when called. It's crucial after sign up since default data won't
                        // won't contain all data(contact and address)
                        forceRefetch: true,
                    }
                )
            ).unwrap();

            setExternalUserId(result.customer.customerId);
            setUserIdentifier({
                customerId: result.customer.customerId,
                businessName: result.customer.businessName,
                phoneNumber: result.customer.phoneNumber,
            });

            return result;
        } catch (error) {
            return rejectWithValue(error);
        }
    }
);

const authenticationSlice = createSlice({
    name: SLICE_NAME,
    initialState,
    reducers: {
        resetError: (state) => {
            state.error = null;
        },
    },
    extraReducers: (builder) => {
        builder.addCase(sendOtp.fulfilled, (state, { payload }) => {
            state.otpData = payload;
            state.isLoading = false;
        });
        builder.addCase(sendOtp.rejected, (state, { payload }) => {
            state.error = payload as string;
            state.isLoading = false;
        });
        builder.addCase(signOut.fulfilled, (state) => {
            state.isAuthenticated = false;
            state.defaultData = null;
            state.isLoading = false;
        });
        builder.addCase(signOut.rejected, (state, { payload }) => {
            state.error = payload as string;
            state.isLoading = false;
        });
        builder.addCase(signIn.fulfilled, (state, { payload }) => {
            state.accounts = payload as Account[];
            state.isLoading = false;
        });
        builder.addMatcher(
            isAnyOf(
                sendOtp.pending,
                signIn.pending,
                signUp.pending,
                signOut.pending,
                saveAccount.pending,
                getDefaultData.pending
            ),
            (state) => {
                state.isLoading = true;
                state.error = null; // Always reset previous error if any
            }
        );
        builder.addMatcher(
            isAnyOf(
                signUp.fulfilled,
                saveAccount.fulfilled,
                getDefaultData.fulfilled
            ),
            (state, { payload }) => {
                state.isAuthenticated = true;
                state.defaultData = payload as DefaultData;
                state.isLoading = false;
            }
        );
        builder.addMatcher(
            isAnyOf(
                signIn.rejected,
                signUp.rejected,
                saveAccount.rejected,
                getDefaultData.rejected
            ),
            (state, { payload }) => {
                state.isAuthenticated = false;
                state.error = payload as string;
                state.isLoading = false;
            }
        );
        builder.addMatcher(
            api.endpoints.getDefaultData.matchFulfilled, // Need to get updated data after cache invalidation
            (state, { payload }) => {
                state.defaultData = payload as DefaultData;
            }
        );
    },
});

export const selectIsLoading = (state: AuthenticationRootState): boolean =>
    state.authentication.isLoading;

export const selectError = (state: AuthenticationRootState): string | null =>
    state.authentication.error;

export const selectCustomerId = (
    state: AuthenticationRootState
): string | null =>
    state.authentication.defaultData
        ? state.authentication.defaultData.customer.customerId
        : null;

export const selectIsAuthenticated = (
    state: AuthenticationRootState
): boolean | null => state.authentication.isAuthenticated;

export const selectBusinessName = (
    state: AuthenticationRootState
): string | null =>
    state.authentication.defaultData
        ? state.authentication.defaultData.customer.businessName
        : null;

export const selectPhoneNumber = (
    state: AuthenticationRootState
): string | null =>
    state.authentication.defaultData
        ? state.authentication.defaultData.customer.phoneNumber
        : null;

export const selectContactId = (
    state: AuthenticationRootState
): string | null =>
    state.authentication.defaultData && state.authentication.defaultData.contact
        ? state.authentication.defaultData.contact.contactId
        : null;

export const selectAddressId = (
    state: AuthenticationRootState
): string | null =>
    state.authentication.defaultData && state.authentication.defaultData.address
        ? state.authentication.defaultData.address.addressId
        : null;

export const selectPaymentMethod = (
    state: AuthenticationRootState
): PaymentMethod | null =>
    state.authentication.defaultData
        ? state.authentication.defaultData.paymentMethod
        : null;

export const selectDefaultContact = (
    state: AuthenticationRootState
): Contact | null => state.authentication.defaultData?.contact ?? null;

export const selectDefaultAddress = (
    state: AuthenticationRootState
): Address | null => state.authentication.defaultData?.address ?? null;

export const selectOtpData = (state: AuthenticationRootState): OTPData | null =>
    state.authentication.otpData;

export const selectAgent = (state: AuthenticationRootState): Agent | null =>
    state.authentication.defaultData
        ? state.authentication.defaultData.agent
        : null;

export const selectAgentBranch = (
    state: AuthenticationRootState
): Branch | null =>
    state.authentication.defaultData
        ? state.authentication.defaultData.agent.branch
        : null;

export const selectBranches = (state: AuthenticationRootState): Branch[] =>
    state.authentication.defaultData
        ? state.authentication.defaultData.branches
        : [];

export const selectAccounts = (state: AuthenticationRootState): Account[] =>
    state.authentication.accounts ?? [];

export { sendOtp, signIn, signUp, signOut, saveAccount, getDefaultData };

export const { resetError } = authenticationSlice.actions;

export default authenticationSlice.reducer;
