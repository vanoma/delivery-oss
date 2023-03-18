import React, { useState } from 'react';
import { useSelector } from 'react-redux';
import AutorenewIcon from '@mui/icons-material/Autorenew';
import AssignmentIndIcon from '@mui/icons-material/AssignmentInd';
import { Delivery } from '../../../../types';
import { selectCurrentTab } from '../../../../redux/slices/deliveriesSlice';
import { DELIVERIES_TAB } from '../../../../routeNames';
import ActionMenuItem from '../ActionMenuItem';
import AssignDriverModal from './AssignDriverModal';

interface Props {
    delivery: Delivery;
    handleCloseMenu: () => void;
}

const AssignDriver: React.FC<Props> = ({ delivery, handleCloseMenu }) => {
    const [openModal, setOpenModal] = useState<boolean>(false);
    const currentTab = useSelector(selectCurrentTab);

    const disabled = ![DELIVERIES_TAB.ACTIVE].includes(currentTab);
    const isAssigned = delivery.package.assignmentId !== null;

    const handleOpenModal = (): void => setOpenModal(true);
    const handleCloseModal = (): void => {
        setOpenModal(false);
        handleCloseMenu();
    };

    return (
        <>
            <ActionMenuItem
                icon={isAssigned ? <AutorenewIcon /> : <AssignmentIndIcon />}
                label={isAssigned ? 'Re-assign driver' : 'Assign driver'}
                disabled={disabled}
                onClick={handleOpenModal}
            />
            {openModal && (
                <AssignDriverModal
                    open={openModal}
                    delivery={delivery}
                    isAssigned={isAssigned}
                    handleClose={handleCloseModal}
                />
            )}
        </>
    );
};

export default AssignDriver;
