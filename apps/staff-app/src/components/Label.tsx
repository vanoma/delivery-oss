import React, { ReactNode } from 'react';
import { styled } from '@mui/material/styles';
import { alpha } from '@mui/system';
import { Theme } from '@mui/material';

type colorType =
    | 'primary'
    | 'secondary'
    | 'info'
    | 'success'
    | 'warning'
    | 'error';

const RootStyle = styled('span')(
    ({ theme, color }: { theme?: Theme; color: colorType }) => ({
        height: 22,
        minWidth: 22,
        lineHeight: 0,
        borderRadius: 16,
        cursor: 'default',
        alignItems: 'center',
        whiteSpace: 'nowrap',
        display: 'inline-flex',
        justifyContent: 'center',
        padding: theme!.spacing(0, 1),
        fontSize: theme!.typography.pxToRem(12),
        fontFamily: theme!.typography.fontFamily,
        fontWeight: theme!.typography.fontWeightBold,
        color: theme!.palette[color].dark,
        backgroundColor: alpha(theme!.palette[color].main, 0.16),
    })
);

export default function Label({
    color,
    children,
}: {
    color: colorType;
    children: ReactNode;
}): JSX.Element {
    return <RootStyle color={color}>{children}</RootStyle>;
}
