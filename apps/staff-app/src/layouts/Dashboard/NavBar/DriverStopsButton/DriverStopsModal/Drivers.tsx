import { TimelineDot } from '@mui/lab';
import {
    Box,
    FormHelperText,
    ListItem,
    ListItemIcon,
    ListItemText,
    Skeleton,
} from '@mui/material';
import { DriverStatus } from '@vanoma/types';
import { CustomList } from '@vanoma/ui-components';
import React from 'react';
import { useGetDriversQuery } from '../../../../../api';
import { getDriverStatusColor } from '../../../../../helpers/driver';

const Drivers: React.FC<{
    selectedDriverId: string | null;
    setSelectedDriverId: React.Dispatch<React.SetStateAction<string | null>>;
}> = ({ selectedDriverId, setSelectedDriverId }) => {
    const { data, error, isFetching } = useGetDriversQuery(
        {
            status: [DriverStatus.ACTIVE, DriverStatus.PENDING],
            sort: 'first_name',
        },
        { pollingInterval: 30000, refetchOnMountOrArgChange: true }
    );

    return (
        <Box p={2}>
            {isFetching && !data && (
                <Skeleton
                    variant="rectangular"
                    animation="wave"
                    height={200}
                    sx={{ borderRadius: 0.5 }}
                />
            )}
            {data && !isFetching && (
                <CustomList sx={{ m: 0, mt: 2 }}>
                    {data.results.map((driver) => (
                        <ListItem
                            button
                            divider
                            key={driver.driverId}
                            selected={driver.driverId === selectedDriverId}
                            onClick={() => setSelectedDriverId(driver.driverId)}
                            disabled={driver.assignmentCount === 0}
                        >
                            <ListItemIcon sx={{ minWidth: 24 }}>
                                <TimelineDot
                                    color={getDriverStatusColor(driver)}
                                />
                            </ListItemIcon>
                            <ListItemText
                                primary={`${driver.firstName} ${driver.lastName}`}
                            />
                        </ListItem>
                    ))}
                </CustomList>
            )}
            {error && (
                <FormHelperText sx={{ ml: 2 }} error>
                    {error}
                </FormHelperText>
            )}
        </Box>
    );
};

export default Drivers;
