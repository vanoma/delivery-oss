import { Skeleton } from '@mui/material';
import React, { ReactElement } from 'react';

const UnpaidBillSkeleton = (): ReactElement => {
    return (
        <>
            <Skeleton />
            <Skeleton animation="wave" width={220} />
            <Skeleton />
            <Skeleton animation="wave" />
            <Skeleton width={220} sx={{ mt: 2 }} />
            <Skeleton
                height={40}
                variant="rectangular"
                animation="wave"
                sx={{ borderRadius: 20, mt: 0.5 }}
            />
            <Skeleton
                height={40}
                variant="rectangular"
                sx={{ borderRadius: 20, mt: 2 }}
            />
            <Skeleton animation="wave" sx={{ mt: 2 }} />
            <Skeleton sx={{ mt: 2 }} />
        </>
    );
};

export default UnpaidBillSkeleton;
