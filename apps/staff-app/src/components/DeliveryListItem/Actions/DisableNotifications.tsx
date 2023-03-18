import React from 'react';
import NotificationsOffIcon from '@mui/icons-material/NotificationsOff';
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

const DisableNotifications: React.FC<Props> = ({
    delivery,
    handleCloseMenu,
}) => {
    const currentPage = useSelector(selectCurrentPage);
    const dispatch = useTypedDispatch();
    const currentTab = useSelector(selectCurrentTab);

    const [updatePackage, { isLoading, error }] = useUpdatePackageMutation();

    const { enableNotifications } = delivery.package;

    const disabled =
        ![DELIVERIES_TAB.ACTIVE].includes(currentTab) ||
        isLoading ||
        !enableNotifications;

    return (
        <>
            <ActionMenuItem
                icon={<NotificationsOffIcon />}
                label="Disable notifications"
                disabled={disabled}
                onClick={() =>
                    updatePackage({
                        packageId: delivery.package.packageId,
                        enableNotifications: false,
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

export default DisableNotifications;
