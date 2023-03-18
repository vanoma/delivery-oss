import React, { ReactElement } from 'react';
import { CopyToClipboard } from '@vanoma/ui-components';
import { Typography, Box, Link } from '@mui/material';
import { Delivery } from '../../../types';

interface Props {
    delivery: Delivery;
}

const DeliveryLink: React.FC<Props> = ({ delivery }): ReactElement => {
    const { deliveryLink } = delivery.package.deliveryOrder;

    return (
        <Box display="flex" alignItems="center" gap={1}>
            <Box display="flex">
                <Typography variant="subtitle2" mr={1}>
                    Delivery link:
                </Typography>
                <Link href={deliveryLink} target="_blank">
                    Open in new tab
                </Link>
            </Box>
            <CopyToClipboard
                value={deliveryLink}
                message="Copied to the clipboard!"
            />
        </Box>
    );
};

export default DeliveryLink;
