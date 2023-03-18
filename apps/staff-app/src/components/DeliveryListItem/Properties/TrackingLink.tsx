import React, { ReactElement } from 'react';
import { Typography, Box, Link } from '@mui/material';
import { CopyToClipboard } from '@vanoma/ui-components';
import { Delivery } from '../../../types';

interface Props {
    delivery: Delivery;
}

const TrackingLink: React.FC<Props> = ({ delivery }): ReactElement => {
    const { trackingLink, trackingNumber } = delivery.package;

    return (
        <Box display="flex" alignItems="center" gap={1}>
            <Box display="flex">
                <Typography variant="subtitle2" mr={1}>
                    Tracking Number:
                </Typography>
                <Link href={trackingLink} target="_blank">
                    {trackingNumber}
                </Link>
            </Box>
            <CopyToClipboard
                value={trackingLink}
                message="Copied to the clipboard!"
            />
        </Box>
    );
};

export default TrackingLink;
