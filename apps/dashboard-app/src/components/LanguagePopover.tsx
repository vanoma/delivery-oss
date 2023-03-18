import React, { ReactElement, useRef, useState } from 'react';
import { alpha } from '@mui/system';
import {
    Box,
    MenuItem,
    ListItemIcon,
    ListItemText,
    IconButton,
    Tooltip,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import Flags from 'country-flag-icons/react/3x2';
import MenuPopover from './MenuPopover';
import i18n from '../locales/i18n';
import storage from '../services/storage';

const languages = [
    {
        value: 'en',
        label: 'English',
        icon: <Flags.GB title="" />,
    },
    {
        value: 'rw',
        label: 'Kinyarwanda',
        icon: <Flags.RW title="" />,
    },
    {
        value: 'fr',
        label: 'French',
        icon: <Flags.FR title="" />,
    },
];

const LanguagePopover = (): ReactElement => {
    const anchorRef = useRef(null);
    const { t } = useTranslation();
    const [open, setOpen] = useState(false);

    const handleOpen = (): void => {
        setOpen(true);
    };

    const handleClose = (): void => {
        setOpen(false);
    };

    const handleLanguageSelection = (language: string): void => {
        i18n.changeLanguage(language);
        handleClose();
        storage.setItem('chosenLanguage', language);
    };

    return (
        <>
            <Tooltip title={t('dashboard.navbar.languages')!}>
                <IconButton
                    ref={anchorRef}
                    onClick={handleOpen}
                    sx={{
                        padding: 0,
                        width: 44,
                        height: 44,
                        ...(open && {
                            bgcolor: (theme) =>
                                alpha(
                                    theme.palette.primary.main,
                                    theme.palette.action.focusOpacity
                                ),
                        }),
                    }}
                >
                    <span style={{ width: 24 }}>
                        {
                            languages.find((lan) => lan.value === i18n.language)
                                ?.icon
                        }
                    </span>
                </IconButton>
            </Tooltip>
            <MenuPopover
                open={open}
                onClose={handleClose}
                anchorRef={anchorRef}
            >
                <Box sx={{ py: 1 }}>
                    {languages.map((option) => (
                        <MenuItem
                            key={option.value}
                            selected={option.value === i18n.language}
                            onClick={() =>
                                handleLanguageSelection(option.value)
                            }
                            sx={{ py: 1, px: 2.5, height: 48 }}
                        >
                            <ListItemIcon sx={{ mr: 2 }}>
                                <span style={{ width: 32, height: 24 }}>
                                    {option.icon}
                                </span>
                            </ListItemIcon>
                            <ListItemText
                                primaryTypographyProps={{ variant: 'body2' }}
                            >
                                {option.label}
                            </ListItemText>
                        </MenuItem>
                    ))}
                </Box>
            </MenuPopover>
        </>
    );
};

export default LanguagePopover;
