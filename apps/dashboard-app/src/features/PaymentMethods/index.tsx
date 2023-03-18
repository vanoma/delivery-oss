import React, { ReactElement } from 'react';
import { Stack, Box, Typography } from '@mui/material';
import { styled } from '@mui/material/styles';
import { useSelector } from 'react-redux';
import PaymentMethodView from './PaymentMethod';
import '../../locales/i18n';
import RemovePaymentMethod from './RemovePaymentMethod';
import PaymentMethodViewSkeleton from './PaymentMethodSkeleton';
import Head from './Head';
import NewPaymentMethod from '../../components/NewPaymentMethod';
import { useGetPaymentMethodsQuery } from '../../api';
import { selectCustomerId } from '../../redux/slices/authenticationSlice';
import CustomSnackBar from '../../components/CustomSnackBar';

const FlexBox = styled(Box)(({ theme }) => ({
    display: 'flex',
    gap: theme.spacing(2),
    flexWrap: 'wrap',
    [theme.breakpoints.down('xs')]: {
        flexDirection: 'column',
    },
}));

const PaymentMethods = (): ReactElement => {
    const [openRemovePaymentMethod, setOpenRemovePaymentMethod] =
        React.useState<{ open: boolean; id: string | null }>({
            open: false,
            id: null,
        });
    const [openNewPaymentMethod, setOpenNewPaymentMethod] =
        React.useState(false);

    const customerId = useSelector(selectCustomerId);
    const { data, isFetching, error, refetch } = useGetPaymentMethodsQuery({
        customerId: customerId!,
    });

    const handleRemovePaymentMethodOpen = (id: string): void => {
        setOpenRemovePaymentMethod({ open: true, id });
    };
    const handleRemovePaymentMethodClose = (): void => {
        setOpenRemovePaymentMethod({ open: false, id: null });
    };

    const handleNewPaymentMethodOpen = (): void => {
        setOpenNewPaymentMethod(true);
    };
    const handleNewPaymentMethodClose = (): void => {
        setOpenNewPaymentMethod(false);
    };

    return (
        <Stack spacing={1}>
            <Head
                isLoading={isFetching}
                handleNewPaymentMethodOpen={handleNewPaymentMethodOpen}
            />
            <FlexBox>
                {isFetching || data ? (
                    data?.results.map((paymentMethod) => (
                        <PaymentMethodView
                            key={paymentMethod.paymentMethodId}
                            handleRemovePaymentMethodOpen={
                                handleRemovePaymentMethodOpen
                            }
                            paymentMethod={paymentMethod}
                        />
                    ))
                ) : (
                    <Typography>
                        You don&apos;t have any payment method.
                    </Typography>
                )}
                {isFetching &&
                    [...new Array(3)].map((el, i) => (
                        // eslint-disable-next-line react/no-array-index-key
                        <PaymentMethodViewSkeleton key={i} />
                    ))}
            </FlexBox>
            <RemovePaymentMethod
                openRemovePaymentMethod={openRemovePaymentMethod}
                handleRemovePaymentMethodClose={handleRemovePaymentMethodClose}
            />
            <NewPaymentMethod
                openNewPaymentMethod={openNewPaymentMethod}
                handleNewPaymentMethodClose={handleNewPaymentMethodClose}
                phoneNumberPreFill=""
            />
            <CustomSnackBar
                message={error as string}
                severity="error"
                onRetry={refetch}
            />
        </Stack>
    );
};

export default PaymentMethods;
