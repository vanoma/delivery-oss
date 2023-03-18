import React, { ReactElement, useRef, useState } from 'react';
import {
    Card,
    Collapse,
    Divider,
    IconButton,
    ListItem,
    ListItemText,
    MenuItem,
    ListItemIcon,
} from '@mui/material';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';
import { useTranslation } from 'react-i18next';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import MoreHorizIcon from '@mui/icons-material/MoreHoriz';
import { useSelector } from 'react-redux';
import { Contact } from '@vanoma/types';
import '../../locales/i18n';
import ContactListItemText from '../../components/ContactItem';
import MenuPopover from '../../components/MenuPopover';
import Addresses from './Addresses';
import {
    selectAddressId,
    selectAgent,
    selectContactId,
} from '../../redux/slices/authenticationSlice';
import DeliveryLinkButton from '../../components/DeliveryLinkButton';

const ContactView = ({
    contact,
    handleContactOpen,
    handleDeleteAddressOpen,
    handleDeleteContactOpen,
}: {
    contact: Contact;
    handleContactOpen: () => void;
    // eslint-disable-next-line no-unused-vars
    handleDeleteAddressOpen: (contactIdAndAddressId: {
        contactId: string;
        addressId: string;
    }) => void;
    // eslint-disable-next-line no-unused-vars
    handleDeleteContactOpen: (contactId: string) => void;
}): ReactElement => {
    const [expanded, setExpanded] = useState(false);
    const [openMenu, setOpenMenu] = useState(false);
    const { t } = useTranslation();
    const anchorRef = useRef(null);

    const defaultContactId = useSelector(selectContactId);
    const defaultAddressId = useSelector(selectAddressId);
    const agent = useSelector(selectAgent);

    const handleMenuOpen = (): void => {
        setOpenMenu(true);
    };
    const handleMenuClose = (): void => {
        setOpenMenu(false);
    };

    return (
        <Card sx={{ mb: 2 }}>
            <ListItem>
                <ContactListItemText contact={contact} />
                <DeliveryLinkButton
                    fromContactId={defaultContactId!}
                    fromAddressId={
                        agent!.branch?.address.addressId ?? defaultAddressId!
                    }
                    buttonText={t('customers.contact.link')}
                    buttonVariant="outlined"
                    toContact={contact}
                />
                <IconButton
                    size="small"
                    sx={{ p: 0.5, ml: 1 }}
                    ref={anchorRef}
                    onClick={handleMenuOpen}
                >
                    <MoreHorizIcon />
                </IconButton>
                <IconButton
                    sx={{ p: 0.5, ml: 1 }}
                    onClick={() => setExpanded(!expanded)}
                >
                    {expanded ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                </IconButton>
            </ListItem>
            <MenuPopover
                open={openMenu}
                onClose={handleMenuClose}
                anchorRef={anchorRef}
                sx={{ py: 1.25 }}
            >
                <MenuItem
                    sx={{ py: 1, px: 2.5, height: 48 }}
                    onClick={() => {
                        handleMenuClose();
                        handleContactOpen();
                    }}
                >
                    <ListItemIcon>
                        <EditIcon />
                    </ListItemIcon>
                    <ListItemText
                        primaryTypographyProps={{
                            variant: 'body2',
                        }}
                    >
                        {t('customers.contact.edit')}
                    </ListItemText>
                </MenuItem>
                <MenuItem
                    sx={{ py: 1, px: 2.5, height: 48 }}
                    onClick={() => handleDeleteContactOpen(contact.contactId)}
                >
                    <ListItemIcon>
                        <DeleteIcon />
                    </ListItemIcon>
                    <ListItemText
                        primaryTypographyProps={{
                            variant: 'body2',
                        }}
                    >
                        {t('customers.contact.delete')}
                    </ListItemText>
                </MenuItem>
            </MenuPopover>
            <Collapse in={expanded}>
                <Divider />
                {expanded && (
                    <Addresses
                        contactId={contact.contactId}
                        handleDeleteAddressOpen={handleDeleteAddressOpen}
                    />
                )}
            </Collapse>
        </Card>
    );
};

export default ContactView;
