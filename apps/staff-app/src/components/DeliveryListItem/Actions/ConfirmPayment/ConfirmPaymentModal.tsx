import React from 'react';
import {
    Button,
    FormHelperText,
    Stack,
    TextField,
    Typography,
    Card,
    Box,
} from '@mui/material';
import { LocalizationProvider, StaticDateTimePicker } from '@mui/lab';
import AdapterDateFns from '@mui/lab/AdapterDateFns';
import { Formik, Form, FormikProps } from 'formik';
import * as Yup from 'yup';
import moment from 'moment';
import { CustomModal, LoadingIndicator } from '@vanoma/ui-components';
import { useSelector } from 'react-redux';
import { useConfirmPaymentMutation } from '../../../../api';
import { Delivery } from '../../../../types';
import { useTypedDispatch } from '../../../../redux/typedHooks';
import {
    changeCurrentPage,
    selectCurrentPage,
} from '../../../../redux/slices/deliveriesSlice';
import { PaymentFormValue } from './types';
import SelectPaymentMethod from './SelectPaymentMethod';

const initialValues: PaymentFormValue = {
    paymentMethodId: '',
    operatorTransactionId: '',
    description: 'MoMo Code transaction',
    paymentTime: undefined,
};

const confirmPaymentValidationSchema: Yup.SchemaOf<PaymentFormValue> =
    Yup.object().shape({
        paymentMethodId: Yup.string().required(
            'Please choose the payment method'
        ),
        operatorTransactionId: Yup.string().required(
            'Please enter operator transaction id'
        ),
        description: Yup.string().required('Please provide a description'),
        paymentTime: Yup.date().required('Please enter payment time'),
    });

interface Props {
    open: boolean;
    delivery: Delivery;
    handleClose: () => void;
}

const ConfirmPaymentModal: React.FC<Props> = ({
    open,
    delivery,
    handleClose,
}) => {
    const currentPage = useSelector(selectCurrentPage);
    const dispatch = useTypedDispatch();

    const { customerId } = delivery.package.deliveryOrder;
    const [confirmPayment, { isLoading, error }] = useConfirmPaymentMutation();

    const handleSubmit = (values: PaymentFormValue): void => {
        const { paymentTime, ...others } = values;
        const { deliveryOrderId } = delivery.package.deliveryOrder;
        confirmPayment({
            deliveryOrderId,
            totalAmount: delivery.package.totalAmount,
            paymentTime: moment(paymentTime).toISOString(),
            ...others,
        })
            .unwrap()
            .then(() => {
                dispatch(changeCurrentPage(currentPage));
                handleClose();
            });
    };

    return (
        <CustomModal open={open} handleClose={handleClose} sx={{ p: 0 }}>
            <Box overflow="scroll" maxHeight="90vh" p={2}>
                <Formik
                    initialValues={initialValues}
                    validationSchema={confirmPaymentValidationSchema}
                    onSubmit={handleSubmit}
                >
                    {(formikProps: FormikProps<PaymentFormValue>) => {
                        const { values, handleChange, errors, touched } =
                            formikProps;

                        return (
                            <Form>
                                <Typography
                                    id="transition-modal-title"
                                    variant="h5"
                                >
                                    Confirm payment
                                </Typography>
                                <Stack
                                    spacing={2}
                                    sx={{ alignItems: 'center', mt: 3 }}
                                >
                                    <TextField
                                        label="Transaction id"
                                        name="operatorTransactionId"
                                        value={values.operatorTransactionId}
                                        onChange={handleChange}
                                        error={
                                            !!(
                                                errors.operatorTransactionId &&
                                                touched.operatorTransactionId
                                            )
                                        }
                                        helperText={
                                            (touched.operatorTransactionId &&
                                                errors.operatorTransactionId) ??
                                            undefined
                                        }
                                        size="small"
                                        fullWidth
                                    />
                                    <TextField
                                        label="Description"
                                        name="description"
                                        value={values.description}
                                        onChange={handleChange}
                                        error={
                                            !!(
                                                errors.description &&
                                                touched.description
                                            )
                                        }
                                        helperText={
                                            (touched.description &&
                                                errors.description) ??
                                            undefined
                                        }
                                        size="small"
                                        fullWidth
                                    />
                                    <SelectPaymentMethod
                                        customerId={customerId}
                                        formikProps={formikProps}
                                    />
                                    <Box display="flex" width="100%">
                                        <Card
                                            sx={{
                                                flexGrow: 3,
                                                overflow: 'hidden',
                                            }}
                                        >
                                            <LocalizationProvider
                                                dateAdapter={AdapterDateFns}
                                            >
                                                <StaticDateTimePicker
                                                    // displayStaticWrapperAs="desktop"
                                                    value={values.paymentTime}
                                                    onChange={(
                                                        value: Date | null
                                                    ) =>
                                                        formikProps.setFieldValue(
                                                            'paymentTime',
                                                            value
                                                        )
                                                    }
                                                    renderInput={(params) => (
                                                        <TextField
                                                            // eslint-disable-next-line react/jsx-props-no-spreading
                                                            {...params}
                                                        />
                                                    )}
                                                    // orientation="portrait"
                                                    // TODO: In UTC. We should use moment lib to get client local time
                                                    maxDate={new Date()}
                                                    ampm={false}
                                                />
                                                {touched.paymentTime &&
                                                errors.paymentTime ? (
                                                    <FormHelperText
                                                        sx={{ p: 2 }}
                                                        error={
                                                            !!(
                                                                errors.paymentTime &&
                                                                touched.paymentTime
                                                            )
                                                        }
                                                    >
                                                        {errors.paymentTime}
                                                    </FormHelperText>
                                                ) : undefined}
                                            </LocalizationProvider>
                                        </Card>
                                    </Box>
                                    <Button
                                        type="submit"
                                        sx={{ height: 40 }}
                                        fullWidth
                                        disabled={isLoading}
                                    >
                                        {isLoading ? (
                                            <LoadingIndicator />
                                        ) : (
                                            'Confirm'
                                        )}
                                    </Button>
                                </Stack>
                            </Form>
                        );
                    }}
                </Formik>
                {error && (
                    <FormHelperText sx={{ mt: 2 }} error>
                        {error}
                    </FormHelperText>
                )}
            </Box>
        </CustomModal>
    );
};

export default ConfirmPaymentModal;
