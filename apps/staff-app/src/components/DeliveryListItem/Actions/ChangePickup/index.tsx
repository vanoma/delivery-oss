import React, { useState } from 'react';
import { useSelector } from 'react-redux';
import MoreTimeIcon from '@mui/icons-material/MoreTime';
import { Delivery } from '../../../../types';
import { selectCurrentTab } from '../../../../redux/slices/deliveriesSlice';
import { DELIVERIES_TAB } from '../../../../routeNames';
import ActionMenuItem from '../ActionMenuItem';
import ChangePickupModal from './ChangePickupModal';

interface Props {
    delivery: Delivery;
    handleCloseMenu: () => void;
}

const ChangePickup: React.FC<Props> = ({ delivery, handleCloseMenu }) => {
    const [openModal, setOpenModal] = useState<boolean>(false);
    const currentTab = useSelector(selectCurrentTab);

    const disabled = [DELIVERIES_TAB.COMPLETE].includes(currentTab);

    const handleOpenModal = (): void => setOpenModal(true);
    const handleCloseModal = (): void => {
        setOpenModal(false);
        handleCloseMenu();
    };

    return (
        <>
            <ActionMenuItem
                icon={<MoreTimeIcon />}
                label="Update Pickup"
                disabled={disabled}
                onClick={handleOpenModal}
            />
            {openModal && (
                <ChangePickupModal
                    open={openModal}
                    delivery={delivery}
                    handleClose={handleCloseModal}
                />
            )}
        </>
    );
};

export default ChangePickup;
