import React, { useState } from 'react';
import {
    Box,
    Button,
    FormHelperText,
    MenuItem,
    Select,
    SelectChangeEvent,
    Stack,
    TextField,
    Typography,
} from '@mui/material';
import { CustomModal, LoadingIndicator } from '@vanoma/ui-components';
import { makeStyles } from '@mui/styles';
import { Customer } from '@vanoma/types';
import { DesktopDatePicker, LocalizationProvider } from '@mui/lab';
import AdapterDateFns from '@mui/lab/AdapterDateFns';
import moment from 'moment';
import { useUpdateCustomerMutation } from '../../../api';

const useStyles = makeStyles({
    paper: {
        borderRadius: 16,
        maxHeight: 140,
        overflow: 'scroll',
    },
});

const EditCustomerModal: React.FC<{
    open: boolean;
    handleClose: () => void;
    customer: Customer;
}> = ({ open, handleClose, customer }) => {
    const classes = useStyles();

    const [weightingFactor, setWeightingFactor] = useState(
        customer.weightingFactor
    );
    const [billingInterval, setBillingInterval] = useState(
        customer.billingInterval
    );
    const [billingGracePeriod, setBillingGracePeriod] = useState(
        customer.billingGracePeriod
    );
    const [postpaidExpiry, setPostpaidExpiry] = React.useState<Date | null>(
        customer.postpaidExpiry ? new Date(customer.postpaidExpiry) : null
    );
    const [fixedPriceAmount, setFixedPriceAmount] = useState(
        customer.fixedPriceAmount
    );
    const [fixedPriceExpiry, setFixedPriceExpiry] = React.useState<Date | null>(
        customer.fixedPriceExpiry ? new Date(customer.fixedPriceExpiry) : null
    );

    const [updateCustomer, { isLoading, error }] = useUpdateCustomerMutation();

    const disableButton =
        isLoading ||
        (customer.weightingFactor === weightingFactor &&
            customer.billingInterval === billingInterval &&
            (customer.billingGracePeriod === billingGracePeriod ||
                Number.isNaN(billingGracePeriod)) &&
            customer.billingInterval === billingInterval &&
            moment(customer.postpaidExpiry).toISOString() ===
                moment(postpaidExpiry).toISOString() &&
            ((customer.fixedPriceAmount === fixedPriceAmount &&
                moment(customer.fixedPriceExpiry).toISOString() ===
                    moment(fixedPriceExpiry).toISOString()) ||
                !fixedPriceExpiry));

    return (
        <CustomModal open={open} handleClose={handleClose}>
            <Typography variant="h5">
                {`Edit Customer: ${customer.businessName}`}
            </Typography>
            <LocalizationProvider dateAdapter={AdapterDateFns}>
                <Stack spacing={2} mt={3}>
                    <Box>
                        <Typography>Weighting factor</Typography>
                        <Select
                            onChange={(e: SelectChangeEvent<string>) =>
                                setWeightingFactor(
                                    Number.parseFloat(e.target.value)
                                )
                            }
                            displayEmpty
                            size="small"
                            MenuProps={{
                                classes: {
                                    paper: classes.paper,
                                },
                            }}
                            fullWidth
                            value={weightingFactor.toString()}
                        >
                            <MenuItem value="1">1.00</MenuItem>
                            <MenuItem value="1.25">1.25</MenuItem>
                        </Select>
                    </Box>
                    <Box>
                        <Typography>Billing interval</Typography>
                        <Select
                            onChange={(e: SelectChangeEvent<string>) =>
                                setBillingInterval(
                                    Number.parseFloat(e.target.value)
                                )
                            }
                            displayEmpty
                            size="small"
                            MenuProps={{
                                classes: {
                                    paper: classes.paper,
                                },
                            }}
                            fullWidth
                            value={billingInterval.toString()}
                        >
                            <MenuItem value="7">7</MenuItem>
                            <MenuItem value="14">14</MenuItem>
                            <MenuItem value="30">30</MenuItem>
                        </Select>
                    </Box>
                    <Box>
                        <Typography>Billing grace period</Typography>
                        <TextField
                            value={billingGracePeriod.toString()}
                            onChange={(e) =>
                                setBillingGracePeriod(
                                    Number.parseFloat(e.target.value)
                                )
                            }
                            size="small"
                            fullWidth
                            type="number"
                        />
                    </Box>
                    <Box>
                        <Typography>Post paid expiry</Typography>
                        <DesktopDatePicker
                            value={postpaidExpiry}
                            onChange={(value) => setPostpaidExpiry(value)}
                            renderInput={(params) => (
                                <TextField
                                    // eslint-disable-next-line react/jsx-props-no-spreading
                                    {...params}
                                    onKeyDown={(e) => e.preventDefault()}
                                    size="small"
                                    fullWidth
                                />
                            )}
                            inputFormat="dd/MM/yyyy"
                        />
                    </Box>
                    <Box>
                        <Typography>Fixed price amount</Typography>
                        <TextField
                            value={fixedPriceAmount?.toString() ?? ''}
                            onChange={(e) =>
                                setFixedPriceAmount(
                                    e.target.value
                                        ? Number.parseFloat(e.target.value)
                                        : null
                                )
                            }
                            size="small"
                            fullWidth
                            type="number"
                        />
                    </Box>
                    <Box>
                        <Typography>Fixed price expiry</Typography>
                        <DesktopDatePicker
                            value={fixedPriceExpiry}
                            onChange={(value) => setFixedPriceExpiry(value)}
                            renderInput={(params) => (
                                <TextField
                                    // eslint-disable-next-line react/jsx-props-no-spreading
                                    {...params}
                                    onKeyDown={(e) => e.preventDefault()}
                                    size="small"
                                    fullWidth
                                />
                            )}
                            inputFormat="dd/MM/yyyy"
                        />
                    </Box>
                    <Button
                        type="submit"
                        sx={{ height: 40 }}
                        fullWidth
                        disabled={disableButton}
                        onClick={() =>
                            updateCustomer({
                                customerId: customer.customerId,
                                weightingFactor,
                                billingInterval,
                                billingGracePeriod,
                                postpaidExpiry: postpaidExpiry
                                    ? moment(postpaidExpiry).toISOString()
                                    : null,
                                fixedPriceAmount,
                                fixedPriceExpiry:
                                    fixedPriceExpiry && fixedPriceAmount
                                        ? moment(fixedPriceExpiry).toISOString()
                                        : null,
                            })
                        }
                    >
                        {isLoading ? <LoadingIndicator /> : 'Update'}
                    </Button>
                    {error && (
                        <FormHelperText error sx={{ mt: 0.5, mx: 1.75 }}>
                            {error}
                        </FormHelperText>
                    )}
                </Stack>
            </LocalizationProvider>
        </CustomModal>
    );
};

export default EditCustomerModal;
