import React, { ReactElement, ReactNode, useRef, useState } from 'react';
import NotificationsNoneIcon from '@mui/icons-material/NotificationsNone';
import DoneAllIcon from '@mui/icons-material/DoneAll';
import {
    Box,
    List,
    Badge,
    Tooltip,
    Divider,
    IconButton,
    Typography,
    ListSubheader,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import NotificationItem from './NotificationItem';
import {
    markAllNotificationsAsRead,
    selectReadNotifications,
    selectUnreadNotifications,
} from '../../redux/slices/notificationsSlice';
import { useTypedDispatch } from '../../helpers/reduxToolkit';
import MenuPopover from '../../components/MenuPopover';
import Scrollbar from '../../components/Scrollbar';

const NotificationsContainer = ({
    children,
    isNew = false,
}: {
    children: ReactNode;
    isNew?: boolean;
}): ReactElement => {
    const { t } = useTranslation();

    return (
        <List
            disablePadding
            subheader={
                <ListSubheader
                    disableSticky
                    sx={{
                        py: 1,
                        px: 2.5,
                        typography: 'overline',
                    }}
                >
                    {isNew
                        ? t('dashboard.navbar.new')
                        : t('dashboard.navbar.viewed')}
                </ListSubheader>
            }
        >
            {children}
        </List>
    );
};

const NotificationsPopover = (): ReactElement => {
    const anchorRef = useRef(null);
    const dispatch = useTypedDispatch();
    const { t } = useTranslation();
    const [open, setOpen] = useState(false);
    const readNotifications = useSelector(selectReadNotifications);
    const unreadNotifications = useSelector(selectUnreadNotifications);

    const totalUnRead = unreadNotifications.length;
    const noNotifications = totalUnRead === 0 && readNotifications.length === 0;

    const handleOpen = (): void => {
        setOpen(true);
    };

    const handleClose = (): void => {
        setOpen(false);
    };

    return (
        <>
            <IconButton
                ref={anchorRef}
                onClick={handleOpen}
                sx={{
                    padding: 0,
                    width: 44,
                    height: 44,
                    '&:hover': (theme) => ({
                        '& > span > svg': {
                            color: theme.palette.common.white,
                        },
                    }),
                }}
            >
                <Badge badgeContent={totalUnRead} color="error">
                    <NotificationsNoneIcon />
                </Badge>
            </IconButton>
            <MenuPopover
                open={open}
                onClose={handleClose}
                anchorRef={anchorRef}
                sx={{ width: 360 }}
            >
                <Box
                    sx={{
                        display: 'flex',
                        alignItems: 'center',
                        py: 2,
                        px: 2.5,
                    }}
                >
                    <Box sx={{ flexGrow: 1 }}>
                        <Typography variant="subtitle1">
                            {t('dashboard.navbar.notifications')}
                        </Typography>
                        {!noNotifications && (
                            <Typography
                                variant="body2"
                                sx={{ color: 'text.secondary' }}
                            >
                                {t('dashboard.navbar.unreadNotifications', {
                                    totalUnRead,
                                })}
                            </Typography>
                        )}
                    </Box>

                    {totalUnRead > 0 && (
                        <Tooltip title={t('dashboard.navbar.markAllAsRead')!}>
                            <IconButton
                                color="primary"
                                onClick={() =>
                                    dispatch(markAllNotificationsAsRead())
                                }
                            >
                                <DoneAllIcon />
                            </IconButton>
                        </Tooltip>
                    )}
                </Box>
                <Divider />
                <Scrollbar
                    sx={{ height: noNotifications ? {} : { xs: 340, sm: 540 } }}
                >
                    {noNotifications ? (
                        <Typography align="center" py={8}>
                            {t('dashboard.navbar.thereIsNoNotifications')}
                        </Typography>
                    ) : (
                        <>
                            {totalUnRead !== 0 && (
                                <NotificationsContainer isNew>
                                    {unreadNotifications.map((notification) => (
                                        <NotificationItem
                                            key={notification.packageEventId}
                                            notification={notification}
                                            handleClose={handleClose}
                                        />
                                    ))}
                                </NotificationsContainer>
                            )}
                            {readNotifications.length !== 0 && (
                                <NotificationsContainer>
                                    {readNotifications.map((notification) => (
                                        <NotificationItem
                                            key={notification.packageEventId}
                                            notification={notification}
                                            handleClose={handleClose}
                                        />
                                    ))}
                                </NotificationsContainer>
                            )}
                        </>
                    )}
                </Scrollbar>
            </MenuPopover>
        </>
    );
};

export default NotificationsPopover;
