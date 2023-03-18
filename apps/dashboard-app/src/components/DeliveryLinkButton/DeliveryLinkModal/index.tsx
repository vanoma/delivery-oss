import React, { useState } from 'react';
import {
    Typography,
    Box,
    FormHelperText,
    Card,
    Modal,
    Backdrop,
    Fade,
    styled,
    TextField,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import {
    localizePhoneNumber,
    prefixNumberWithCountryCode,
} from '@vanoma/helpers';
import { useNavigate } from 'react-router-dom';
import { Contact, PackageSize } from '@vanoma/types';
import NewContactForm from '../../NewContactForm';
import BillingStatusContainer from '../../BillingStatusContainer';
import WhoIsPaying from './WhoIsPaying';
import {
    selectAgent,
    selectCustomerId,
} from '../../../redux/slices/authenticationSlice';
import { useCreateDeliveryLinkMutation } from '../../../api';
import { DELIVERIES, DELIVERIES_TAB } from '../../../routeNames';

const PopUp = styled(Card)(({ theme }) => ({
    position: 'absolute' as const,
    top: '50%',
    left: '50%',
    transform: 'translate(-50%, -50%)',
    width: theme.spacing(50),
    padding: theme.spacing(3),
    outline: 0,
    borderRadius: theme.spacing(2),
    [theme.breakpoints.down('sm')]: {
        width: theme.spacing(40),
    },
}));

const DeliveryLinkModal: React.FC<{
    toContact?: Contact | null;
    pickUpStart?: string | null;
    openLink: boolean;
    handleLinkClose: () => void;
    fromContactId: string;
    fromAddressId: string;
    resetDelivery?: () => void;
}> = ({
    toContact,
    pickUpStart,
    openLink,
    handleLinkClose,
    fromContactId,
    fromAddressId,
    resetDelivery,
}) => {
    const { t } = useTranslation();
    const [loading, setLoading] = useState(0);

    const customerId = useSelector(selectCustomerId);
    const agent = useSelector(selectAgent);

    const [createDeliveryLink, { error }] = useCreateDeliveryLinkMutation();
    const navigate = useNavigate();

    const generateDeliveryLink = (
        isCustomerPaying: boolean,
        fromNote: string,
        size: PackageSize,
        phoneNumber?: string
    ): void => {
        setLoading(isCustomerPaying ? 1 : 2);
        createDeliveryLink({
            customerId: customerId!,
            isCustomerPaying,
            packages: [
                {
                    size,
                    pickUpStart: pickUpStart ?? null,
                    fromContact: { contactId: fromContactId },
                    fromAddress: { addressId: fromAddressId },
                    toContact: toContact
                        ? { contactId: toContact.contactId }
                        : { phoneNumberOne: phoneNumber! },
                    fromNote,
                },
            ],
            agentId: agent!.agentId,
        })
            .unwrap()
            .then(() => {
                if (resetDelivery) {
                    resetDelivery();
                }
                navigate(`${DELIVERIES}/${DELIVERIES_TAB.REQUEST}`);
            })
            .finally(() => setLoading(0));
    };

    return (
        <Modal
            open={openLink}
            onClose={handleLinkClose}
            closeAfterTransition
            BackdropComponent={Backdrop}
            BackdropProps={{
                timeout: 500,
                sx: { backgroundColor: '#000000cc' },
            }}
            sx={{
                overflow: 'scroll',
            }}
        >
            <Fade in={openLink}>
                <PopUp sx={{ mt: 8 }}>
                    <BillingStatusContainer isModal>
                        <Box sx={{ pb: 1 }}>
                            {toContact === undefined ? (
                                <>
                                    <Typography variant="h5" align="center">
                                        {t(
                                            'customers.linkGeneratorModal.getDeliveryLink'
                                        )}
                                    </Typography>
                                    <NewContactForm
                                        initialValues={{
                                            name: '',
                                            phoneNumber: '',
                                            isCustomerPaying: true,
                                        }}
                                        onSubmit={() => {}}
                                    >
                                        {({ values, errors }) =>
                                            values.phoneNumber &&
                                            !errors.phoneNumber && (
                                                <WhoIsPaying
                                                    loading={loading}
                                                    generateDeliveryLink={
                                                        generateDeliveryLink
                                                    }
                                                    phoneNumber={prefixNumberWithCountryCode(
                                                        values.phoneNumber
                                                    )}
                                                />
                                            )
                                        }
                                    </NewContactForm>
                                </>
                            ) : (
                                <>
                                    {toContact && (
                                        <>
                                            <Typography
                                                variant="h5"
                                                align="center"
                                            >
                                                {t(
                                                    'customers.linkGeneratorModal.getDeliveryLink'
                                                )}
                                            </Typography>
                                            <TextField
                                                label={t(
                                                    'customers.linkGeneratorModal.customerPhoneNumber'
                                                )}
                                                value={localizePhoneNumber(
                                                    toContact.phoneNumberOne
                                                )}
                                                size="small"
                                                fullWidth
                                                disabled
                                                sx={{ my: 2 }}
                                            />
                                        </>
                                    )}
                                    <WhoIsPaying
                                        loading={loading}
                                        generateDeliveryLink={
                                            generateDeliveryLink
                                        }
                                    />
                                </>
                            )}
                        </Box>
                        {error && (
                            <FormHelperText sx={{ mt: 2 }} error>
                                {error}
                            </FormHelperText>
                        )}
                    </BillingStatusContainer>
                </PopUp>
            </Fade>
        </Modal>
    );
};

export default DeliveryLinkModal;
