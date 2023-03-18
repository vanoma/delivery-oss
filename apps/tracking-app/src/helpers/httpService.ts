import axios, { AxiosError, AxiosInstance } from 'axios';
import * as Sentry from '@sentry/gatsby';

const internetErrorMessage = 'Please check your internet connection.';

const createHttpClient = (): AxiosInstance => {
    const instance = axios.create({
        baseURL: process.env.API_URL,
    });

    instance.interceptors.response.use(
        (response) => response,
        (error: AxiosError<{ errorCode: string; errorMessage: string }>) => {
            Sentry.captureException(error);
            if (error.message === 'Network Error') {
                // eslint-disable-next-line prefer-promise-reject-errors
                return Promise.reject({
                    name: 'NETWORK',
                    message: internetErrorMessage,
                });
            }
            if (!error.response || error.response.status === 500) {
                // eslint-disable-next-line prefer-promise-reject-errors
                return Promise.reject({
                    name: 'DEFAULT',
                    message: 'Something went wrong. Please contact support.',
                });
            }
            // eslint-disable-next-line prefer-promise-reject-errors
            return Promise.reject({
                name: error.response?.data.errorCode,
                message: error.response?.data.errorMessage,
            });
        }
    );
    return instance;
};

// Return an instance of the client so that it can be reused across different callers.
export default createHttpClient();
