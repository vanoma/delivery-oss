import React, { ReactElement } from 'react';
import { Stack, Box, Skeleton, TextField } from '@mui/material';
import AdapterDateFns from '@mui/lab/AdapterDateFns';
import { LocalizationProvider, StaticDatePicker } from '@mui/lab';

const BillDatePicker = ({
    rangeDate,
    setRangeDate,
    isLoadingAndNoBill,
}: {
    rangeDate: Date | undefined;
    setRangeDate: React.Dispatch<React.SetStateAction<Date | undefined>>;
    isLoadingAndNoBill: boolean;
}): ReactElement => {
    return (
        <Box
            sx={{
                border: (theme) => `1px solid ${theme.palette.primary.light}`,
                borderRadius: 0.5,
                p: 0.5,
                width: { md: 330 },
            }}
        >
            {isLoadingAndNoBill ? (
                <Stack spacing={2} sx={{ mb: 10 }}>
                    <Skeleton />
                    <Skeleton animation="wave" />
                    <Skeleton />
                    <Skeleton animation="wave" />
                    <Skeleton width={220} />
                    <Skeleton animation="wave" />
                    <Skeleton width={220} />
                </Stack>
            ) : (
                <LocalizationProvider dateAdapter={AdapterDateFns}>
                    <StaticDatePicker
                        displayStaticWrapperAs="desktop"
                        value={rangeDate}
                        onChange={(value) => {
                            if (value) {
                                value.setHours(23);
                                value.setMinutes(59);
                                value.setSeconds(59);
                                value.setMilliseconds(0);
                                setRangeDate(value);
                            }
                        }}
                        renderInput={(params) => (
                            // eslint-disable-next-line react/jsx-props-no-spreading
                            <TextField {...params} />
                        )}
                        orientation="portrait"
                        // TODO: In UTC. We should use moment lib to get client local time
                        maxDate={new Date()}
                    />
                </LocalizationProvider>
            )}
        </Box>
    );
};

export default BillDatePicker;
