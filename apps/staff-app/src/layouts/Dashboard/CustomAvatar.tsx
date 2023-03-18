import React, { ReactElement } from 'react';
import { Avatar } from '@mui/material';
import { useSelector } from 'react-redux';
import { selectUser } from '../../redux/slices/authenticationSlice';

export default function CustomAvatar(): ReactElement {
    const user = useSelector(selectUser);

    const charOne = user?.firstName.charAt(0) ?? '';
    const charTwo = user?.lastName.charAt(0) ?? '';

    return <Avatar>{`${charOne}${charTwo}`}</Avatar>;
}
