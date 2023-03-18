import { BaseQueryFn } from '@reduxjs/toolkit/query';
import { storage } from '@vanoma/helpers';
import axios, { AxiosRequestConfig, AxiosError, AxiosResponse } from 'axios';
import { signOut } from '../redux/slices/authenticationSlice';

interface AxiosArgs {
    url: string;
    method: AxiosRequestConfig['method'];
    data?: AxiosRequestConfig['data'];
    headers?: AxiosRequestConfig['headers'];
}

const getAccessToken = (): string | null => storage.getItem('accessToken');

export const axiosBaseQuery = (
    { baseUrl }: { baseUrl: string } = { baseUrl: '' }
): BaseQueryFn<string | AxiosArgs, unknown, string> => {
    const instance = axios.create({ baseURL: baseUrl, withCredentials: true });

    return async (args) => {
        const config: AxiosRequestConfig =
            typeof args === 'string' ? { url: args } : { ...args };

        // Set content-type header for POST and PATCH requests
        config.headers = {
            'Content-Type': 'application/json',
            ...(config.headers ?? {}),
        };

        const token = getAccessToken();
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        try {
            const response = await instance.request(config);
            return { data: response.data, meta: { response } };
        } catch (axiosError) {
            const error = axiosError as AxiosError<{
                errorCode: string;
                errorMessage: string;
            }>;

            const { message, response } = error;

            if (message === 'Network Error') {
                return {
                    error: 'Please check your internet connection.',
                    meta: { response },
                };
            }

            return {
                error: response?.data.errorMessage,
                meta: { response },
            };
        }
    };
};

export const reauthBaseQuery = (
    { baseUrl }: { baseUrl: string } = { baseUrl: '' }
): BaseQueryFn<string | AxiosArgs, unknown, string> => {
    const baseQuery = axiosBaseQuery({ baseUrl });
    return async (args, api, extraOptions) => {
        let result = await baseQuery(args, api, extraOptions);
        const meta = result.meta as { response: AxiosResponse };
        const userId = storage.getItem('userId');

        if (
            result.error &&
            meta.response &&
            meta.response.status === 401 &&
            userId &&
            // There was an infinity loop on sign out. It shouldn't
            // try attempt to refresh token on sign out
            meta.response.config.url !== '/sign-out'
        ) {
            // Try to get a new token
            const refreshResult = await baseQuery(
                { url: '/refresh-token', method: 'POST', data: { userId } },
                api,
                extraOptions
            );

            if (refreshResult.data) {
                storage.setItem(
                    'accessToken',
                    (refreshResult.data as { accessToken: string }).accessToken
                );

                // Retry initial request
                result = await baseQuery(args, api, extraOptions);
            } else {
                api.dispatch(signOut());
            }
        }

        return result;
    };
};
