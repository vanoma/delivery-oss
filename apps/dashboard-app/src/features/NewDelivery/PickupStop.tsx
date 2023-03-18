import { Box, Button, Container } from '@mui/material';
import { LoadingIndicator } from '@vanoma/ui-components';
import React from 'react';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import { useTypedDispatch } from '../../helpers/reduxToolkit';
import { EDIT_MODE } from './constants';
import Instructions from './Instructions';
import {
    createPackage,
    selectFromAddress,
    selectFromContact,
    selectFromNote,
    selectIsLoading,
    selectPackageCount,
    updateAllPackages,
    updateFromNote,
} from './slice';
import StepContent from './StepContent';
import StepHead from './StepHead';
import Stop from './Stop';

const PickupStop: React.FC<{
    isDone: boolean;
    editMode: EDIT_MODE;
    isEditing: boolean;
    onCompleteStep: () => void;
    onEditStep: () => void;
}> = ({ isDone, editMode, isEditing, onCompleteStep, onEditStep }) => {
    const { t } = useTranslation();
    const dispatch = useTypedDispatch();

    const fromContact = useSelector(selectFromContact);
    const fromAddress = useSelector(selectFromAddress);
    const fromNote = useSelector(selectFromNote);
    const isLoading = useSelector(selectIsLoading);
    const packageCount = useSelector(selectPackageCount);

    const isExpanded = isDone || isEditing;
    const showEditButton =
        isDone && !isEditing && editMode !== EDIT_MODE.RANDOM;

    const handleNext = async (): Promise<void> => {
        if (packageCount === 0) {
            await dispatch(createPackage()).unwrap();
        } else {
            await dispatch(updateAllPackages()).unwrap();
        }
        onCompleteStep();
    };

    return (
        <Container disableGutters>
            <StepHead
                title={t('delivery.stops.from')}
                done={isDone}
                current={isEditing}
            >
                {showEditButton && (
                    <Button
                        size="small"
                        variant="outlined"
                        disabled={isLoading}
                        onClick={onEditStep}
                    >
                        {t('delivery.stop.edit')}
                    </Button>
                )}
            </StepHead>
            <StepContent in={isExpanded} sx={{ pl: 4.5 }}>
                <Stop
                    contact={fromContact}
                    address={fromAddress}
                    isEditing={isEditing}
                />
                {fromAddress && (
                    <Instructions
                        isEditing={isEditing}
                        note={fromNote}
                        label={t('delivery.package.pickupInstructionsOptional')}
                        placeholder={t('delivery.from.egFirstDoorOnTheRight')}
                        onChange={(value) => dispatch(updateFromNote(value))}
                        property={t('delivery.package.pickupInstructions')}
                        pt={2}
                        pb={0}
                    />
                )}
                {isEditing && (
                    <Box
                        display="flex"
                        justifyContent="flex-end"
                        mt={2}
                        pr={1.875}
                    >
                        <Button
                            size="small"
                            variant="outlined"
                            disabled={!isDone || isLoading}
                            onClick={handleNext}
                        >
                            {!isLoading ? (
                                t('delivery.stop.next')
                            ) : (
                                <LoadingIndicator />
                            )}
                        </Button>
                    </Box>
                )}
            </StepContent>
        </Container>
    );
};

export default PickupStop;
