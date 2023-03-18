import React, { useState } from 'react';
import { Button, FormHelperText, Typography, TextField } from '@mui/material';
import { CustomModal, LoadingIndicator } from '@vanoma/ui-components';
import { useSelector } from 'react-redux';
import { useUpdatePackageMutation } from '../../../../api';
import { Delivery } from '../../../../types';
import { useTypedDispatch } from '../../../../redux/typedHooks';
import {
    changeCurrentPage,
    selectCurrentPage,
} from '../../../../redux/slices/deliveriesSlice';
import PickUpStartSelector from '../../../PickUpStartSelector';

interface Props {
    open: boolean;
    delivery: Delivery;
    handleClose: () => void;
}

const ChangePickModal: React.FC<Props> = ({ open, delivery, handleClose }) => {
    const [pickUpStart, setPickUpStart] = useState('');
    const [pickUpChangeNote, setPickUpChangeNote] = useState('');
    const currentPage = useSelector(selectCurrentPage);
    const dispatch = useTypedDispatch();

    const [updatePackage, { isLoading, error }] = useUpdatePackageMutation();

    return (
        <CustomModal open={open} handleClose={handleClose}>
            <Typography variant="h5">Update pickup time</Typography>
            <TextField
                label="Reason"
                name="reason"
                value={pickUpChangeNote}
                onChange={(e) => setPickUpChangeNote(e.target.value)}
                size="small"
                fullWidth
                sx={{ mt: 2 }}
            />
            <PickUpStartSelector
                selected={pickUpStart}
                onSelect={setPickUpStart}
            />
            <Button
                variant="contained"
                fullWidth
                sx={{ height: 40, mt: 2 }}
                onClick={() =>
                    updatePackage({
                        packageId: delivery.package.packageId,
                        pickUpStart,
                        pickUpChangeNote,
                    })
                        .unwrap()
                        .then(() => {
                            dispatch(changeCurrentPage(currentPage));
                            handleClose();
                        })
                }
                disabled={
                    pickUpStart === '' ||
                    pickUpChangeNote.trim() === '' ||
                    isLoading
                }
            >
                {isLoading ? <LoadingIndicator /> : 'Update'}
            </Button>
            {error && (
                <FormHelperText sx={{ mt: 2 }} error>
                    {error}
                </FormHelperText>
            )}
        </CustomModal>
    );
};

export default ChangePickModal;
