import React, { ReactElement } from 'react';
import { Card, Skeleton, Stack, Typography } from '@mui/material';
import { styled } from '@mui/material/styles';

const PaymentMethodPlaceholder = styled(Card)(({ theme }) => ({
    padding: theme.spacing(3),

    width: '100%',
    [theme.breakpoints.up('sm')]: {
        minWidth: theme.spacing(45),
        width: theme.spacing(46.5),
    },
}));

const PaymentMethodViewSkeleton = (): ReactElement => {
    return (
        <PaymentMethodPlaceholder>
            <Stack spacing={1.9}>
                <Typography variant="h5">
                    <Skeleton animation="wave" />
                </Typography>
                <Typography variant="h3">
                    <Skeleton width={220} />
                </Typography>
                <Skeleton animation="wave" />
            </Stack>
        </PaymentMethodPlaceholder>
    );
};

export default PaymentMethodViewSkeleton;
