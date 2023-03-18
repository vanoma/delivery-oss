import React from 'react';
import { Box, Card, Stack } from '@mui/material';
import { localizePhoneNumber } from '@vanoma/helpers';
import { Customer } from '@vanoma/types';
import moment from 'moment';
import EditButton from './EditButton';
import InfoPair from '../../components/InfoPair';

const CustomerView: React.FC<{ customer: Customer }> = ({ customer }) => {
    return (
        <Card sx={{ mb: 2, p: 2 }}>
            <Box
                display="flex"
                justifyContent="space-between"
                alignItems="start"
            >
                <Stack>
                    <InfoPair
                        property="Business name"
                        value={customer.businessName}
                    />
                    <InfoPair
                        property="Phone number"
                        value={localizePhoneNumber(customer.phoneNumber)}
                    />
                    <InfoPair
                        property="Created at"
                        value={moment(customer.createdAt).format(
                            'MMM Do YYYY, h:mm A'
                        )}
                    />
                    <InfoPair
                        property="Weighting factor"
                        value={customer.weightingFactor.toFixed(2).toString()}
                    />
                    <InfoPair
                        property="Prepaid"
                        value={`${customer.isPrepaid}`}
                    />
                    <InfoPair
                        property="Has fixed price"
                        value={`${customer.hasFixedPrice}`}
                    />
                    <InfoPair
                        property="Billing interval"
                        value={`${customer.billingInterval}`}
                    />
                    <InfoPair
                        property="Billing grace period"
                        value={`${customer.billingGracePeriod}`}
                    />
                </Stack>
                <EditButton customer={customer} />
            </Box>
        </Card>
    );
};

export default CustomerView;
