import { Box, FormHelperText, LinearProgress, Typography } from '@mui/material';
import { CustomModal } from '@vanoma/ui-components';
import moment from 'moment';
import React from 'react';
import { useGetAssignmentsQuery } from '../../../../api';
import { Delivery } from '../../../../types';

const AssignmentsModal: React.FC<{
    open: boolean;
    delivery: Delivery;
    handleClose: () => void;
}> = ({ open, delivery, handleClose }) => {
    const { data, isLoading, error } = useGetAssignmentsQuery(
        {
            packageId: delivery.package.packageId,
        },
        { refetchOnMountOrArgChange: true }
    );

    return (
        <CustomModal
            open={open}
            handleClose={handleClose}
            sx={{ p: 0, minHeight: 150 }}
        >
            {isLoading ? <LinearProgress /> : <div style={{ height: 4 }} />}
            <Box p={2} pt={1.5}>
                <Typography variant="h5">Assignments</Typography>
                {data &&
                    data.results.map(({ driver, createdAt }) => (
                        <Box
                            display="flex"
                            justifyContent="space-between"
                            mt={1}
                        >
                            <Typography>{`${driver.firstName} ${driver.lastName}`}</Typography>
                            <Typography>
                                {moment(createdAt).format('h:mm A')}
                            </Typography>
                        </Box>
                    ))}
                {data && data.count === 0 && (
                    <Typography py={4} align="center">
                        No Assignments at the moment
                    </Typography>
                )}
                {error && (
                    <FormHelperText sx={{ ml: 2 }} error>
                        {error}
                    </FormHelperText>
                )}
            </Box>
        </CustomModal>
    );
};

export default AssignmentsModal;
