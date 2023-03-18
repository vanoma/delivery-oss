import { Box } from '@mui/material';
import { CustomModal } from '@vanoma/ui-components';
import React from 'react';
import Drivers from './Drivers';
import Stops from './Stops';

const DriverStopsModal: React.FC<{
    isOpen: boolean;
    handleClose: () => void;
}> = ({ isOpen, handleClose }) => {
    const [selectedDriverId, setSelectedDriverId] = React.useState<
        string | null
    >(null);

    return (
        <CustomModal
            open={isOpen}
            handleClose={handleClose}
            sx={{
                width: '80%',
                height: '90%',
                p: 0,
            }}
        >
            <Box
                display="flex"
                flexDirection="column"
                justifyContent="space-between"
                height="100%"
            >
                <Stops selectedDriverId={selectedDriverId} />
                <Drivers
                    selectedDriverId={selectedDriverId}
                    setSelectedDriverId={setSelectedDriverId}
                />
            </Box>
        </CustomModal>
    );
};

export default DriverStopsModal;
