import React, { useRef, useState } from 'react';
import { Typography, Pagination, Skeleton } from '@mui/material';
import '../locales/i18n';
import { SerializedError } from '@reduxjs/toolkit';
import { useTranslation } from 'react-i18next';
import CustomSnackBar from './CustomSnackBar';
import { ListOf } from '../types';
import { DELIVERIES_TAB } from '../routeNames';

export const PAGE_SIZE = 10;
const PROGRESS_INIT = 0;
const PROGRESS_DIFF = 5;
const PROGRESS_FINAL = 95;

interface Props<T> {
    isFetching: boolean;
    data: ListOf<T> | undefined;
    error: string | SerializedError | undefined;
    refetch: () => void;
    currentPage: number;
    setCurrentPage: React.Dispatch<React.SetStateAction<number>>;
    // eslint-disable-next-line no-unused-vars
    renderItem: (item: T, progress: number) => JSX.Element;
    tab: string;
}

const DeliveryListContainer = <T extends any>({
    isFetching,
    data,
    error,
    refetch,
    currentPage,
    setCurrentPage,
    renderItem,
    tab,
}: Props<T>): JSX.Element => {
    const { t } = useTranslation();
    const [progress, setProgress] = useState(0);

    // eslint-disable-next-line no-undef
    const timerRef = useRef<NodeJS.Timer>();

    React.useEffect(() => {
        if (timerRef.current) {
            clearInterval(timerRef.current);
        }
        if (!isFetching && tab !== DELIVERIES_TAB.COMPLETE) {
            timerRef.current = setInterval(() => {
                setProgress((prevProgress) =>
                    prevProgress >= PROGRESS_FINAL
                        ? PROGRESS_INIT
                        : prevProgress + PROGRESS_DIFF
                );
            }, 3000);
        }
        const timer = timerRef.current;
        if (timer) {
            return () => clearInterval(timer);
        }
        return () => {};
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [isFetching]);

    return (
        <>
            {isFetching &&
                !data &&
                [...new Array(10)].map((e, index) => (
                    <Skeleton
                        // eslint-disable-next-line react/no-array-index-key
                        key={index}
                        variant="rectangular"
                        animation="wave"
                        height={138}
                        sx={{ borderRadius: 0.5, mb: 2 }}
                    />
                ))}
            {!isFetching && data && data.count === 0 && (
                <Typography>
                    {t('deliveries.orders.thereIsNoDeliveryToShowYet')}
                </Typography>
            )}
            {data &&
                data.count > 0 &&
                data.results.map((item) => renderItem(item, progress))}
            {(isFetching || (data && data.count > 0)) && (
                <Pagination
                    count={Math.ceil((data ? data.count : 0) / PAGE_SIZE)}
                    page={currentPage}
                    variant="outlined"
                    color="primary"
                    onChange={(e, value) => setCurrentPage(value)}
                    disabled={isFetching}
                />
            )}
            <CustomSnackBar
                message={error as string}
                severity="error"
                onRetry={refetch}
            />
        </>
    );
};

export default DeliveryListContainer;
