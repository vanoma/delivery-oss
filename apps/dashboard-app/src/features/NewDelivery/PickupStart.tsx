import {
    Box,
    Container,
    ListItemIcon,
    ListItemText,
    MenuItem,
    Select,
    SelectChangeEvent,
    Typography,
    Button,
    FormHelperText,
} from '@mui/material';
import React from 'react';
import { useTranslation } from 'react-i18next';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import { makeStyles } from '@mui/styles';
import moment from 'moment';
import { useSelector } from 'react-redux';
import { getTimeIntervals } from '@vanoma/helpers';
import { useTypedDispatch } from '../../helpers/reduxToolkit';
import StepHead from './StepHead';
import StepContent from './StepContent';
import SelectedInfo from '../../components/SelectedInfo';
import CustomSnackBar from '../../components/CustomSnackBar';
import {
    DEFAULT_PICKUP_START,
    selectIsLoading,
    selectPickUpStartError,
    updatePickUpStart,
    updateAllPackages,
    selectPickUpStart,
} from './slice';
import { useGetBusinessHoursQuery } from '../../api';
import i18n from '../../locales/i18n';
import { EDIT_MODE } from './constants';
import { selectCustomerId } from '../../redux/slices/authenticationSlice';

const useStyles = makeStyles({
    paper: {
        borderRadius: 16,
        maxHeight: 140,
        overflow: 'scroll',
    },
});

const displayPickupTime = (time: string): string => {
    const now = moment();
    // Selected time is in UTC but we want it in current user's TZ
    const selectedTime = moment(time);
    const day =
        now.isoWeekday() === selectedTime.isoWeekday() ? 'today' : 'tomorrow';
    return `${i18n.t(`delivery.time.${day}`)} ${selectedTime.format('H:mm')}`;
};

const displayDeliveryTime = (time: string): string => {
    const now = moment();
    const deliveryTimeStart = moment(time).add(30, 'm');
    const deliveryTimeEnd = moment(time).add(2, 'h');
    const day =
        now.isoWeekday() === deliveryTimeStart.isoWeekday()
            ? 'today'
            : 'tomorrow';
    return `${i18n.t(`delivery.time.${day}`)} ${deliveryTimeStart.format(
        'H:mm'
    )} - ${deliveryTimeEnd.format('H:mm')}`;
};

const PickupStart: React.FC<{
    isDone: boolean;
    editMode: EDIT_MODE;
    isEditing: boolean;
    onCompleteStep: () => void;
    onEditStep: () => void;
}> = ({ isDone, editMode, isEditing, onCompleteStep, onEditStep }) => {
    const pickUpStart = useSelector(selectPickUpStart);
    const pickupStartError = useSelector(selectPickUpStartError);
    const isLoading = useSelector(selectIsLoading);
    const customerId = useSelector(selectCustomerId);
    const { data: businessHours, error, refetch } = useGetBusinessHoursQuery();

    const classes = useStyles();
    const { t } = useTranslation();
    const dispatch = useTypedDispatch();

    const handleChange = async (value: string | null): Promise<void> => {
        dispatch(updatePickUpStart(value));
        await dispatch(updateAllPackages()).unwrap();
        onCompleteStep();
    };

    const isExpanded = isDone || isEditing;
    const showEditButton =
        isDone && !isEditing && editMode !== EDIT_MODE.RANDOM;

    const exclusives = [
        'ebfb2198f9f0479cba67c630705fdba6',
        'eec3f1a97676471dba98a119aaeae63d',
    ];

    return (
        <Container disableGutters sx={{ pt: 2 }}>
            <StepHead
                title={t('delivery.time.pickUpTime')}
                done={isDone}
                current={isEditing}
            >
                {showEditButton && (
                    <Button
                        size="small"
                        variant="outlined"
                        disabled={isLoading}
                        onClick={() => {
                            dispatch(updatePickUpStart(null));
                            onEditStep();
                        }}
                    >
                        {t('delivery.stop.edit')}
                    </Button>
                )}
            </StepHead>
            <StepContent in={isExpanded}>
                {isDone ? (
                    <>
                        <SelectedInfo>
                            <Typography>
                                {pickUpStart === DEFAULT_PICKUP_START
                                    ? t('delivery.time.soonEnough')
                                    : displayPickupTime(pickUpStart!)}
                            </Typography>
                        </SelectedInfo>
                        <Typography
                            variant="h5"
                            mt={2}
                            mb={1.5}
                            pl={{ xs: 2, sm: 0 }}
                        >
                            {t('delivery.time.deliveryTime')}
                        </Typography>
                        <SelectedInfo>
                            <Typography color="primary" fontWeight="bold">
                                {displayDeliveryTime(
                                    pickUpStart === DEFAULT_PICKUP_START
                                        ? new Date().toISOString()
                                        : pickUpStart!
                                )}
                            </Typography>
                        </SelectedInfo>
                    </>
                ) : (
                    <Select
                        onChange={(e: SelectChangeEvent<string>) =>
                            handleChange(e.target.value)
                        }
                        displayEmpty
                        size="small"
                        MenuProps={{
                            classes: {
                                paper: classes.paper,
                            },
                        }}
                        fullWidth
                        sx={{ maxWidth: 320, mx: 'auto' }}
                    >
                        <MenuItem>
                            <ListItemText>
                                {t('delivery.time.prompt')}
                            </ListItemText>
                        </MenuItem>
                        <MenuItem value={DEFAULT_PICKUP_START}>
                            <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                <ListItemIcon sx={{ minWidth: 36 }}>
                                    <AccessTimeIcon />
                                </ListItemIcon>
                                <ListItemText>
                                    {t('delivery.time.soonEnough')}
                                </ListItemText>
                            </Box>
                        </MenuItem>
                        {businessHours && businessHours.length !== 0 ? (
                            getTimeIntervals(
                                businessHours,
                                {
                                    today: t('delivery.time.today'),
                                    tomorrow: t('delivery.time.tomorrow'),
                                },
                                customerId && !exclusives.includes(customerId)
                                    ? 100
                                    : 0
                            ).map(({ label, value }) => (
                                <MenuItem value={value} key={value}>
                                    <Box
                                        sx={{
                                            display: 'flex',
                                            alignItems: 'center',
                                        }}
                                    >
                                        <ListItemIcon sx={{ minWidth: 36 }}>
                                            <AccessTimeIcon />
                                        </ListItemIcon>
                                        <ListItemText>{label}</ListItemText>
                                    </Box>
                                </MenuItem>
                            ))
                        ) : (
                            <MenuItem />
                        )}
                    </Select>
                )}
                {pickupStartError && (
                    <FormHelperText error>{pickupStartError}</FormHelperText>
                )}
            </StepContent>
            <CustomSnackBar
                message={error as string}
                severity="error"
                onRetry={refetch}
            />
        </Container>
    );
};

export default PickupStart;
