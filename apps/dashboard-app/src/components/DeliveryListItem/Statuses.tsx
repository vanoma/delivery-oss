import React, { ReactElement } from 'react';
import { Typography, Stack } from '@mui/material';
import { sentenceCase } from 'sentence-case';
import { useTranslation } from 'react-i18next';
import '../../locales/i18n';
import { Package, PackageStatus, PaymentStatus } from '@vanoma/types';
import Label from '../Label';

type colorType =
    | 'primary'
    | 'secondary'
    | 'info'
    | 'success'
    | 'warning'
    | 'error';

type StatusColors = {
    // eslint-disable-next-line no-unused-vars
    [key in PaymentStatus | PackageStatus]: colorType;
};

const statusColors: StatusColors = {
    PAID: 'success',
    COMPLETE: 'success',
    PLACED: 'warning',
    UNPAID: 'warning',
    STARTED: 'warning',
    REQUEST: 'info',
    PENDING: 'warning',
    PARTIAL: 'warning',
    NO_CHARGE: 'info',
    CANCELED: 'error',
    INCOMPLETE: 'error',
};

const getNormalizedTranslationKey = (value: string): string =>
    value
        .toLocaleLowerCase()
        .split('_')
        .map((s, i) => (i === 0 ? s : sentenceCase(s)))
        .join('');

interface Props {
    tab: string;
    delivery: Package;
}

const DeliveryStatus: React.FC<Props> = ({ tab, delivery }): ReactElement => {
    const { t } = useTranslation();
    const { paymentStatus, status } = delivery;

    return (
        <Stack spacing={2} direction="row" sx={{ alignItems: 'center' }}>
            <Typography variant="subtitle2">
                {t('deliveries.order.status')}
            </Typography>
            <Label color={statusColors[paymentStatus]}>
                {sentenceCase(
                    t(
                        `deliveries.order.${getNormalizedTranslationKey(
                            paymentStatus
                        )}`
                    )
                )}
            </Label>
            <Label color={statusColors[status]}>
                {sentenceCase(t(`deliveries.order.${tab}`))}
            </Label>
        </Stack>
    );
};

export default DeliveryStatus;
