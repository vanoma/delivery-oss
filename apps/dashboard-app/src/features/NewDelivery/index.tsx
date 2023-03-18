import React, { useEffect, useState } from 'react';
import { Card, Divider, Stack } from '@mui/material';
import { styled } from '@mui/material/styles';
import { useSelector } from 'react-redux';
import {
    selectError,
    resetState,
    resetError,
    selectPackages,
    selectPickUpStart,
    selectFromContact,
    selectFromAddress,
    selectPackageCount,
} from './slice';
import PickupStart from './PickupStart';
import Payment from './Payment';
import CustomSnackBar from '../../components/CustomSnackBar';
import BillingStatusContainer from '../../components/BillingStatusContainer';
import { EDIT_MODE } from './constants';
import {
    selectAgentBranch,
    selectDefaultAddress,
    selectDefaultContact,
} from '../../redux/slices/authenticationSlice';
import { useTypedDispatch } from '../../helpers/reduxToolkit';
import Packages from './Packages';
import PickupStop from './PickupStop';

const StyledCard = styled(Card)(({ theme }) => ({
    display: 'flex',
    padding: `0 ${theme.spacing(2)}`,
    [theme.breakpoints.down('sm')]: {
        paddingLeft: theme.spacing(1),
    },
    [theme.breakpoints.down('md')]: {
        paddingRight: 0,
    },
    '&:before': {
        width: 48,
        backgroundColor: theme.palette.primary.light,
        top: 0,
        left: theme.spacing(2),
        bottom: 0,
        content: "''",
        position: 'absolute',
        zIndex: -100,
        [theme.breakpoints.down('sm')]: {
            width: 42,
            left: 0,
        },
    },
}));

const ColoredVerticalLine = styled('div')(({ theme }) => ({
    width: 48,
    backgroundColor: theme.palette.primary.light,
    [theme.breakpoints.down('md')]: {
        display: 'none',
    },
}));

const NewDelivery = (): JSX.Element => {
    const [editMode, setEditMode] = useState<EDIT_MODE>(EDIT_MODE.DEFAULT);
    const [currentStep, setCurrentStep] = useState<number>(0);
    const [previousStep, setPreviousStep] = useState<number | null>(null);

    const error = useSelector(selectError);
    const packages = useSelector(selectPackages);
    const packageCount = useSelector(selectPackageCount);
    const defaultContact = useSelector(selectDefaultContact);
    const defaultAddress = useSelector(selectDefaultAddress);
    const pickUpStart = useSelector(selectPickUpStart);
    const fromContact = useSelector(selectFromContact);
    const fromAddress = useSelector(selectFromAddress);
    const agentBranch = useSelector(selectAgentBranch);

    const isPickupStartDone = pickUpStart !== null;
    const isPickupStopDone = fromContact !== null && fromAddress !== null;
    const isPackagesDone =
        packageCount > 0 && packages[packages.length - 1]!.toAddress !== null;

    const dispatch = useTypedDispatch();

    const handleEditStep = (value: number): void => {
        setPreviousStep(currentStep);
        setCurrentStep(value);
        setEditMode(EDIT_MODE.RANDOM);
    };

    const handleCompleteStep = (value: number): void => {
        if (previousStep !== null) {
            setCurrentStep(previousStep);
            setPreviousStep(null);
        } else {
            setCurrentStep(value);
        }

        setEditMode(EDIT_MODE.DEFAULT);
    };

    const resetDelivery = (): void => {
        dispatch(resetState({ defaultContact, defaultAddress, agentBranch }));
        setCurrentStep(1);
    };

    useEffect(() => {
        // On mount, pick the correct step to edit based on which ones are complete.
        if (isPickupStartDone) {
            // if pickup stop is done, we have to check if we have created at least one
            // package before setting the packages step as current. That's because we
            // create a new package when user clicks "next" from pickup stop. If we don't
            // have any packages, we wouldn't be able to recover.
            if (isPickupStopDone && packageCount > 0) {
                // The packages step could also be done but we will defaulting to it
                // anywhere. This way, if the user did not click "next" to get pricing,
                // they will be forced to do so.
                setCurrentStep(2);
            } else {
                setCurrentStep(1);
            }
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return (
        <BillingStatusContainer>
            <StyledCard>
                <Stack
                    spacing={1}
                    sx={{
                        width: '100%',
                        pl: { xs: 0, sm: 1.5 },
                        py: { xs: 2, sm: 3 },
                    }}
                    divider={
                        <div>
                            <Divider
                                component="div"
                                sx={{
                                    marginLeft: { xs: 4.2, sm: 5.8 },
                                    borderBottomWidth: 4,
                                }}
                            />
                        </div>
                    }
                >
                    <PickupStart
                        isDone={isPickupStartDone}
                        editMode={editMode}
                        isEditing={currentStep === 0}
                        onCompleteStep={() => handleCompleteStep(1)}
                        onEditStep={() => handleEditStep(0)}
                    />
                    <PickupStop
                        isDone={isPickupStopDone}
                        editMode={editMode}
                        isEditing={currentStep === 1}
                        onCompleteStep={() => handleCompleteStep(2)}
                        onEditStep={() => handleEditStep(1)}
                    />
                    <Packages
                        isDone={isPackagesDone}
                        editMode={editMode}
                        isEditing={currentStep === 2}
                        resetDelivery={resetDelivery}
                        onCompleteStep={() => handleCompleteStep(3)}
                        onEditStep={() => handleEditStep(2)}
                    />
                    <Payment isEditing={currentStep === 3} />
                </Stack>
                <ColoredVerticalLine />
            </StyledCard>
            <CustomSnackBar
                message={error}
                severity="error"
                onReset={() => dispatch(resetError())}
            />
        </BillingStatusContainer>
    );
};

export default NewDelivery;
