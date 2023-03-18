import React from 'react';
import HighlightOffIcon from '@mui/icons-material/HighlightOff';
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
import { useCancelAssignmentMutation } from '../../../api';
import { Delivery } from '../../../types';
import { DELIVERIES_TAB } from '../../../routeNames';

interface Props {
    delivery: Delivery;
    handleCloseMenu: () => void;
}

const CancelAssignment: React.FC<Props> = ({ delivery, handleCloseMenu }) => {
    const currentPage = useSelector(selectCurrentPage);
    const dispatch = useTypedDispatch();
    const currentTab = useSelector(selectCurrentTab);

    const [cancelAssignment, { isLoading, error }] =
        useCancelAssignmentMutation();

    const disabled = ![DELIVERIES_TAB.ACTIVE].includes(currentTab);
    const isAssigned = delivery.package.assignmentId !== null;

    return (
        <>
            <ActionMenuItem
                icon={<HighlightOffIcon />}
                label="Cancel assignment"
                disabled={disabled || isLoading || !isAssigned}
                onClick={() =>
                    cancelAssignment(delivery.package.assignmentId!)
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

export default CancelAssignment;
