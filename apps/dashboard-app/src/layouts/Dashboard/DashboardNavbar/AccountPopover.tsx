import React, { ReactElement, useContext, useRef, useState } from 'react';
import {
    Button,
    Box,
    Divider,
    Typography,
    Avatar,
    IconButton,
    MenuItem,
    ListItemIcon,
    ListItemText,
    LinearProgress,
} from '@mui/material';
import DarkModeIcon from '@mui/icons-material/DarkMode';
import LightModeIcon from '@mui/icons-material/LightMode';
import { useTranslation } from 'react-i18next';
import { DarkModeContext } from '@vanoma/ui-theme';
import { useSelector } from 'react-redux';
import { LoadingIndicator } from '@vanoma/ui-components';
import { localizePhoneNumber } from '@vanoma/helpers';
import {
    signOut,
    selectError,
    selectIsLoading,
    selectBusinessName,
    selectAgent,
} from '../../../redux/slices/authenticationSlice';
import { useTypedDispatch } from '../../../helpers/reduxToolkit';
import MenuPopover from '../../../components/MenuPopover';
import CustomSnackBar from '../../../components/CustomSnackBar';
import BranchSelector from '../../../components/BranchSelector';
import { useUpdateAgentMutation } from '../../../api';

const AccountPopover = (): ReactElement => {
    const anchorRef = useRef(null);
    const dispatch = useTypedDispatch();
    const darkModeContext = useContext(DarkModeContext);
    const [open, setOpen] = useState(false);
    const businessName = useSelector(selectBusinessName);
    const isLoading = useSelector(selectIsLoading);
    const error = useSelector(selectError);
    const agent = useSelector(selectAgent);
    const { t } = useTranslation();

    const [
        updateAgent,
        { isLoading: isLoadingUpdateAgent, error: updateError },
    ] = useUpdateAgentMutation();

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
                <Avatar>{`${businessName?.charAt(0)}`}</Avatar>
            </IconButton>
            <MenuPopover
                open={open}
                onClose={handleClose}
                anchorRef={anchorRef}
                sx={{ width: 240 }}
            >
                <Box sx={{ my: 1.5, px: 2 }}>
                    <Typography variant="subtitle1" noWrap>
                        {agent!.fullName}
                    </Typography>
                    <Typography
                        variant="body2"
                        sx={{ color: 'text.secondary' }}
                        noWrap
                        mb={1}
                    >
                        {localizePhoneNumber(agent!.phoneNumber)}
                    </Typography>
                    <BranchSelector
                        value={agent!.branch?.branchId ?? ''}
                        onChange={(value) =>
                            updateAgent({
                                agentId: agent!.agentId,
                                fullName: agent!.fullName,
                                branchId: value,
                            })
                                .unwrap()
                                .then(() => handleClose)
                        }
                        disabled={isLoadingUpdateAgent}
                    />
                </Box>
                {isLoadingUpdateAgent && <LinearProgress sx={{ my: 0.8125 }} />}
                {!isLoadingUpdateAgent && (
                    <Divider sx={{ my: 0.8125, py: 0.2 }} />
                )}
                <MenuItem
                    onClick={() => {
                        handleClose();
                        darkModeContext.setIsDarkModeOn(
                            !darkModeContext.isDarkModeOn
                        );
                    }}
                    sx={{ py: 1, px: 2, height: 48 }}
                >
                    <ListItemIcon>
                        {darkModeContext.isDarkModeOn ? (
                            <LightModeIcon />
                        ) : (
                            <DarkModeIcon />
                        )}
                    </ListItemIcon>
                    <ListItemText primaryTypographyProps={{ variant: 'body2' }}>
                        {darkModeContext.isDarkModeOn
                            ? t('dashboard.navbar.turnDarkModeOff')
                            : t('dashboard.navbar.turnDarkModeOn')}
                    </ListItemText>
                </MenuItem>
                <Box sx={{ p: 2, pt: 1.5 }}>
                    <Button
                        fullWidth
                        variant="outlined"
                        size="medium"
                        onClick={() => dispatch(signOut())}
                        disabled={isLoading}
                    >
                        {isLoading ? (
                            <LoadingIndicator />
                        ) : (
                            t('dashboard.navbar.signOut')
                        )}
                    </Button>
                </Box>
                <CustomSnackBar
                    message={error || (updateError as string)}
                    severity="error"
                />
            </MenuPopover>
        </>
    );
};

export default AccountPopover;
