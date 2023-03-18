import React, { useState } from 'react';
import CreditScoreIcon from '@mui/icons-material/CreditScore';
import { PaymentStatus } from '@vanoma/types';
import { Delivery } from '../../../../types';
import ActionMenuItem from '../ActionMenuItem';
import ConfirmPaymentModal from './ConfirmPaymentModal';

interface Props {
    delivery: Delivery;
    handleCloseMenu: () => void;
}

const ConfirmPayment: React.FC<Props> = ({ delivery, handleCloseMenu }) => {
    const [openModal, setOpenModal] = useState<boolean>(false);

    const disabled = ![PaymentStatus.UNPAID, PaymentStatus.PARTIAL].includes(
        delivery.package.paymentStatus
    );

    const handleOpenModal = (): void => setOpenModal(true);
    const handleCloseModal = (): void => {
        setOpenModal(false);
        handleCloseMenu();
    };

    return (
        <>
            <ActionMenuItem
                icon={<CreditScoreIcon />}
                label="Confirm Payment"
                disabled={disabled}
                onClick={handleOpenModal}
            />
            {openModal && (
                <ConfirmPaymentModal
                    open={openModal}
                    delivery={delivery}
                    handleClose={handleCloseModal}
                />
            )}
        </>
    );
};

export default ConfirmPayment;
