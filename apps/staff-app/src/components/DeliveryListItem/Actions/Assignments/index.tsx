import React, { useState } from 'react';
import AssignmentIcon from '@mui/icons-material/Assignment';
import { PackageStatus } from '@vanoma/types';
import { Delivery } from '../../../../types';
import ActionMenuItem from '../ActionMenuItem';
import AssignmentsModal from './AssignmentsModal';

interface Props {
    delivery: Delivery;
    handleCloseMenu: () => void;
}

const Assignments: React.FC<Props> = ({ delivery, handleCloseMenu }) => {
    const [openModal, setOpenModal] = useState<boolean>(false);

    const disabled = ![PackageStatus.PLACED, PackageStatus.COMPLETE].includes(
        delivery.package.status
    );

    const handleOpenModal = (): void => setOpenModal(true);
    const handleCloseModal = (): void => {
        setOpenModal(false);
        handleCloseMenu();
    };

    return (
        <>
            <ActionMenuItem
                icon={<AssignmentIcon />}
                label="Assignments"
                disabled={disabled}
                onClick={handleOpenModal}
            />
            {openModal && (
                <AssignmentsModal
                    open={openModal}
                    handleClose={handleCloseModal}
                    delivery={delivery}
                />
            )}
        </>
    );
};

export default Assignments;
