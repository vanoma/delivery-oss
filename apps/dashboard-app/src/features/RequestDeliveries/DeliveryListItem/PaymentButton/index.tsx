import React, { useState } from 'react';
import { Box, Button } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { Package } from '@vanoma/types';
import PaymentModal from './PaymentModal';
import InfoPair from '../../../../components/InfoPair';

const PaymentButton: React.FC<{
    delivery: Package;
}> = ({ delivery }) => {
    const [open, setOpen] = useState(false);
    const { t } = useTranslation();

    const handleOpen = (): void => {
        setOpen(true);
    };
    const handleClose = (): void => {
        setOpen(false);
    };

    return (
        <Box sx={{ mt: 1 }}>
            <InfoPair
                property={t('deliveries.order.price')}
                value={t('deliveries.order.priceWithCurrency', {
                    price: Math.ceil(delivery.totalAmount),
                })}
            />
            <Button size="small" onClick={handleOpen} sx={{ mt: 2 }}>
                {t('delivery.payment.pay')}
            </Button>
            {open && (
                <PaymentModal
                    open={open}
                    delivery={delivery}
                    handleClose={handleClose}
                />
            )}
        </Box>
    );
};

export default PaymentButton;
