import { Card, ListItem, ListItemText } from '@mui/material';
import moment from 'moment';
import React, { ReactElement } from 'react';
import { localizePhoneNumber } from '@vanoma/helpers';
import { OTP } from '@vanoma/types';

const VerificationView = ({ otp }: { otp: OTP }): ReactElement => {
    return (
        <Card sx={{ mb: 2 }}>
            <ListItem>
                <ListItemText primary={localizePhoneNumber(otp.phoneNumber)} />
                <ListItemText primary={otp.otpCode} />
                <ListItemText
                    primary={`${moment(otp.createdAt).format('h:mm A')} `}
                    sx={{ textAlign: 'right' }}
                />
            </ListItem>
        </Card>
    );
};

export default VerificationView;
