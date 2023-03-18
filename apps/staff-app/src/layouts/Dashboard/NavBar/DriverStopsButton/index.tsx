import React, { useState } from 'react';
import { IconButton } from '@mui/material';
import TwoWheelerIcon from '@mui/icons-material/TwoWheeler';
import DriversStopsModal from './DriverStopsModal';

export default function DriverStopsButton(): JSX.Element {
    const [isOpen, setOpen] = useState<boolean>(false);

    const handleOpen = (): void => setOpen(true);
    const handleClose = (): void => setOpen(false);

    return (
        <>
            <IconButton onClick={handleOpen}>
                <TwoWheelerIcon />
            </IconButton>
            {isOpen && (
                <DriversStopsModal isOpen={isOpen} handleClose={handleClose} />
            )}
        </>
    );
}
