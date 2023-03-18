/* eslint-disable no-nested-ternary */
import React, { useState } from 'react';
import { Box, ButtonBase, Collapse, Typography } from '@mui/material';
import { alpha } from '@mui/system';
import {
    PackagePickupIcon,
    PackageDeliveredIcon,
    LocationButton,
} from '@vanoma/ui-components';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import {
    formatAddressForPublicView,
    formatContactForPrivateView,
} from '@vanoma/helpers';
import { Address, CurrentStop, CurrentTask, TaskType } from '@vanoma/types';
import markerIcon from '../../../../../../../public/assets/driver-marker.png';

const getContact = (task: CurrentTask): string =>
    formatContactForPrivateView(
        task.type === TaskType.PICK_UP
            ? task.package.fromContact
            : task.package.toContact
    );

const getAddressLine = (task: CurrentTask): string =>
    formatAddressForPublicView(
        task.type === TaskType.PICK_UP
            ? task.package.fromAddress
            : task.package.toAddress
    );

const getNote = (task: CurrentTask): string | null =>
    task.type === TaskType.PICK_UP
        ? task.package.fromNote
        : task.package.toNote;

const getAddress = (task: CurrentTask): Address =>
    task.type === TaskType.PICK_UP
        ? task.package.fromAddress
        : task.package.toAddress;

const StopView: React.FC<{
    stop: CurrentStop;
    stopIndex: number;
    current: boolean;
}> = ({ stop, stopIndex, current }) => {
    const [expand, setExpand] = useState(current);

    return (
        <Box
            sx={(theme) => ({
                border: `1px solid ${theme.palette.primary.light}`,
                borderBottomLeftRadius: expand ? 16 : 0,
                borderBottomRightRadius: expand ? 16 : 0,
                mb: 2,
            })}
        >
            <Box
                display="flex"
                alignItems="center"
                justifyContent="space-between"
                sx={(theme) => ({
                    background:
                        stop.completedAt !== null
                            ? alpha(theme.palette.success.main, 0.2)
                            : current
                            ? alpha(theme.palette.primary.light, 0.1)
                            : alpha(theme.palette.grey[900], 0.2),
                    p: 1,
                })}
                component={ButtonBase}
                width="100%"
                onClick={() => setExpand(!expand)}
            >
                <Box display="flex" justifyContent="center" alignItems="center">
                    <Box
                        display="flex"
                        justifyContent="center"
                        alignItems="center"
                        height={26}
                        width={26}
                        sx={(theme) => ({
                            background: 'white',
                            border: `1px solid ${theme.palette.primary.light}`,
                        })}
                        borderRadius={0.5}
                        mr={1}
                    >
                        <Typography variant="h6" color="primary">
                            {stopIndex + 1}
                        </Typography>
                    </Box>
                    <Typography
                        variant="h6"
                        color={current ? 'primary' : 'text.primary'}
                    >
                        {getAddressLine(stop.currentTasks[0])}
                    </Typography>
                </Box>
                {stop.completedAt !== null && (
                    <CheckCircleIcon color="primary" />
                )}
            </Box>
            <Collapse in={expand}>
                <Box p={2}>
                    <Box
                        display="flex"
                        alignItems="center"
                        justifyContent="space-between"
                        mb={2}
                    >
                        <Typography variant="h6">Tasks</Typography>
                        <LocationButton
                            location={{
                                lat: getAddress(stop.currentTasks[0]).latitude,
                                lng: getAddress(stop.currentTasks[0]).longitude,
                            }}
                            googleMapsApiKey={process.env.GOOGLE_API_KEY!}
                            icon={markerIcon}
                        />
                    </Box>
                    {stop.currentTasks.map((task, index) => (
                        <Box
                            sx={(theme) => ({
                                border: `1px solid ${theme.palette.primary.light}`,
                                borderRadius: 0.5,
                                p: 2,
                                mt: index !== 0 ? 2 : 0,
                            })}
                        >
                            <Box
                                display="flex"
                                alignItems="center"
                                justifyContent="space-between"
                            >
                                <Box display="flex" alignItems="center" mb={1}>
                                    {task.type === TaskType.PICK_UP ? (
                                        <PackagePickupIcon color="primary" />
                                    ) : (
                                        <PackageDeliveredIcon color="primary" />
                                    )}
                                    <Typography color="primary" ml={1}>
                                        {task.type === TaskType.PICK_UP
                                            ? 'Pick-up'
                                            : 'Drop-off'}
                                    </Typography>
                                </Box>
                                {task.completedAt !== null && (
                                    <CheckCircleIcon color="primary" />
                                )}
                            </Box>

                            <Typography variant="body1">
                                {getContact(task)}
                            </Typography>
                            <Typography color="text.secondary">
                                {getAddressLine(task)}
                            </Typography>
                            <Typography mt={2}>{getNote(task)}</Typography>
                        </Box>
                    ))}
                </Box>
            </Collapse>
        </Box>
    );
};

export default StopView;
