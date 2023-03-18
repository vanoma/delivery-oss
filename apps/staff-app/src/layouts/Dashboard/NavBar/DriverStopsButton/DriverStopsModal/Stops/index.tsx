/* eslint-disable no-nested-ternary */
import React from 'react';
import { Box, FormHelperText, LinearProgress, Typography } from '@mui/material';
import Stop from './Stop';
import { useGetStopsQuery } from '../../../../../../api';

const Stops: React.FC<{ selectedDriverId: string | null }> = ({
    selectedDriverId,
}) => {
    const { data, error, isFetching } = useGetStopsQuery(selectedDriverId!, {
        skip: selectedDriverId === null,
        refetchOnMountOrArgChange: true,
    });

    return (
        <Box height="calc(100% - 240px)" overflow="scroll">
            {isFetching ? <LinearProgress /> : <div style={{ height: 4 }} />}
            <Box px={2} pb={2}>
                <Typography variant="h5" my={2}>
                    Stops
                </Typography>
                {data &&
                    data.results.map((stop, index) => (
                        <Stop
                            stop={stop}
                            stopIndex={index}
                            current={
                                (index === 0 && stop.completedAt === null) ||
                                (index > 0 &&
                                    data.results[index - 1].completedAt !==
                                        null &&
                                    stop.completedAt === null)
                            }
                        />
                    ))}
                {!selectedDriverId && (
                    <Typography my={10} align="center">
                        Select a driver
                    </Typography>
                )}
            </Box>
            {error && (
                <FormHelperText sx={{ ml: 2 }} error>
                    {error}
                </FormHelperText>
            )}
        </Box>
    );
};

export default Stops;
