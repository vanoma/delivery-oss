import React, { useState } from 'react';
import {
    Box,
    Button,
    FormHelperText,
    LinearProgress,
    Typography,
} from '@mui/material';
import { CustomModal, LoadingIndicator } from '@vanoma/ui-components';
import { useSelector } from 'react-redux';
import { Delivery } from '../../../../types';
import PickUpStartSelector from '../../../PickUpStartSelector';
import { useTypedDispatch } from '../../../../redux/typedHooks';
import {
    changeCurrentPage,
    selectCurrentPage,
} from '../../../../redux/slices/deliveriesSlice';
import {
    useDuplicateDeliveryOrderMutation,
    useGetDeliveryOrderPackagesQuery,
} from '../../../../api';
import Stop from './Stop';

interface Props {
    open: boolean;
    delivery: Delivery;
    handleClose: () => void;
}

const DuplicateDeliveryModal: React.FC<Props> = ({
    open,
    delivery,
    handleClose,
}) => {
    const [pickUpStart, setPickUpStart] = useState<string>('');
    const currentPage = useSelector(selectCurrentPage);
    const dispatch = useTypedDispatch();

    const [
        duplicateDelivery,
        {
            isLoading: isLoadingDuplicateDelivery,
            error: duplicateDeliveryError,
        },
    ] = useDuplicateDeliveryOrderMutation();
    const { data, isLoading, error } = useGetDeliveryOrderPackagesQuery(
        delivery.package.deliveryOrder.deliveryOrderId
    );

    return (
        <CustomModal
            open={open}
            handleClose={handleClose}
            sx={{ p: 0, width: 700 }}
        >
            {isLoading && <LinearProgress />}
            <Box p={2}>
                <Typography variant="h5" mb={1}>
                    Duplicate order
                </Typography>
                <Typography color="primary" variant="h6" mt={1}>
                    Choose time
                </Typography>
                <PickUpStartSelector
                    selected={pickUpStart}
                    onSelect={setPickUpStart}
                />
                {data && (
                    <>
                        <Typography variant="h6" mt={1}>
                            Pickup from
                        </Typography>
                        <Stop
                            contact={data!.results[0].fromContact}
                            address={data!.results[0].fromAddress}
                        />
                    </>
                )}
                {data && (
                    <>
                        <Typography variant="h6">Drop off to</Typography>
                        {data.results.map(
                            ({ toContact, toAddress, packageId }) => (
                                <Stop
                                    contact={toContact}
                                    address={toAddress}
                                    key={packageId}
                                />
                            )
                        )}
                    </>
                )}

                <Button
                    variant="contained"
                    fullWidth
                    sx={{ height: 40, mt: 2 }}
                    onClick={() =>
                        duplicateDelivery({
                            deliveryOrderId:
                                delivery.package.deliveryOrder.deliveryOrderId,
                            pickUpStart,
                        })
                            .unwrap()
                            .then(() => {
                                dispatch(changeCurrentPage(currentPage));
                                handleClose();
                            })
                    }
                    disabled={pickUpStart === '' || isLoadingDuplicateDelivery}
                >
                    {isLoadingDuplicateDelivery ? (
                        <LoadingIndicator />
                    ) : (
                        'Duplicate'
                    )}
                </Button>
                {(duplicateDeliveryError || error) && (
                    <FormHelperText sx={{ mt: 2 }} error>
                        {duplicateDeliveryError || error}
                    </FormHelperText>
                )}
            </Box>
        </CustomModal>
    );
};

export default DuplicateDeliveryModal;
