import React from 'react';
import { Typography, Pagination, Skeleton } from '@mui/material';
import { CustomSnackBar } from '@vanoma/ui-components';
import { useSelector } from 'react-redux';
import { useTypedDispatch } from '../redux/typedHooks';
import {
    selectTotalCount,
    changeCurrentPage,
    PAGE_SIZE,
    selectCurrentPage,
    selectIsLoading,
    selectError,
    selectDeliveries,
} from '../redux/slices/deliveriesSlice';
import { Delivery } from '../types';

interface Props {
    // eslint-disable-next-line no-unused-vars
    renderItem: (item: Delivery) => JSX.Element;
}

const DeliveryListContainer = ({ renderItem }: Props): JSX.Element => {
    const dispatch = useTypedDispatch();
    const totalCount = useSelector(selectTotalCount);
    const currentPage = useSelector(selectCurrentPage);
    const isLoading = useSelector(selectIsLoading);
    const error = useSelector(selectError);
    const deliveries = useSelector(selectDeliveries);

    return (
        <>
            {isLoading &&
                [...new Array(10)].map((e, index) => (
                    <Skeleton
                        // eslint-disable-next-line react/no-array-index-key
                        key={index}
                        variant="rectangular"
                        animation="wave"
                        height={256}
                        sx={{ borderRadius: 0.5, mb: 2 }}
                    />
                ))}
            {!isLoading && totalCount === 0 && (
                <Typography>No deliveries yet</Typography>
            )}
            {!isLoading && deliveries.map((item) => renderItem(item))}
            {(isLoading || totalCount > 0) && (
                <Pagination
                    count={Math.ceil(totalCount / PAGE_SIZE)}
                    page={currentPage}
                    variant="outlined"
                    color="primary"
                    onChange={(e, value) => dispatch(changeCurrentPage(value))}
                    disabled={isLoading}
                />
            )}
            <CustomSnackBar message={error as string} severity="error" />
        </>
    );
};

export default DeliveryListContainer;
