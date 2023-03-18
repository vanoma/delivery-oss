import React, { useState } from 'react';
import EditIcon from '@mui/icons-material/Edit';
import { IconButton } from '@mui/material';
import { Customer } from '@vanoma/types';
import EditCustomerModal from './EditCustomerModal';

const EditButton: React.FC<{ customer: Customer }> = ({ customer }) => {
    const [openModal, setOpenModal] = useState(false);

    return (
        <>
            <IconButton sx={{ ml: 2 }} onClick={() => setOpenModal(true)}>
                <EditIcon />
            </IconButton>
            {openModal && (
                <EditCustomerModal
                    customer={customer}
                    open={openModal}
                    handleClose={() => setOpenModal(false)}
                />
            )}
        </>
    );
};

export default EditButton;
