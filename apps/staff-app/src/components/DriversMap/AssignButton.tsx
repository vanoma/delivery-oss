import { Button } from '@mui/material';
import { Driver } from '@vanoma/types';
import { CustomSnackBar, LoadingIndicator } from '@vanoma/ui-components';
import React from 'react';
import { useCreateAssignmentMutation } from '../../api';
import { getDeliveries } from '../../redux/slices/deliveriesSlice';
import { useTypedDispatch } from '../../redux/typedHooks';
import { Delivery } from '../../types';

const AssignButton: React.FC<{
    driver: Driver;
    delivery: Delivery;
    handleOpen?: () => void;
    handleClose?: () => void;
}> = ({ driver, delivery, handleOpen, handleClose }) => {
    const dispatch = useTypedDispatch();
    const [createAssignment, { error, isLoading }] =
        useCreateAssignmentMutation();

    return (
        <>
            <Button
                size="medium"
                onClick={() => {
                    createAssignment({
                        driverId: driver.driverId,
                        packageId: delivery.package.packageId,
                    })
                        .unwrap()
                        .then(() => {
                            dispatch(getDeliveries(handleOpen));
                            if (handleClose) handleClose();
                        })
                        // eslint-disable-next-line no-unused-vars
                        .catch((_) => {});
                }}
                disabled={isLoading}
            >
                {isLoading ? <LoadingIndicator /> : 'Assign'}
            </Button>
            <CustomSnackBar message={error as string} severity="error" />
        </>
    );
};

export default AssignButton;
