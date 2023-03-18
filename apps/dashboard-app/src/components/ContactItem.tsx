import { ListItemText } from '@mui/material';
import { SxProps, Theme } from '@mui/system';
import React, { ReactElement } from 'react';
import { localizePhoneNumber } from '@vanoma/helpers';
import { Contact } from '@vanoma/types';

const ContactItem = ({
    contact,
    sx,
    secondaryLineHeight,
}: {
    contact: Contact;
    sx?: SxProps<Theme>;
    secondaryLineHeight?: number;
}): ReactElement => {
    const secondary = `${localizePhoneNumber(contact.phoneNumberOne)}${
        contact.phoneNumberTwo
            ? `/${localizePhoneNumber(contact.phoneNumberOne)}`
            : ''
    }`;

    return (
        <ListItemText
            primary={contact.name ?? secondary}
            secondary={(contact.name && secondary) ?? null}
            sx={sx}
            secondaryTypographyProps={{
                lineHeight: secondaryLineHeight,
            }}
        />
    );
};

export default ContactItem;
