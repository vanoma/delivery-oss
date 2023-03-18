import React from 'react';
import StartIcon from '@mui/icons-material/Start';
import { useSelector } from 'react-redux';
import { LinearProgress } from '@mui/material';
import { CustomSnackBar } from '@vanoma/ui-components';
import ActionMenuItem from './ActionMenuItem';
import { useTypedDispatch } from '../../../redux/typedHooks';
import {
    changeCurrentPage,
    selectCurrentTab,
} from '../../../redux/slices/deliveriesSlice';
import { usePlaceDeliveryOrderMutation } from '../../../api';
import { Delivery } from '../../../types';
import { DELIVERIES_TAB } from '../../../routeNames';

interface Props {
    delivery: Delivery;
    handleCloseMenu: () => void;
}

const PlaceDelivery: React.FC<Props> = ({ delivery, handleCloseMenu }) => {
    const dispatch = useTypedDispatch();
    const currentTab = useSelector(selectCurrentTab);

    const [placeDelivery, { isLoading, error }] =
        usePlaceDeliveryOrderMutation();

    const disabled = ![
        DELIVERIES_TAB.REQUEST,
        DELIVERIES_TAB.DRAFT,
        DELIVERIES_TAB.PENDING,
    ].includes(currentTab);

    return (
        <>
            <ActionMenuItem
                icon={<StartIcon />}
                label="Place Delivery"
                disabled={disabled || isLoading}
                onClick={() =>
                    placeDelivery(
                        delivery.package.deliveryOrder.deliveryOrderId
                    )
                        .unwrap()
                        .then(() => {
                            dispatch(changeCurrentPage(1));
                            handleCloseMenu();
                        })
                }
            />
            {isLoading && <LinearProgress />}
            <CustomSnackBar message={error as string} severity="error" />
        </>
    );
};

export default PlaceDelivery;
