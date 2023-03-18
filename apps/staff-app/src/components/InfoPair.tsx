import React, { ReactElement } from 'react';
import { Stack, Typography } from '@mui/material';

const InfoPair = ({
    property,
    value,
    isValueBold,
}: {
    property: string;
    value: string;
    isValueBold?: boolean;
}): ReactElement => {
    return (
        <Stack spacing={1} direction="row" sx={{ alignItems: 'end' }}>
            <Typography variant="subtitle2">{`${property}:`}</Typography>
            <Typography
                color={!isValueBold ? 'text.secondary' : undefined}
                variant={isValueBold ? 'subtitle2' : 'body2'}
            >
                {value}
            </Typography>
        </Stack>
    );
};

export default InfoPair;
