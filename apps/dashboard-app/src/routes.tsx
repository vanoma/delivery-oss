/* eslint-disable no-nested-ternary */
import React, { lazy } from 'react';
import { Navigate } from 'react-router-dom';
import LoadingPage from './components/LoadingPage';
import {
    AUTH,
    DELIVERIES,
    OVERVIEW,
    ROOT,
    SIGN_IN,
    SIGN_UP,
    CUSTOMERS,
    BILLING,
    ACCOUNT,
} from './routeNames';

const AuthLayout = lazy(() => import('./layouts/AuthLayout'));
const DashboardLayout = lazy(() => import('./layouts/Dashboard'));
const SignIn = lazy(() => import('./pages/SignIn'));
const SignUp = lazy(() => import('./pages/SignUp'));
const Deliveries = lazy(() => import('./pages/Deliveries'));
const Overview = lazy(() => import('./pages/Overview'));
const Contacts = lazy(() => import('./pages/Contacts'));
const Billing = lazy(() => import('./pages/Billing'));
const Account = lazy(() => import('./pages/Account'));

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
                    <Navigate to={OVERVIEW} replace />
                )
            ) : (
                <LoadingPage />
            ),
        children: [
            // { path: SIGN_UP, element: <SignUp /> },
            { path: SIGN_IN, element: <SignIn /> },
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
            { path: OVERVIEW, element: <Overview /> },
            {
                path: `${DELIVERIES}/:tab`,
                element: <Deliveries />,
            },
            { path: CUSTOMERS, element: <Contacts /> },
            { path: `${BILLING}/:tab`, element: <Billing /> },
            { path: `${ACCOUNT}/:tab`, element: <Account /> },
            { path: ROOT, element: <Navigate to={OVERVIEW} replace /> },
        ],
    },
    { path: '*', element: <Navigate to="/" replace /> },
];

export default routes;
