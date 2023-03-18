import React, { useState } from 'react';
import { useSelector } from 'react-redux';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import { Delivery } from '../../../../types';
import { selectCurrentTab } from '../../../../redux/slices/deliveriesSlice';
import { DELIVERIES_TAB } from '../../../../routeNames';
import ActionMenuItem from '../ActionMenuItem';
import DuplicateDeliveryModal from './DuplicateDeliveryModal';

interface Props {
    delivery: Delivery;
    handleCloseMenu: () => void;
}

const DuplicateDelivery: React.FC<Props> = ({ delivery, handleCloseMenu }) => {
    const [openModal, setOpenModal] = useState<boolean>(false);
    const currentTab = useSelector(selectCurrentTab);

    const disabled = !(
        [
            DELIVERIES_TAB.ACTIVE,
            DELIVERIES_TAB.COMPLETE,
            DELIVERIES_TAB.SEARCH,
        ].includes(currentTab) && delivery.package.toAddress !== null
    );

    const handleOpenModal = (): void => setOpenModal(true);
    const handleCloseModal = (): void => {
        setOpenModal(false);
        handleCloseMenu();
    };
    return (
        <>
            <ActionMenuItem
                icon={<ContentCopyIcon />}
                label="Duplicate order"
                disabled={disabled}
                onClick={handleOpenModal}
            />
            {openModal && (
                <DuplicateDeliveryModal
                    open={openModal}
                    delivery={delivery}
                    handleClose={handleCloseModal}
                />
            )}
        </>
    );
};

export default DuplicateDelivery;
