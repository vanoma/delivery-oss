import React from 'react';
import { Typography } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { DeliveryOrderPricing } from '@vanoma/types';

interface Props {
    pricing: DeliveryOrderPricing;
}

const PriceInfo: React.FC<Props> = ({ pricing }) => {
    const { t } = useTranslation();

    return (
        <Typography>
            {t('delivery.payment.priceWithCurrency', {
                price: pricing.totalAmount,
            })}
        </Typography>
    );
};

export default PriceInfo;
