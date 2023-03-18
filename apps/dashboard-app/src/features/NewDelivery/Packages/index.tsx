import React, { useState } from 'react';
import { alpha, Box, Button, Container } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import AddIcon from '@mui/icons-material/Add';
import { LoadingIndicator } from '@vanoma/ui-components';
import StepHead from '../StepHead';
import { EDIT_MODE } from '../constants';
import StepContent from '../StepContent';
import Package from './Package';
import {
    createPackage,
    updateAllPackagesAndGetDeliveryOrderPrice,
    selectIsLoading,
    selectPackages,
    selectPackageCount,
} from '../slice';
import { useTypedDispatch } from '../../../helpers/reduxToolkit';

const Packages: React.FC<{
    isDone: boolean;
    editMode: EDIT_MODE;
    isEditing: boolean;
    resetDelivery: () => void;
    onCompleteStep: () => void;
    onEditStep: () => void;
}> = ({
    isDone,
    editMode,
    isEditing,
    resetDelivery,
    onCompleteStep,
    onEditStep,
}) => {
    const { t } = useTranslation();
    const dispatch = useTypedDispatch();
    const packages = useSelector(selectPackages);
    const isLoading = useSelector(selectIsLoading);
    const packageCount = useSelector(selectPackageCount);
    const [clickedButton, setClickedButton] = useState(0);

    const isExpanded = isDone || isEditing;
    const showEditButton =
        isDone && !isEditing && editMode !== EDIT_MODE.RANDOM;
    const hasPackageSize =
        packageCount > 0 && packages[packageCount - 1].size !== null;

    const handleAdd = (): void => {
        setClickedButton(0);
        dispatch(createPackage());
    };

    const handleNext = (): void => {
        setClickedButton(1);
        dispatch(updateAllPackagesAndGetDeliveryOrderPrice())
            .unwrap()
            .then(() => onCompleteStep());
    };

    return (
        <Container disableGutters>
            <StepHead
                title={t('delivery.stops.to')}
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
                <Box
                    sx={(theme) => ({
                        background: alpha(theme.palette.grey[600], 0.2),
                        p: 1.25,
                    })}
                >
                    {packages.map((pkg, index) => (
                        <Package
                            pkg={pkg}
                            isEditing={isEditing}
                            isLast={index === packageCount - 1}
                            canDelete={packageCount > 1}
                            resetDelivery={resetDelivery}
                            key={pkg.packageId}
                        />
                    ))}
                </Box>
                {isEditing && (
                    <Box
                        display="flex"
                        justifyContent="flex-end"
                        px={1.25}
                        mt={2}
                    >
                        <Button
                            size="small"
                            variant="outlined"
                            sx={{ mr: 2, pr: 2 }}
                            startIcon={<AddIcon />}
                            disabled={!isDone || isLoading || !hasPackageSize}
                            onClick={handleAdd}
                        >
                            {isLoading && clickedButton === 0 ? (
                                <LoadingIndicator />
                            ) : (
                                t('delivery.stop.add')
                            )}
                        </Button>
                        <Button
                            size="small"
                            variant="outlined"
                            disabled={!isDone || isLoading || !hasPackageSize}
                            onClick={handleNext}
                        >
                            {isLoading && clickedButton === 1 ? (
                                <LoadingIndicator />
                            ) : (
                                t('delivery.stop.next')
                            )}
                        </Button>
                    </Box>
                )}
            </StepContent>
        </Container>
    );
};

export default Packages;
