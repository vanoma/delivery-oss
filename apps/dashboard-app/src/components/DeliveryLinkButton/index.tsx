import React, { useState } from 'react';
import { Button } from '@mui/material';
import { Contact } from '@vanoma/types';
import DeliveryLinkModal from './DeliveryLinkModal';

const DeliveryLinkButton: React.FC<{
    toContact?: Contact | null;
    pickUpStart?: string | null;
    fromContactId: string;
    fromAddressId: string;
    resetDelivery?: () => void;
    buttonText: string;
    buttonVariant: 'outlined' | 'contained';
    buttonFullWidth?: boolean;
    startIcon?: React.ReactNode;
    buttonSize?: 'small' | 'medium' | 'large';
}> = ({
    toContact,
    fromContactId,
    fromAddressId,
    pickUpStart,
    resetDelivery,
    buttonText,
    buttonVariant,
    buttonFullWidth = false,
    startIcon,
    buttonSize = 'small',
}) => {
    const [openLink, setOpenLinkGenerator] = useState(false);

    const handleLinkOpen = (): void => {
        setOpenLinkGenerator(true);
    };
    const handleLinkClose = (): void => {
        setOpenLinkGenerator(false);
    };

    return (
        <>
            <Button
                size={buttonSize}
                variant={buttonVariant}
                fullWidth={buttonFullWidth}
                startIcon={startIcon}
                onClick={handleLinkOpen}
            >
                {buttonText}
            </Button>
            {openLink && (
                <DeliveryLinkModal
                    toContact={toContact}
                    pickUpStart={pickUpStart}
                    openLink={openLink}
                    handleLinkClose={handleLinkClose}
                    fromContactId={fromContactId}
                    fromAddressId={fromAddressId}
                    resetDelivery={resetDelivery}
                />
            )}
        </>
    );
};

export default DeliveryLinkButton;
