import React, { useEffect, useState } from 'react';
import { IconButton } from '@mui/material';
import MapIcon from '@mui/icons-material/Map';
import { CustomModal } from '@vanoma/ui-components';
import { useSelector } from 'react-redux';
import DriversMap from '../../../components/DriversMap';
import { useTypedDispatch } from '../../../redux/typedHooks';
import {
    getDeliveries,
    selectCurrentTab,
} from '../../../redux/slices/deliveriesSlice';
import { DELIVERIES_TAB } from '../../../routeNames';

export default function DriversMapButton(): JSX.Element {
    const [isOpen, setOpen] = useState<boolean>(false);

    const handleOpen = (): void => setOpen(true);
    const handleClose = (): void => setOpen(false);

    const dispatch = useTypedDispatch();
    const currentTab = useSelector(selectCurrentTab);

    useEffect(() => {
        if (isOpen) {
            if (currentTab === DELIVERIES_TAB.ACTIVE) {
                dispatch(getDeliveries());
            }
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [isOpen]);

    return (
        <>
            <IconButton onClick={handleOpen}>
                <MapIcon />
            </IconButton>
            {isOpen && (
                <CustomModal
                    open={isOpen}
                    handleClose={handleClose}
                    sx={{ width: '90%', height: '90%', p: 0 }}
                >
                    <DriversMap
                        handleOpen={handleOpen}
                        handleClose={handleClose}
                    />
                </CustomModal>
            )}
        </>
    );
}
