import React, { useState } from 'react';
import EditIcon from '@mui/icons-material/Edit';
import { Delivery } from '../../../types';
import { changeCurrentPage } from '../../../redux/slices/deliveriesSlice';
import ActionMenuItem from './ActionMenuItem';
import TextFieldModal from '../../TextFieldModal';
import { useTypedDispatch } from '../../../redux/typedHooks';
import { useUpdatePackageMutation } from '../../../api';

interface Props {
    delivery: Delivery;
    handleCloseMenu: () => void;
}

const EditToNote: React.FC<Props> = ({ delivery, handleCloseMenu }) => {
    const [openModal, setOpenModal] = useState<boolean>(false);

    const [updatePackage, { isLoading, error }] = useUpdatePackageMutation();
    const dispatch = useTypedDispatch();

    const handleOpenModal = (): void => setOpenModal(true);
    const handleCloseModal = (): void => {
        setOpenModal(false);
        handleCloseMenu();
    };

    return (
        <>
            <ActionMenuItem
                icon={<EditIcon />}
                label="Edit drop off note"
                disabled={false}
                onClick={handleOpenModal}
            />
            {openModal && (
                <TextFieldModal
                    open={openModal}
                    title="Edit drop off note"
                    inputLabel="Note"
                    inputName="note"
                    initialValue={delivery.package.toNote ?? ''}
                    buttonText="Update"
                    isLoading={isLoading}
                    error={error}
                    handleClose={handleCloseModal}
                    handleSubmit={(toNote) =>
                        updatePackage({
                            toNote,
                            packageId: delivery.package.packageId,
                        })
                            .unwrap()
                            .then(() => dispatch(changeCurrentPage(1)))
                    }
                />
            )}
        </>
    );
};

export default EditToNote;
