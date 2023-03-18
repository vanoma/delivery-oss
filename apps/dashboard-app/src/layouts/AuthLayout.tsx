import React, { FC } from 'react';
import { Outlet } from 'react-router-dom';
import { Container, Grid, Stack, Typography, Box, Card } from '@mui/material';
import { styled } from '@mui/material/styles';
import { alpha } from '@mui/system';
import { useTranslation } from 'react-i18next';
import { Hidden, CopyRight } from '@vanoma/ui-components';
import backgroundImage from '../../public/assets/backgroundImage.png';
import Logo from '../components/Logo';
import LanguagePopover from '../components/LanguagePopover';
import '../locales/i18n';

const Main = styled('div')(({ theme }) => ({
    [theme.breakpoints.down('lg')]: {
        background: `linear-gradient(-21deg, ${theme.palette.primary.main} -83%, ${theme.palette.common.black}) 90%`,
        minHeight: '100%',
    },
    [theme.breakpoints.up('lg')]: {
        background: `url(${backgroundImage}) no-repeat`,
        backgroundSize: 'cover',
        height: '100%',
    },
}));

const BlackBackgroundFilter = styled('div')(({ theme }) => ({
    [theme.breakpoints.up('lg')]: {
        height: '100%',
        width: '100%',
        background: alpha(theme.palette.common.black, 0.6),
    },
}));

const DescriptionSection = styled('div')(({ theme }) => ({
    [theme.breakpoints.up('lg')]: {
        background: `linear-gradient(-21deg, ${
            theme.palette.primary.main
        } -83%, ${alpha(theme.palette.common.black, 0.9)}) 90%`,
    },
    height: '100%',
}));

const SectionContainer = styled(Container)(({ theme }) => ({
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'center',
    paddingBottom: theme.spacing(2),
    [theme.breakpoints.up('lg')]: {
        height: 'calc(100% - 120px)',
    },
}));

const AuthLayout: FC = () => {
    const { t } = useTranslation();

    return (
        <Main>
            <BlackBackgroundFilter>
                <Container
                    sx={{
                        py: 2,
                        display: { xs: 'flex', lg: 'none' },
                        justifyContent: 'space-between',
                        alignItems: 'center',
                    }}
                >
                    <Logo />
                    <LanguagePopover />
                </Container>
                <Grid container sx={{ height: '100%' }}>
                    <Grid item xs={12} lg={5}>
                        <DescriptionSection>
                            <Container maxWidth="sm">
                                <Box py={2}>
                                    <Hidden width="lgDown">
                                        <Logo />
                                    </Hidden>
                                </Box>
                            </Container>
                            <SectionContainer
                                maxWidth="sm"
                                sx={{ justifyContent: 'space-between' }}
                            >
                                <Stack
                                    spacing={3}
                                    py={{ xs: 2 }}
                                    pt={{ lg: 25, xl: 40 }}
                                >
                                    <Typography
                                        variant="h2"
                                        textAlign={{
                                            xs: 'center',
                                            lg: 'center',
                                        }}
                                        sx={{ color: 'white' }}
                                    >
                                        {t('auth.welcomeMessage.title')}
                                    </Typography>
                                    <Typography
                                        textAlign={{
                                            xs: 'center',
                                            lg: 'center',
                                        }}
                                        sx={{
                                            color: '#919EAB',
                                            fontSize: 22,
                                            px: { md: 6 },
                                        }}
                                    >
                                        {t('auth.welcomeMessage.subtitle')}
                                    </Typography>
                                </Stack>
                                <Hidden width="lgDown">
                                    <CopyRight />
                                </Hidden>
                            </SectionContainer>
                        </DescriptionSection>
                    </Grid>
                    <Grid item xs={12} lg={7}>
                        <Box
                            sx={{
                                maxWidth: 840,
                                marginLeft: 'auto',
                                marginRight: 'auto',
                            }}
                        >
                            <Container
                                maxWidth="xl"
                                sx={{
                                    display: 'flex',
                                    justifyContent: 'right',
                                }}
                            >
                                <Box py={2}>
                                    <Hidden width="lgDown">
                                        <LanguagePopover />
                                    </Hidden>
                                </Box>
                            </Container>
                        </Box>
                        <SectionContainer maxWidth="sm">
                            <Card>
                                <Outlet />
                            </Card>
                        </SectionContainer>
                    </Grid>
                </Grid>
                <Hidden width="lgUp">
                    <Box mt={10} pb={3}>
                        <CopyRight />
                    </Box>
                </Hidden>
            </BlackBackgroundFilter>
        </Main>
    );
};

export default AuthLayout;
