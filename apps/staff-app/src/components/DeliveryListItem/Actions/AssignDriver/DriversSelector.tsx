import React, { ReactElement, useState } from 'react';
import { styled } from '@mui/material/styles';
import {
    Box,
    Button,
    ListItem,
    ListItemIcon,
    ListItemText,
    FormHelperText,
    Typography,
} from '@mui/material';
import { TimelineDot } from '@mui/lab';
import { LoadingIndicator, CustomList } from '@vanoma/ui-components';
import { useSelector } from 'react-redux';
import { DriverStatus } from '@vanoma/types';
import {
    useGetDriversQuery,
    useCreateAssignmentMutation,
    useCancelAssignmentMutation,
} from '../../../../api';
import { Delivery } from '../../../../types';
import {
    changeCurrentPage,
    selectCurrentPage,
} from '../../../../redux/slices/deliveriesSlice';
import { useTypedDispatch } from '../../../../redux/typedHooks';
import { getDriverStatusColor } from '../../../../helpers/driver';

const DriversContainer = styled('div')(({ theme }) => ({
    border: `1px solid ${theme.palette.primary.light}`,
    borderRadius: `0 0 ${theme.spacing(2)} ${theme.spacing(2)}`,
    margin: `0 ${theme.spacing(1.875)}`,
    backgroundColor: theme.palette.background.default,
    boxShadow: theme!.shadows[24],
    paddingBottom: 16,
    paddingTop: 16,
    marginBottom: 16,
}));

const DriversSelector = ({
    delivery,
    isAssigned,
    closeActionModal,
}: {
    delivery: Delivery;
    isAssigned: boolean;
    closeActionModal: () => void;
}): ReactElement => {
    const [selectedDriverId, setSelectedDriverId] = useState<string | null>();

    const currentPage = useSelector(selectCurrentPage);
    const dispatch = useTypedDispatch();

    const { data, error: driversError } = useGetDriversQuery({
        status: [DriverStatus.ACTIVE, DriverStatus.PENDING],
        sort: 'first_name',
    });
    const [
        createAssignment,
        { error: createAssignmentError, isLoading: isCreateAssignmentLoading },
    ] = useCreateAssignmentMutation();
    const [
        cancelAssignment,
        { error: cancelAssignmentError, isLoading: isCancelAssignmentLoading },
    ] = useCancelAssignmentMutation();

    const handleConfirm = async (): Promise<void> => {
        if (isAssigned) {
            await cancelAssignment(delivery.package.assignmentId!).unwrap();
        }

        await createAssignment({
            driverId: selectedDriverId!,
            packageId: delivery.package.packageId,
        }).unwrap();
        dispatch(changeCurrentPage(currentPage));
        closeActionModal();
    };

    return (
        <DriversContainer>
            {data && (
                <CustomList>
                    {data.results.map((driver) => (
                        <ListItem
                            button
                            divider
                            key={driver.driverId}
                            selected={driver.driverId === selectedDriverId}
                            onClick={() => setSelectedDriverId(driver.driverId)}
                            disabled={
                                isCreateAssignmentLoading ||
                                isCancelAssignmentLoading
                            }
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
            {selectedDriverId && (
                <Box
                    sx={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        p: 2,
                    }}
                >
                    <Typography>
                        {isAssigned ? 'Re-assignment' : 'Assignment'}
                    </Typography>
                    <Button
                        size="small"
                        disabled={
                            isCreateAssignmentLoading ||
                            isCancelAssignmentLoading
                        }
                        onClick={handleConfirm}
                    >
                        {isCreateAssignmentLoading ||
                        isCancelAssignmentLoading ? (
                            <LoadingIndicator />
                        ) : (
                            'Confirm'
                        )}
                    </Button>
                </Box>
            )}
            {(driversError ||
                createAssignmentError ||
                cancelAssignmentError) && (
                <FormHelperText sx={{ ml: 2 }} error>
                    {driversError ||
                        createAssignmentError ||
                        cancelAssignmentError}
                </FormHelperText>
            )}
        </DriversContainer>
    );
};

export default DriversSelector;
