import React, { useState } from 'react';
import { useSelector } from 'react-redux';
import DoNotDisturbIcon from '@mui/icons-material/DoNotDisturb';
import * as Yup from 'yup';
import { Delivery } from '../../../types';
import {
    changeCurrentPage,
    selectCurrentTab,
} from '../../../redux/slices/deliveriesSlice';
import { DELIVERIES_TAB } from '../../../routeNames';
import ActionMenuItem from './ActionMenuItem';
import TextFieldModal from '../../TextFieldModal';
import { useTypedDispatch } from '../../../redux/typedHooks';
import { useCancelPackageMutation } from '../../../api';

interface Props {
    delivery: Delivery;
    handleCloseMenu: () => void;
}

const CancelDelivery: React.FC<Props> = ({ delivery, handleCloseMenu }) => {
    const [openModal, setOpenModal] = useState<boolean>(false);
    const currentTab = useSelector(selectCurrentTab);

    const [cancelPackage, { isLoading, error }] = useCancelPackageMutation();
    const dispatch = useTypedDispatch();

    const disabled = [DELIVERIES_TAB.COMPLETE, DELIVERIES_TAB.SEARCH].includes(
        currentTab
    );

    const handleOpenModal = (): void => setOpenModal(true);
    const handleCloseModal = (): void => {
        setOpenModal(false);
        handleCloseMenu();
    };

    return (
        <>
            <ActionMenuItem
                icon={<DoNotDisturbIcon />}
                label="Cancel Delivery"
                disabled={disabled}
                onClick={handleOpenModal}
            />
            {openModal && (
                <TextFieldModal
                    open={openModal}
                    title="Cancel Delivery"
                    inputLabel="Reason"
                    inputName="reason"
                    buttonText="Cancel"
                    isLoading={isLoading}
                    error={error}
                    handleClose={handleCloseModal}
                    handleSubmit={(note) =>
                        cancelPackage({
                            note,
                            packageId: delivery.package.packageId,
                        })
                            .unwrap()
                            .then(() => dispatch(changeCurrentPage(1)))
                    }
                    validationSchema={Yup.object().shape({
                        reason: Yup.string().required('Reason is required'),
                    })}
                />
            )}
        </>
    );
};

export default CancelDelivery;
