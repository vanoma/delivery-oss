/* eslint-disable no-nested-ternary */
import React, { ReactElement } from 'react';
import {
    Box,
    Card,
    Divider,
    Link,
    Typography,
    Button,
    CircularProgress,
    LinearProgress,
} from '@mui/material';
import { localizePhoneNumber } from '@vanoma/helpers';
import { useTranslation } from 'react-i18next';
import {
    CopyToClipboard,
    DeliveryRequestProgress,
} from '@vanoma/ui-components';
import LocalPhoneIcon from '@mui/icons-material/LocalPhone';
import DoNotDisturbIcon from '@mui/icons-material/DoNotDisturb';
import { Address, Contact, Package } from '@vanoma/types';
import ActionMenu from '../../../components/DeliveryListItem/Actions/ActionMenu';
import { Actions } from '../../../components/DeliveryListItem';
import PaymentButton from './PaymentButton';

interface Props {
    progress: number;
    isFetching: boolean;
    pkg: Package<Contact, Address>;
    setCurrentPage: React.Dispatch<React.SetStateAction<number>>;
}

const DeliveryListItem = ({
    progress,
    isFetching,
    pkg,
    setCurrentPage,
}: Props): ReactElement => {
    const { t } = useTranslation();

    return (
        <Card sx={{ mb: 2, zIndex: 'auto' }}>
            <Box sx={{ p: 2 }}>
                <Box
                    display="flex"
                    justifyContent="space-between"
                    mb={2}
                    alignItems="start"
                >
                    <Typography variant="h6">
                        {t('deliveries.request.requestFor', {
                            customer: `${
                                pkg.toContact.name
                                    ? `${
                                          pkg.toContact.name
                                      } (${localizePhoneNumber(
                                          pkg.toContact.phoneNumberOne
                                      )})`
                                    : localizePhoneNumber(
                                          pkg.toContact.phoneNumberOne
                                      )
                            }`,
                        })}
                    </Typography>
                    <ActionMenu
                        actions={[
                            {
                                label: t('deliveries.order.cancel'),
                                icon: <DoNotDisturbIcon />,
                                render: (closeModal) => (
                                    <Actions.CancelDelivery
                                        delivery={pkg}
                                        setCurrentPage={setCurrentPage}
                                        closeActionModal={closeModal}
                                    />
                                ),
                            },
                        ]}
                    />
                </Box>
                <Box display="flex" alignItems="center" gap={1}>
                    <Link
                        href={pkg.deliveryOrder.deliveryLink}
                        target="_blank"
                        color="inherit"
                    >
                        {t('deliveries.order.deliveryLink')}
                    </Link>
                    <CopyToClipboard
                        value={pkg.deliveryOrder.deliveryLink}
                        message={t('deliveries.order.copiedToTheClipboard')}
                    />
                </Box>
                <Box display="flex" justifyContent="space-between" mt={2}>
                    <Button
                        size="small"
                        variant="outlined"
                        startIcon={<LocalPhoneIcon />}
                        onClick={() => {
                            window.location.href = `tel:${localizePhoneNumber(
                                pkg.toContact.phoneNumberOne
                            )}`;
                        }}
                    >
                        {localizePhoneNumber(pkg.toContact.phoneNumberOne)}
                    </Button>
                    {!isFetching && (
                        <CircularProgress
                            size={24}
                            variant="determinate"
                            value={progress}
                        />
                    )}
                </Box>
            </Box>
            <Divider />
            {isFetching && <LinearProgress />}
            <DeliveryRequestProgress
                pkg={pkg}
                renderPaymentContent={() => <PaymentButton delivery={pkg} />}
                sentText={t('deliveries.request.sent')}
                deliveryLinkSentText={t('deliveries.request.deliveryLinkSent')}
                openedText={t('deliveries.request.opened')}
                yetToBeOpenedText={t('deliveries.request.yetToBeOpened')}
                buyerOpenedLinkText={t('deliveries.request.customerOpenedLink')}
                addressText={t('deliveries.request.address')}
                yetToProvideAddressText={t(
                    'deliveries.request.yetToProvideAddress'
                )}
                buyerProvidedAddressText={t(
                    'deliveries.request.customerProvidedAddress'
                )}
                paymentText={t('deliveries.request.payment')}
                yetToBePaidText={t('deliveries.request.yetToBePaid')}
                dispatchedText={t('deliveries.request.dispatched')}
                yetToBeDispatchedText={t(
                    'deliveries.request.yetToBeDispatched'
                )}
                waitingForBuyerToPayText={t(
                    'deliveries.request.waitingForCustomerToPay',
                    { price: Math.ceil(pkg.totalAmount) }
                )}
                googleMapsApiKey={process.env.GOOGLE_API_KEY!}
                pickUpText={t('deliveries.request.pickUp')}
                dropOffText={t('deliveries.request.dropOff')}
            />
        </Card>
    );
};

export default DeliveryListItem;
