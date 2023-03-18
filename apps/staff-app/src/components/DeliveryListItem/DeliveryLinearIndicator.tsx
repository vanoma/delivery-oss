/* eslint-disable no-plusplus */
import {
    LinearProgress,
    linearProgressClasses,
    LinearProgressProps,
    styled,
} from '@mui/material';
import { createEventsMap } from '@vanoma/helpers';
import { PackageEventName, Stop } from '@vanoma/types';
import moment, { Moment } from 'moment';
import React from 'react';
import { Delivery } from '../../types';

interface TimeComplex {
    waitingTime: number;
    lateOnlyBy: number;
}

const getGoingToStopWaitingTime = (stop: Stop): TimeComplex => {
    const { departBy, departedAt, arriveBy } = stop;
    const now = moment();
    const arriveByM = moment(arriveBy);
    const departByM = moment(departBy);

    return {
        waitingTime: arriveByM.diff(departByM) - now.diff(moment(departedAt)),
        lateOnlyBy: Math.round(arriveByM.diff(departByM) / 4 / 1000 / 60),
    };
};

const getArrivedAtStopWaitingTime = (arrivedAt: Moment): number => {
    const now = moment();

    return arrivedAt.add(5, 'minute').diff(now);
};
const getPackagePickedUpWaitingTime = (pickedUpAt: Moment): number => {
    const now = moment();

    return pickedUpAt.add(5, 'minute').diff(now);
};

const getProgressColor = ({
    waitingTime,
    lateOnlyBy,
}: {
    waitingTime: number;
    lateOnlyBy: number;
}): LinearProgressProps['color'] => {
    const waitingTimeInMinutes = waitingTime / 1000 / 60;
    if (waitingTimeInMinutes >= 0) {
        return 'success';
    }
    if (Math.abs(waitingTimeInMinutes) < lateOnlyBy) {
        return 'warning';
    }
    return 'error';
};

const getProgressAndColor = (
    delivery: Delivery
): { progress: number; color: LinearProgressProps['color'] } => {
    const eventsMap = createEventsMap(delivery.package.events);
    const falsyReturn = { progress: 0, color: undefined };

    const arrivedDropOff = eventsMap[PackageEventName.DRIVER_ARRIVED_DROP_OFF];
    if (arrivedDropOff) {
        const waitingTime = getArrivedAtStopWaitingTime(
            moment(arrivedDropOff.createdAt)
        );
        return {
            progress: 100,
            color: getProgressColor({ waitingTime, lateOnlyBy: 5 }),
        };
    }

    if (PackageEventName.DRIVER_DEPARTING_DROP_OFF in eventsMap) {
        if (!delivery.assignment) return falsyReturn;
        return {
            progress: 80,
            color: getProgressColor(
                getGoingToStopWaitingTime(delivery.assignment.tasks[1].stop)
            ),
        };
    }

    const packagePickedUp = eventsMap[PackageEventName.PACKAGE_PICKED_UP];
    if (packagePickedUp) {
        const waitingTime = getPackagePickedUpWaitingTime(
            moment(packagePickedUp.createdAt)
        );
        return {
            progress: 60,
            color: getProgressColor({ waitingTime, lateOnlyBy: 5 }),
        };
    }

    const arrivedPickUp = eventsMap[PackageEventName.DRIVER_ARRIVED_PICK_UP];
    if (arrivedPickUp) {
        const waitingTime = getArrivedAtStopWaitingTime(
            moment(arrivedPickUp.createdAt)
        );
        return {
            progress: 40,
            color: getProgressColor({ waitingTime, lateOnlyBy: 5 }),
        };
    }

    if (PackageEventName.DRIVER_DEPARTING_PICK_UP in eventsMap) {
        if (!delivery.assignment) return falsyReturn;
        return {
            progress: 20,
            color: getProgressColor(
                getGoingToStopWaitingTime(delivery.assignment.tasks[0].stop)
            ),
        };
    }

    return falsyReturn;
};

const CustomLinearProgress = styled(LinearProgress)(({ theme }) => ({
    height: 2,
    [`&.${linearProgressClasses.colorPrimary}`]: {
        backgroundColor:
            theme.palette.grey[theme.palette.mode === 'light' ? 300 : 800],
    },
}));

const DeliveryLinearIndicator: React.FC<{ delivery: Delivery }> = ({
    delivery,
}) => {
    const { progress, color } = getProgressAndColor(delivery);

    return (
        <>
            {progress > 0 ? (
                <CustomLinearProgress
                    variant="determinate"
                    value={progress}
                    color={color}
                />
            ) : (
                <div style={{ height: 2 }} />
            )}
        </>
    );
};

export default DeliveryLinearIndicator;
