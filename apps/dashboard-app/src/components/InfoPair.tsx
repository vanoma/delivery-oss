import React, { ReactElement } from 'react';
import { Stack, Typography } from '@mui/material';

const InfoPair = ({
    property,
    value,
}: {
    property: string;
    value: string;
}): ReactElement => {
    return (
        <Stack spacing={1} direction="row" sx={{ alignItems: 'end' }}>
            <Typography variant="subtitle2">{`${property}:`}</Typography>
            <Typography color="text.secondary" variant="body2">
                {value}
            </Typography>
        </Stack>
    );
};

export default InfoPair;
