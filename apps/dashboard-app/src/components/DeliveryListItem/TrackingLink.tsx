import React, { ReactElement } from 'react';
import { Typography, Box, Link } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { CopyToClipboard } from '@vanoma/ui-components';
import { Package } from '@vanoma/types';

interface Props {
    delivery: Package;
}

const Property: React.FC<Props> = ({ delivery }): ReactElement => {
    const { t } = useTranslation();

    return (
        <Box display="flex" alignItems="center" gap={1}>
            <Box display="flex">
                <Typography variant="subtitle2" mr={1}>
                    {`${t('deliveries.order.trackingNumber')}:`}
                </Typography>
                <Link href={delivery.trackingLink} target="_blank">
                    {delivery.trackingNumber}
                </Link>
            </Box>
            <CopyToClipboard
                value={delivery.trackingLink}
                message={t('deliveries.order.copiedToTheClipboard')}
            />
        </Box>
    );
};

export default Property;
