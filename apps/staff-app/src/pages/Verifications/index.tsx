import React, { ReactElement } from 'react';
import {
    Container,
    ListItem,
    ListItemText,
    Typography,
    Skeleton,
} from '@mui/material';
import { CustomSnackBar } from '@vanoma/ui-components';
import Verification from './VerificationItem';
import { useGetOtpListQuery } from '../../api';

const Verifications = (): ReactElement => {
    const { data, error, isFetching } = useGetOtpListQuery(
        { sort: 'createdAt,desc' },
        {
            refetchOnMountOrArgChange: true,
        }
    );

    return (
        <Container>
            <Typography sx={{ mt: 1.5, mb: 3 }} variant="h4">
                Verifications
            </Typography>
            <ListItem selected sx={{ mb: 2 }}>
                <ListItemText primary="Phone number" />
                <ListItemText primary="Verification code" />
                <ListItemText primary="Sent At" sx={{ textAlign: 'right' }} />
            </ListItem>
            {isFetching &&
                [...new Array(10)].map((value) => (
                    <Skeleton
                        key={value}
                        variant="rectangular"
                        animation="wave"
                        height={48}
                        sx={{ borderRadius: 0.5, mb: 2 }}
                    />
                ))}
            {!isFetching &&
                data &&
                data.count > 0 &&
                data.results.map((otp) => (
                    <Verification key={otp.otpId} otp={otp} />
                ))}
            {!isFetching && data && data.count === 0 && (
                <Typography>
                    There are no verifications at the moment.
                </Typography>
            )}
            <CustomSnackBar message={error as string} severity="error" />
        </Container>
    );
};

export default Verifications;
