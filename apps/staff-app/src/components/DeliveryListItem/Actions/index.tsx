import React, { ReactElement, useState, useRef } from 'react';
import { IconButton, Box } from '@mui/material';
import MoreHorizIcon from '@mui/icons-material/MoreHoriz';
import { MenuPopover } from '@vanoma/ui-components';
import ChangePickup from './ChangePickup';
import PlaceDelivery from './PlaceDelivery';
import AssignDriver from './AssignDriver';
import ConfirmPayment from './ConfirmPayment';
import CancelDelivery from './CancelDelivery';
import { Delivery } from '../../../types';
import FreezeAssignment from './FreezeAssignment';
import DuplicateDelivery from './DuplicateDelivery';
import CancelAssignment from './CancelAssignment';
import EditFromNote from './EditFromNote';
import EditToNote from './EditToNote';
import Assignments from './Assignments';
import DisableNotifications from './DisableNotifications';

interface Props {
    delivery: Delivery;
}

const Actions: React.FC<Props> = ({ delivery }): ReactElement => {
    const [openMenu, setOpenMenu] = useState(false);
    const anchorRef = useRef(null);

    const handleOpenMenu = (): void => setOpenMenu(true);
    const handleCloseMenu = (): void => setOpenMenu(false);

    return (
        <>
            <Box
                sx={{
                    flexGrow: 1,
                    justifyContent: 'flex-end',
                    display: 'flex',
                    marginLeft: 'auto',
                }}
            >
                <IconButton
                    size="small"
                    sx={{ p: 0.5 }}
                    ref={anchorRef}
                    onClick={handleOpenMenu}
                >
                    <MoreHorizIcon />
                </IconButton>
                <MenuPopover
                    open={openMenu}
                    onClose={handleCloseMenu}
                    anchorRef={anchorRef}
                    sx={{ py: 1.25, overflow: 'scroll' }}
                >
                    <PlaceDelivery
                        delivery={delivery}
                        handleCloseMenu={handleCloseMenu}
                    />
                    <AssignDriver
                        delivery={delivery}
                        handleCloseMenu={handleCloseMenu}
                    />
                    <CancelAssignment
                        delivery={delivery}
                        handleCloseMenu={handleCloseMenu}
                    />
                    <FreezeAssignment
                        delivery={delivery}
                        handleCloseMenu={handleCloseMenu}
                    />
                    <Assignments
                        delivery={delivery}
                        handleCloseMenu={handleCloseMenu}
                    />
                    <ConfirmPayment
                        delivery={delivery}
                        handleCloseMenu={handleCloseMenu}
                    />
                    <DuplicateDelivery
                        delivery={delivery}
                        handleCloseMenu={handleCloseMenu}
                    />
                    <DisableNotifications
                        delivery={delivery}
                        handleCloseMenu={handleCloseMenu}
                    />
                    <CancelDelivery
                        delivery={delivery}
                        handleCloseMenu={handleCloseMenu}
                    />
                    <EditFromNote
                        delivery={delivery}
                        handleCloseMenu={handleCloseMenu}
                    />
                    <EditToNote
                        delivery={delivery}
                        handleCloseMenu={handleCloseMenu}
                    />
                    <ChangePickup
                        delivery={delivery}
                        handleCloseMenu={handleCloseMenu}
                    />
                </MenuPopover>
            </Box>
        </>
    );
};

export default Actions;
