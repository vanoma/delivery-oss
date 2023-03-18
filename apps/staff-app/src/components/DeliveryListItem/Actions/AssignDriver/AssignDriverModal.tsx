import React, { ReactElement } from 'react';
import { Stack } from '@mui/material';
import { CustomModal } from '@vanoma/ui-components';
import { Delivery } from '../../../../types';
import DriversMap from '../../../DriversMap';
import DriversSelector from './DriversSelector';

interface Props {
    open: boolean;
    delivery: Delivery;
    isAssigned: boolean;
    handleClose: () => void;
}

const AssignDriverModal: React.FC<Props> = ({
    open,
    delivery,
    isAssigned,
    handleClose,
}): ReactElement => {
    return (
        <CustomModal
            open={open}
            handleClose={handleClose}
            sx={{ width: '90%', height: '90%', p: 0 }}
        >
            <Stack direction="column" width="100%" height="100%">
                <DriversMap />
                <DriversSelector
                    delivery={delivery}
                    isAssigned={isAssigned}
                    closeActionModal={handleClose}
                />
            </Stack>
        </CustomModal>
    );
};

export default AssignDriverModal;
