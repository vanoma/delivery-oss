import React, { ReactElement, useState } from 'react';
import { Card, Stack, Typography, Box, Divider, Skeleton } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { styled } from '@mui/material/styles';
import { useSelector } from 'react-redux';
import UnpaidBill from './UnpaidBill';
import BillDatePicker from './DatePicker';
import BillTimeToggle from './BillTimeToggle';
import { useGetDeliverySpendingQuery } from '../../api';
import {
    selectAgent,
    selectCustomerId,
} from '../../redux/slices/authenticationSlice';
import CustomSnackBar from '../../components/CustomSnackBar';
import BranchSelector from '../../components/BranchSelector';

const FlexBox = styled(Box)(({ theme }) => ({
    display: 'flex',
    justifyContent: 'space-between',
    gap: theme.spacing(2),
    flexDirection: 'column',
    padding: `0 ${theme.spacing(3)}`,
    [theme.breakpoints.down('xs')]: {
        padding: `0 ${theme.spacing(0.5)}`,
    },
}));

const PayBalance = (): ReactElement => {
    const { t } = useTranslation();

    const customerId = useSelector(selectCustomerId);
    const agent = useSelector(selectAgent);

    const [rangeDate, setRangeDate] = useState<Date>();
    const [range, setRange] = useState<'all' | 'endingAt'>('all');
    const [selectedBranchId, setSelectedBranchId] = useState(
        agent!.branch?.branchId ?? 'all'
    );

    const { data, isFetching, error, refetch } = useGetDeliverySpendingQuery({
        customerId: customerId!,
        endAt: rangeDate?.toISOString(),
        branchId: selectedBranchId !== 'all' ? selectedBranchId : undefined,
    });

    const isLoadingAndNoBill = isFetching && !data;

    return (
        <>
            <Card sx={{ py: { xs: 2, sm: 3 } }}>
                <Stack spacing={2}>
                    <Typography
                        variant="h5"
                        sx={{
                            lineHeight: 1,
                            px: { xs: 2, sm: 3 },
                        }}
                    >
                        {isLoadingAndNoBill ? (
                            <Skeleton width={220} />
                        ) : (
                            t('billing.payBalance.unpaidDeliveries')
                        )}
                    </Typography>
                    <Divider />
                    <FlexBox
                        sx={{
                            flexDirection: {
                                md: range === 'all' ? 'column' : 'row',
                            },
                        }}
                    >
                        <Stack spacing={2}>
                            <BranchSelector
                                value={selectedBranchId}
                                onChange={(value) => setSelectedBranchId(value)}
                                allBranches
                                sx={{
                                    maxWidth: 320,
                                    mx: 'auto',
                                    margin: 0,
                                }}
                            />
                            <BillTimeToggle
                                range={range}
                                setRange={setRange}
                                setRangeDate={setRangeDate}
                                isLoadingAndNoBill={isLoadingAndNoBill}
                            />
                            {range !== 'all' && (
                                <BillDatePicker
                                    rangeDate={rangeDate}
                                    setRangeDate={setRangeDate}
                                    isLoadingAndNoBill={isLoadingAndNoBill}
                                />
                            )}
                        </Stack>
                        <UnpaidBill
                            isLoading={isFetching}
                            bill={data}
                            disablePayButton={
                                !data ||
                                (data && data?.totalAmount === 0) ||
                                (range === 'endingAt' && !rangeDate)
                            }
                            rangeDate={rangeDate}
                            refetch={refetch}
                            branchId={
                                selectedBranchId !== 'all'
                                    ? selectedBranchId
                                    : undefined
                            }
                        />
                    </FlexBox>
                </Stack>
            </Card>
            <CustomSnackBar
                message={error as string}
                severity="error"
                onRetry={refetch}
            />
        </>
    );
};

export default PayBalance;
