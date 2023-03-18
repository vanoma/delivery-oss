import React, { ReactElement } from 'react';
import { styled } from '@mui/material/styles';
import { Link as RouterLink } from 'react-router-dom';
import {
    Box,
    Card,
    Container,
    Divider,
    Stack,
    Tooltip,
    Typography,
} from '@mui/material';
import VerifiedIcon from '@mui/icons-material/Verified';
import { useSelector } from 'react-redux';
import CustomAvatar from '../CustomAvatar';
import LogoColor from '../../../components/LogoColor';
import { selectUser } from '../../../redux/slices/authenticationSlice';

const AccountStyle = styled(Card)(({ theme }) => ({
    display: 'flex',
    alignItems: 'center',
    padding: theme.spacing(2, 2.5),
}));

export default function Header(): ReactElement {
    const user = useSelector(selectUser);

    return (
        <>
            <Container
                sx={{ pt: { xs: 1.4, lg: 2 }, pb: { xs: 0.7, lg: 3.6 } }}
            >
                <Box
                    component={RouterLink}
                    to="/"
                    sx={{ display: 'inline-flex' }}
                >
                    <LogoColor />
                </Box>
            </Container>
            <Divider />
            <Box sx={{ mb: 5, mx: 2.5, mt: 3 }}>
                <AccountStyle>
                    <CustomAvatar />
                    <Box sx={{ ml: 2, flexGrow: 1 }}>
                        <Stack
                            spacing={2}
                            direction="row"
                            sx={{
                                display: 'flex',
                                justifyContent: 'space-between',
                            }}
                        >
                            <Typography
                                variant="subtitle2"
                                sx={{ color: 'text.primary' }}
                            >
                                {`${user?.firstName} ${user?.lastName}`}
                            </Typography>
                            <Tooltip title="Verified account">
                                <VerifiedIcon
                                    color="primary"
                                    fontSize="small"
                                />
                            </Tooltip>
                        </Stack>
                        <Typography
                            variant="body2"
                            sx={{ color: 'text.secondary' }}
                        >
                            Staff
                        </Typography>
                    </Box>
                </AccountStyle>
            </Box>
        </>
    );
}
