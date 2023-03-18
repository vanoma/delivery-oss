import React from 'react';
import StopCircleIcon from '@mui/icons-material/StopCircle';
import { useSelector } from 'react-redux';
import { LinearProgress } from '@mui/material';
import { CustomSnackBar } from '@vanoma/ui-components';
import ActionMenuItem from './ActionMenuItem';
import { useTypedDispatch } from '../../../redux/typedHooks';
import {
    changeCurrentPage,
    selectCurrentPage,
    selectCurrentTab,
} from '../../../redux/slices/deliveriesSlice';
import { useUpdatePackageMutation } from '../../../api';
import { Delivery } from '../../../types';
import { DELIVERIES_TAB } from '../../../routeNames';

interface Props {
    delivery: Delivery;
    handleCloseMenu: () => void;
}

const FreezeAssignment: React.FC<Props> = ({ delivery, handleCloseMenu }) => {
    const currentPage = useSelector(selectCurrentPage);
    const dispatch = useTypedDispatch();
    const currentTab = useSelector(selectCurrentTab);

    const [updatePackage, { isLoading, error }] = useUpdatePackageMutation();

    const disabled = ![DELIVERIES_TAB.ACTIVE].includes(currentTab);
    const isAssigned = delivery.package.driverId !== null;
    const { isAssignable } = delivery.package;

    return (
        <>
            <ActionMenuItem
                icon={<StopCircleIcon />}
                label="Freeze assignment"
                disabled={disabled || isLoading || isAssigned || !isAssignable}
                onClick={() =>
                    updatePackage({
                        packageId: delivery.package.packageId,
                        isAssignable: false,
                    })
                        .unwrap()
                        .then(() => {
                            dispatch(changeCurrentPage(currentPage));
                            handleCloseMenu();
                        })
                }
            />
            {isLoading && <LinearProgress />}
            <CustomSnackBar message={error as string} severity="error" />
        </>
    );
};

export default FreezeAssignment;
