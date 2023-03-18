import React from 'react';
import { Box, Grid, IconButton, ListItemText } from '@mui/material';
import LocationOnOutlinedIcon from '@mui/icons-material/LocationOnOutlined';
import PersonOutlineOutlinedIcon from '@mui/icons-material/PersonOutlineOutlined';
import EditIcon from '@mui/icons-material/Edit';
import { styled } from '@mui/system';
import { localizePhoneNumber } from '@vanoma/helpers';
import { Contact, Address } from '@vanoma/types';
import SelectedInfo from './SelectedInfo';
import { houseNumberAndStreetName } from '../helpers/address';

const ContactListItemText = styled(ListItemText)(({ theme }) => ({
    [theme.breakpoints.down('md')]: {
        width: '100%',
    },
}));

const SelectedContact: React.FC<{
    isEditing: boolean;
    contact: Contact;
    address: Address | null;
    disabled: boolean;
    resetContact: () => void;
    resetAddress: () => void;
}> = ({
    isEditing,
    contact,
    address,
    disabled,
    resetContact,
    resetAddress,
}) => {
    return (
        <SelectedInfo
            sx={{
                display: { xs: 'block', sm: 'flex' },
                py: 0,
            }}
        >
            <Grid container>
                <Grid item xs={12} sm={address !== null ? 6 : 12}>
                    <Box
                        sx={{
                            display: 'flex',
                            justifyContent: 'flex-start',
                            alignItems: 'center',
                            marginRight: {
                                xs: 0,
                                md: address !== null ? 6 : 0,
                            },
                        }}
                    >
                        <PersonOutlineOutlinedIcon sx={{ mr: 2 }} />
                        <ContactListItemText
                            primary={contact.name}
                            secondary={localizePhoneNumber(
                                contact.phoneNumberOne
                            )}
                        />
                        {isEditing && (
                            <IconButton
                                onClick={resetContact}
                                sx={{
                                    height: 40,
                                    my: 1,
                                }}
                                disabled={disabled}
                            >
                                <EditIcon />
                            </IconButton>
                        )}
                    </Box>
                </Grid>
            </Grid>
            {address !== null && (
                <Grid item xs={12} sm={6}>
                    <Box
                        sx={{
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center',
                        }}
                    >
                        <Box
                            sx={{
                                display: 'flex',
                                justifyContent: 'center',
                                alignItems: 'center',
                            }}
                        >
                            <LocationOnOutlinedIcon sx={{ mr: 2 }} />
                            <ContactListItemText
                                primary={address.addressName}
                                primaryTypographyProps={{
                                    variant: 'body2',
                                }}
                                secondary={houseNumberAndStreetName(address)}
                            />
                        </Box>
                        {isEditing && (
                            <IconButton
                                onClick={resetAddress}
                                sx={{
                                    height: 40,
                                }}
                                disabled={disabled}
                            >
                                <EditIcon />
                            </IconButton>
                        )}
                    </Box>
                </Grid>
            )}
        </SelectedInfo>
    );
};

export default SelectedContact;
