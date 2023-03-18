import React, { FC } from 'react';
import { ToggleButton, ToggleButtonGroup } from '@mui/material';
import { useSelector } from 'react-redux';
import { PaymentStatus } from '@vanoma/types';
import { DELIVERIES_TAB } from '../../routeNames';
import { useTypedDispatch } from '../../redux/typedHooks';
import {
    changePaymentStatus,
    selectPaymentStatus,
} from '../../redux/slices/deliveriesSlice';

interface Props {
    tab: string;
}

const PaymentStatusFilter: FC<Props> = ({ tab }) => {
    const dispatch = useTypedDispatch();
    const paymentStatus = useSelector(selectPaymentStatus);

    return (
        <>
            {(tab === DELIVERIES_TAB.ACTIVE ||
                tab === DELIVERIES_TAB.COMPLETE ||
                tab === DELIVERIES_TAB.SEARCH) && (
                <ToggleButtonGroup
                    color="primary"
                    value={paymentStatus}
                    exclusive
                    size="small"
                    onChange={(e, value) => {
                        dispatch(changePaymentStatus(value));
                    }}
                >
                    <ToggleButton value={PaymentStatus.PAID}>
                        {PaymentStatus.PAID}
                    </ToggleButton>
                    <ToggleButton value={PaymentStatus.UNPAID}>
                        {PaymentStatus.UNPAID}
                    </ToggleButton>
                </ToggleButtonGroup>
            )}
        </>
    );
};

export default PaymentStatusFilter;
