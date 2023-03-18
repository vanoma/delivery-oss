import React, { ReactElement } from 'react';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import { formatDistanceToNow } from 'date-fns';
import {
    Theme,
    ListItemButton,
    ListItemText,
    Typography,
    Box,
    useMediaQuery,
} from '@mui/material';
import { HashLink } from 'react-router-hash-link';
import {
    inclementClickCount,
    readNotification,
} from '../../redux/slices/notificationsSlice';
import { DELIVERIES, DELIVERIES_TAB } from '../../routeNames';
import { EventNotification } from '../../types';
import { useTypedDispatch } from '../../redux/typedHooks';

const scrollWithOffset = (el: HTMLElement, isSmall: boolean): void => {
    const yCoordinate = el.getBoundingClientRect().top + window.pageYOffset;
    const yOffset = isSmall ? -120 : -144;
    window.scrollTo({ top: yCoordinate + yOffset, behavior: 'smooth' });
};

const NotificationItem = ({
    notification,
    handleClose,
}: {
    notification: EventNotification;
    handleClose: () => void;
}): ReactElement => {
    const isSmall = useMediaQuery((theme: Theme) =>
        theme.breakpoints.down('lg')
    );
    const dispatch = useTypedDispatch();

    const handleNotificationClick = (): void => {
        handleClose();
        if (!notification.data.isRead) {
            dispatch(readNotification(notification.data.packageId));
        } else {
            dispatch(inclementClickCount(notification.data.packageId));
        }
    };

    return (
        <ListItemButton
            to={`${DELIVERIES}/${DELIVERIES_TAB.REQUEST.toLowerCase()}#${
                notification.data.packageId
            }`}
            disableGutters
            component={HashLink}
            smooth
            scroll={(el: HTMLElement) => scrollWithOffset(el, isSmall)}
            sx={{
                py: 1.5,
                px: 2.5,
                mt: '1px',
                ...(!notification.data.isRead && {
                    bgcolor: 'action.selected',
                }),
            }}
            onClick={handleNotificationClick}
        >
            <ListItemText
                primary={
                    <Typography
                        variant="subtitle2"
                        sx={{
                            color: notification.data.isRead
                                ? 'text.secondary'
                                : undefined,
                        }}
                    >
                        {notification.content}
                    </Typography>
                }
                secondary={
                    <Typography
                        variant="caption"
                        sx={{
                            mt: 0.5,
                            display: 'flex',
                            alignItems: 'center',
                            color: 'text.disabled',
                        }}
                    >
                        <Box
                            component={AccessTimeIcon}
                            sx={{ mr: 0.5, width: 16, height: 16 }}
                        />
                        {formatDistanceToNow(
                            new Date(notification.data.createdAt)
                        )}
                    </Typography>
                }
            />
        </ListItemButton>
    );
};

export default NotificationItem;
