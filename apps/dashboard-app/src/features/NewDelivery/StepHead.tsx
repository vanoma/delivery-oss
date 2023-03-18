/* eslint-disable no-nested-ternary */
import React, { ReactElement, ReactNode } from 'react';
import { Stack, Typography, useTheme } from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CircleOutlinedIcon from '@mui/icons-material/CircleOutlined';
import RadioButtonCheckedIcon from '@mui/icons-material/RadioButtonChecked';

const StepHead = ({
    title,
    done,
    current,
    children,
}: {
    title: string;
    done: boolean;
    current: boolean;
    children?: ReactNode;
}): ReactElement => {
    const theme = useTheme();

    return (
        <Stack direction="row" spacing={3.375} sx={{ alignItems: 'center' }}>
            {current ? (
                <RadioButtonCheckedIcon color="primary" />
            ) : done ? (
                <CheckCircleIcon color="primary" />
            ) : (
                <CircleOutlinedIcon
                    sx={{ color: theme.palette.primary.light }}
                />
            )}
            <Stack
                direction="row"
                sx={{
                    justifyContent: 'space-between',
                    flexGrow: 1,
                    alignItems: 'center',
                    pr: 1.875,
                }}
            >
                <Typography variant="h5" pt={0.5}>
                    {title}
                </Typography>
                {children}
            </Stack>
        </Stack>
    );
};

export default StepHead;
