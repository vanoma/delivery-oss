import React, { FC, useState } from 'react';
import {
    Button,
    Card,
    // CardHeader,
    Grid,
    Typography,
    // Box,
    Container,
    Box,
    // Stack,
} from '@mui/material';
import { styled } from '@mui/material/styles';
import { alpha } from '@mui/system';
// import { merge } from 'lodash';
// import ReactApexChart from 'react-apexcharts';
import { useNavigate } from 'react-router-dom';
// import { TimelineDot } from '@mui/lab';
// import BaseOptionChart from '../components/chart/BaseOptionChart';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import '../../locales/i18n';
import FormatListBulletedIcon from '@mui/icons-material/FormatListBulleted';
import SmsOutlinedIcon from '@mui/icons-material/SmsOutlined';
import { CustomModal } from '@vanoma/ui-components';
import { DELIVERIES } from '../../routeNames';
import {
    selectAddressId,
    selectAgent,
    selectBusinessName,
    selectContactId,
    selectCustomerId,
} from '../../redux/slices/authenticationSlice';
import DeliveryLinkModal from '../../components/DeliveryLinkButton/DeliveryLinkModal';

const NewDeliveryCard = styled(Card)(({ theme }) => ({
    background: `linear-gradient(270deg, ${alpha(
        theme.palette.primary.main,
        0.6
    )} 0%, ${alpha(theme.palette.primary.main, 0)} 100%)`,
    padding: theme.spacing(3),
}));

const Overview: FC = () => {
    const businessName = useSelector(selectBusinessName);
   

    const navigate = useNavigate();
    const { t } = useTranslation();
    const defaultContactId = useSelector(selectContactId);
    const defaultAddressId = useSelector(selectAddressId);
    const agent = useSelector(selectAgent);
    const customerId = useSelector(selectCustomerId);
    const [openConfirm, setOpenConfirm] = useState({
        open: false,
        buttonNumber: 0,
    });
    const [openLink, setOpenLinkGenerator] = useState(false);

    const handleConfirmOpen = (number: number): void => {
        setOpenConfirm({ open: true, buttonNumber: number });
    };

    const handleLinkOpen = (): void => {
        setOpenLinkGenerator(true);
    };
    const handleLinkClose = (): void => {
        setOpenLinkGenerator(false);
    };

    const handleConfirmClose = (number: number): void => {
        setOpenConfirm({ open: false, buttonNumber: number });
        if (number === 1) {
            navigate({
                pathname: `${DELIVERIES}/new`,
            });
        } else if (number === 2) {
            handleLinkOpen();
        }
    };

    const exclusives = [
        'ebfb2198f9f0479cba67c630705fdba6',
        'eec3f1a97676471dba98a119aaeae63d',
    ];

    const showConfirmation = customerId && !exclusives.includes(customerId);

    return (
        <Container>
            <Typography
                variant="h4"
                mt={{ xs: 0, lg: 1.5 }}
                mb={{ xs: 2, lg: 4 }}
            >
                {`${t(
                    'overview.welcomeMessage.welcomeBack'
                )}, ${businessName}!`}
            </Typography>
            <Grid
                container
                spacing={{ xs: 2, sm: 3 }}
                sx={{ mb: { xs: 2, sm: 3 } }}
            >
                <Grid item xs={12} md={6}>
                    <NewDeliveryCard>
                        <Grid container spacing={2}>
                            <Grid
                                item
                                xs={12}
                                //  md={8}
                            >
                                <Typography variant="h4" mb={2}>
                                    {t('overview.newDeliveryCard.title')}
                                </Typography>
                                <Typography variant="body1" maxWidth={400}>
                                    {t('overview.newDeliveryCard.subtitle')}
                                </Typography>
                                {/* <Typography
                                    variant="body1"
                                    maxWidth={400}
                                    mt={3}
                                /> */}
                                <Box
                                    mt={5}
                                    display="flex"
                                    gap={3}
                                    flexDirection={{ xs: 'column', sm: 'row' }}
                                >
                                    <Button
                                        variant="contained"
                                        size="medium"
                                        onClick={() => navigate({
                                                      pathname: `${DELIVERIES}/new`,
                                                  })
                                        }
                                        startIcon={<FormatListBulletedIcon />}
                                    >
                                        {t(
                                            'overview.newDeliveryCard.newDelivery'
                                        )}
                                    </Button>
                                    <Button
                                        variant="contained"
                                        size="medium"
                                        startIcon={<SmsOutlinedIcon />}
                                        onClick={() => handleLinkOpen()
                                        }
                                    >
                                        {t(
                                            'overview.newDeliveryCard.deliveryLink'
                                        )}
                                    </Button>
                                   
                                    <DeliveryLinkModal
                                        fromContactId={defaultContactId!}
                                        fromAddressId={
                                            agent!.branch?.address.addressId ??
                                            defaultAddressId!
                                        }
                                        openLink={openLink}
                                        handleLinkClose={handleLinkClose}
                                    />
                                </Box>
                            </Grid>
                        </Grid>
                    </NewDeliveryCard>
                </Grid>
                
            </Grid>
        </Container>
    );
};

export default Overview;
