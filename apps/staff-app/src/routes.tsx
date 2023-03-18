/* eslint-disable no-nested-ternary */
import React, { lazy } from 'react';
import { Navigate } from 'react-router-dom';
import LoadingPage from './components/LoadingPage';
import {
    AUTH,
    DELIVERIES,
    ROOT,
    SIGN_IN,
    SIGN_UP,
    CHECK_PRICE,
    VERIFICATIONS,
    DELIVERIES_TAB,
    CUSTOMERS,
} from './routeNames';

const AuthLayout = lazy(() => import('./layouts/AuthLayout'));
const DashboardLayout = lazy(() => import('./layouts/Dashboard'));
const CheckPrice = lazy(() => import('./pages/CheckPrice'));
const Deliveries = lazy(() => import('./pages/Deliveries'));
const SignIn = lazy(() => import('./pages/SignIn'));
const SignUp = lazy(() => import('./pages/SignUp'));
const Verifications = lazy(() => import('./pages/Verifications'));
const Customers = lazy(() => import('./pages/Customers'));

interface PartialRouteObject {
    path: string;
    element: React.ReactNode;
    children?: {
        path: string;
        element: React.ReactNode;
    }[];
}

const routes = (isAuthenticated: boolean | null): PartialRouteObject[] => [
    {
        path: AUTH,
        element:
            isAuthenticated !== null ? (
                !isAuthenticated ? (
                    <AuthLayout />
                ) : (
                    <Navigate to={DELIVERIES} replace />
                )
            ) : (
                <LoadingPage />
            ),
        children: [
            { path: SIGN_IN, element: <SignIn /> },
            { path: SIGN_UP, element: <SignUp /> },
        ],
    },
    {
        path: ROOT,
        element:
            isAuthenticated !== null ? (
                isAuthenticated ? (
                    <DashboardLayout />
                ) : (
                    <Navigate to={`${AUTH}/${SIGN_UP}`} replace />
                )
            ) : (
                <LoadingPage />
            ),
        children: [
            { path: `${DELIVERIES}/:tab`, element: <Deliveries /> },
            { path: CHECK_PRICE, element: <CheckPrice /> },
            { path: VERIFICATIONS, element: <Verifications /> },
            { path: CUSTOMERS, element: <Customers /> },
            {
                path: ROOT,
                element: (
                    <Navigate
                        to={`${DELIVERIES}/${DELIVERIES_TAB.ACTIVE}`}
                        replace
                    />
                ),
            },
        ],
    },
    { path: '*', element: <Navigate to="/" replace /> },
];

export default routes;
