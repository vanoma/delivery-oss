import React, { ReactElement } from 'react';
import { Typography, Stack, Tooltip } from '@mui/material';
import { sentenceCase } from 'sentence-case';
import moment from 'moment';
import { sortEvents } from '@vanoma/helpers';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import { useSelector } from 'react-redux';
import StopCircleIcon from '@mui/icons-material/StopCircle';
import {
    AssignmentStatus,
    PackageEvent,
    PackageEventName,
} from '@vanoma/types';
import Label from '../Label';
import { Delivery, StatusColor } from '../../types';
import { selectCurrentTab } from '../../redux/slices/deliveriesSlice';
import { DELIVERIES_TAB } from '../../routeNames';
import {
    PACKAGE_STATUS_COLOR,
    PAYMENT_STATUS_COLOR,
} from '../../helpers/constants';
import { useGetDriverQuery } from '../../api';

interface Props {
    delivery: Delivery;
}

const isOverdue = (delivery: Delivery): boolean =>
    moment(delivery.package.pickUpStart).isBefore(moment.now());

const scheduleStatusColor = (delivery: Delivery): StatusColor =>
    isOverdue(delivery) ? 'error' : 'info';

const driverArrivedPickUp = (events: PackageEvent[]): boolean =>
    events.some(
        (event) => event.eventName === PackageEventName.DRIVER_ARRIVED_PICK_UP
    );

const getTimeInText = (absoluteDifference: number): string => {
    if (absoluteDifference < 60) {
        return `${Math.round(absoluteDifference)} minutes`;
    }

    if (absoluteDifference === 60) {
        return 'an hour';
    }

    const hours = Math.trunc(absoluteDifference / 60);
    const minutes = absoluteDifference - hours * 60;

    if (minutes === 0) {
        return `${hours} hours$`;
    }

    return `${hours} hour${hours > 1 ? 's' : ''} and ${minutes} minutes`;
};

const displayRemainingTime = (utcTime: string): string => {
    const now = moment();
    const time = moment(utcTime);

    const difference = Math.trunc(time.diff(now) / (1000 * 60));
    const isLate = difference < 0;
    const timeInText = getTimeInText(Math.abs(difference));

    if (isLate) {
        return `${timeInText} ago`;
    }

    return `In ${timeInText}`;
};

const hasExceededMinutes = (event: PackageEvent, minutes: number): boolean =>
    moment.utc().diff(moment(event.createdAt)) > minutes * 60 * 1000;

const isTakingTooLongOnStop = (events: PackageEvent[]): boolean => {
    const sortedEvents = sortEvents(events);
    const lastEvent = sortedEvents[events.length - 1];

    if (
        lastEvent &&
        lastEvent.eventName.includes('ARRIVED') &&
        hasExceededMinutes(lastEvent, 5)
    )
        return true;

    return false;
};

const DeliveryStatus: React.FC<Props> = ({ delivery }): ReactElement => {
    const currentTab = useSelector(selectCurrentTab);

    const { paymentStatus, status, events } = delivery.package;

    const { data } = useGetDriverQuery(delivery.package.driverId!, {
        skip:
            delivery.package.assignmentId !== null ||
            delivery.package.driverId === null,
    });

    return (
        <>
            <Stack
                gap={{ xs: 1, sm: 2 }}
                direction="row"
                sx={{ alignItems: 'center', flexWrap: 'wrap', pb: 1.25 }}
            >
                <Typography variant="subtitle2">Status</Typography>
                <Label color={PAYMENT_STATUS_COLOR[paymentStatus]}>
                    {sentenceCase(paymentStatus)}
                </Label>
                <Label color={PACKAGE_STATUS_COLOR[status]}>
                    {sentenceCase(status)}
                </Label>
                {!delivery.package.isAssignable && (
                    <Tooltip title="Can no longer be assigned automatically">
                        <StopCircleIcon />
                    </Tooltip>
                )}
                {(currentTab === DELIVERIES_TAB.ACTIVE ||
                    currentTab === DELIVERIES_TAB.REQUEST) &&
                    delivery.package.pickUpStart &&
                    !driverArrivedPickUp(events) && (
                        <Label color={scheduleStatusColor(delivery)}>
                            {displayRemainingTime(
                                delivery.package.pickUpStart!
                            )}
                        </Label>
                    )}
                {delivery.assignment !== null && (
                    <Label
                        color={
                            delivery.assignment.status ===
                            AssignmentStatus.PENDING
                                ? 'warning'
                                : 'success'
                        }
                    >
                        <span
                            style={{ fontWeight: 'bold', fontSize: 14 }}
                        >{`${delivery.assignment.driver.firstName} ${delivery.assignment.driver.lastName}`}</span>
                    </Label>
                )}
                {data && (
                    <Label color="success">
                        <span
                            style={{ fontWeight: 'bold', fontSize: 14 }}
                        >{`${data.firstName} ${data.lastName}`}</span>
                    </Label>
                )}
                {delivery.package.isExpress && (
                    <Label color="info">
                        <span style={{ fontWeight: 'bold', fontSize: 14 }}>
                            Express
                        </span>
                    </Label>
                )}
                {isTakingTooLongOnStop(events) && (
                    <Tooltip title="Exceeded 5 min at stop.">
                        <WarningAmberIcon color="error" />
                    </Tooltip>
                )}
            </Stack>
        </>
    );
};

export default DeliveryStatus;
