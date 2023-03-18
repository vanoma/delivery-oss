/* eslint-disable no-param-reassign */
import {
    createSlice,
    createAsyncThunk,
    isAnyOf,
    PayloadAction,
} from '@reduxjs/toolkit';
import { Staff, StaffStatus } from '@vanoma/types';
import { storage } from '@vanoma/helpers';
import {
    SliceState,
    RootState,
    SendOTPRequest,
    SignInRequest,
} from '../../types';
import { api } from '../../api';
import { setExternalUserId } from '../../services/oneSignal';

const SLICE_NAME = 'authentication';

interface OTPData {
    phoneNumber: string;
    otpId: string;
}

interface SignUpData {
    otpCode: string;
    firstName: string;
    lastName: string;
}

interface AuthenticationState extends SliceState {
    isAuthenticated: boolean | null;
    otpData: OTPData | null;
    signUpData: SignUpData | null;
    staff: Staff | null;
}

interface AuthenticationRootState extends RootState {
    authentication: AuthenticationState;
}

const initialState: AuthenticationState = {
    isLoading: false,
    success: null,
    error: null,
    isAuthenticated: null,
    otpData: null,
    signUpData: null,
    staff: null,
};

export const sendOtp = createAsyncThunk<OTPData, SendOTPRequest>(
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

export const signIn = createAsyncThunk<Staff | string, SignInRequest>(
    `${SLICE_NAME}/signIn`,
    async (body, { dispatch, rejectWithValue }) => {
        try {
            const { accessToken, userId, staffId } = await dispatch(
                api.endpoints.signIn.initiate(body)
            ).unwrap();

            // Save data in local storage
            storage.setItem('accessToken', accessToken);
            storage.setItem('userId', userId);
            storage.setItem('staffId', staffId);

            // Fetch staff data
            const staff = await dispatch(
                api.endpoints.getStaff.initiate(staffId)
            ).unwrap();

            // Validate access
            if (staff.status !== StaffStatus.ACTIVE) {
                storage.clear();
                return rejectWithValue(
                    'You are not authorized to sign in yet.'
                );
            }

            setExternalUserId();
            return staff;
        } catch (error) {
            return rejectWithValue(error);
        }
    }
);

export const signUp = createAsyncThunk<string, { password: string }>(
    `${SLICE_NAME}/signUp`,
    async ({ password }, { dispatch, getState, rejectWithValue }) => {
        try {
            const { otpData, signUpData } = (
                getState() as AuthenticationRootState
            ).authentication;
            await dispatch(
                api.endpoints.signUp.initiate({
                    phoneNumber: otpData!.phoneNumber,
                    firstName: signUpData!.firstName,
                    lastName: signUpData!.lastName,
                    password,
                    otpId: otpData!.otpId,
                    otpCode: signUpData!.otpCode,
                })
            ).unwrap();
            return 'Done! Your account is waiting for approval.';
        } catch (error) {
            return rejectWithValue(error);
        }
    }
);

export const signOut = createAsyncThunk<void, void>(
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

export const getStaff = createAsyncThunk<Staff | string, void>(
    `${SLICE_NAME}/getStaff`,
    async (arg, { dispatch, rejectWithValue }) => {
        const accessToken = storage.getItem('accessToken');
        const staffId = storage.getItem('staffId');

        if (!accessToken || !staffId) {
            // TODO: translation here required. Although we don't show this error in the UI.
            return rejectWithValue('You must log in');
        }

        try {
            // Fetch staff data
            const staff = await dispatch(
                api.endpoints.getStaff.initiate(staffId, {
                    // Since it won't be called many times, it shouldn't use cached data
                    // when called. It's crucial after sign up since default data won't
                    // won't contain all data(contact and address)
                    forceRefetch: true,
                })
            ).unwrap();

            // Validate access
            if (staff.status !== StaffStatus.ACTIVE) {
                rejectWithValue('You are not authorized to sign in yet.');
            }

            setExternalUserId();
            return staff;
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
        updateSignUpData: (state, { payload }: PayloadAction<SignUpData>) => {
            state.signUpData = payload;
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
            state.isLoading = false;
        });
        builder.addCase(signOut.rejected, (state, { payload }) => {
            state.error = payload as string;
            state.isLoading = false;
        });
        builder.addCase(signUp.fulfilled, (state, { payload }) => {
            state.success = payload as string;
            state.isLoading = false;
        });
        builder.addMatcher(
            isAnyOf(
                sendOtp.pending,
                signIn.pending,
                signUp.pending,
                signOut.pending,
                getStaff.pending
            ),
            (state) => {
                state.isLoading = true;
                state.error = null; // Always reset previous error if any
            }
        );
        builder.addMatcher(
            isAnyOf(signIn.fulfilled, getStaff.fulfilled),
            (state, { payload }) => {
                state.isAuthenticated = true;
                state.staff = payload as Staff;
                state.isLoading = false;
            }
        );
        builder.addMatcher(
            isAnyOf(signIn.rejected, signUp.rejected, getStaff.rejected),
            (state, { payload }) => {
                state.isAuthenticated = false;
                state.error = payload as string;
                state.isLoading = false;
            }
        );
    },
});

export const selectIsLoading = (state: AuthenticationRootState): boolean =>
    state.authentication.isLoading;

export const selectError = (state: AuthenticationRootState): string | null =>
    state.authentication.error;
export const selectSuccess = (state: AuthenticationRootState): string | null =>
    state.authentication.success;

export const selectIsAuthenticated = (
    state: AuthenticationRootState
): boolean | null => state.authentication.isAuthenticated;

export const selectOtpData = (state: AuthenticationRootState): OTPData | null =>
    state.authentication.otpData;

export const selectUser = (state: AuthenticationRootState): Staff | null =>
    state.authentication.staff;

export const { resetError, updateSignUpData } = authenticationSlice.actions;

export default authenticationSlice.reducer;
