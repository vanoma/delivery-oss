import React, { ReactElement, useContext, useRef, useState } from 'react';
import {
    Button,
    Box,
    Divider,
    Typography,
    IconButton,
    MenuItem,
    ListItemIcon,
    ListItemText,
} from '@mui/material';
import DarkModeIcon from '@mui/icons-material/DarkMode';
import LightModeIcon from '@mui/icons-material/LightMode';
import { DarkModeContext } from '@vanoma/ui-theme';
import { localizePhoneNumber } from '@vanoma/helpers';
import { LoadingIndicator, MenuPopover } from '@vanoma/ui-components';
import { useSelector } from 'react-redux';
import CustomAvatar from '../CustomAvatar';
import { useTypedDispatch } from '../../../redux/typedHooks';
import {
    signOut,
    selectUser,
    selectIsLoading,
} from '../../../redux/slices/authenticationSlice';

const AccountPopover = (): ReactElement => {
    const anchorRef = useRef(null);
    const dispatch = useTypedDispatch();
    const darkModeContext = useContext(DarkModeContext);
    const [open, setOpen] = useState(false);
    const user = useSelector(selectUser);
    const isLoading = useSelector(selectIsLoading);

    const handleOpen = (): void => {
        setOpen(true);
    };
    const handleClose = (): void => {
        setOpen(false);
    };

    return (
        <>
            <IconButton
                ref={anchorRef}
                onClick={handleOpen}
                sx={{
                    p: 1 / 4,
                }}
            >
                <CustomAvatar />
            </IconButton>
            <MenuPopover
                open={open}
                onClose={handleClose}
                anchorRef={anchorRef}
                sx={{ width: 220 }}
            >
                <Box sx={{ my: 1.5, px: 2.5 }}>
                    <Typography variant="subtitle1" noWrap>
                        {`${user?.firstName} ${user?.lastName}`}
                    </Typography>
                    <Typography
                        variant="body2"
                        sx={{ color: 'text.secondary' }}
                        noWrap
                    >
                        {user ? localizePhoneNumber(user!.phoneNumber) : ''}
                    </Typography>
                </Box>
                <Divider sx={{ my: 1 }} />
                <MenuItem
                    onClick={() => {
                        handleClose();
                        darkModeContext.setIsDarkModeOn(
                            !darkModeContext.isDarkModeOn
                        );
                    }}
                    sx={{ py: 1, px: 2.5, height: 48 }}
                >
                    <ListItemIcon>
                        {darkModeContext.isDarkModeOn ? (
                            <LightModeIcon />
                        ) : (
                            <DarkModeIcon />
                        )}
                    </ListItemIcon>
                    <ListItemText primaryTypographyProps={{ variant: 'body2' }}>
                        {`Turn dark mode ${
                            darkModeContext.isDarkModeOn ? 'off' : 'on'
                        }`}
                    </ListItemText>
                </MenuItem>
                <Box sx={{ p: 2, pt: 1.5 }}>
                    <Button
                        fullWidth
                        variant="outlined"
                        size="medium"
                        onClick={() => {
                            handleClose();
                            dispatch(signOut());
                        }}
                        disabled={isLoading}
                    >
                        {isLoading ? <LoadingIndicator /> : 'Sign out'}
                    </Button>
                </Box>
            </MenuPopover>
        </>
    );
};

export default AccountPopover;
