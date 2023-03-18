import React from 'react';
import { styled } from '@mui/material/styles';
import logo from '../../public/assets/logo.png';

const LogoWrapper = styled('div')(() => ({
    background: `url(${logo}) no-repeat`,
    backgroundSize: 'cover',
    height: 40,
    width: 150.72,
}));

export default function Logo(): JSX.Element {
    return <LogoWrapper />;
}
