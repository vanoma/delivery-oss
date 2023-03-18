import {
    Button,
    FormHelperText,
    Stack,
    TextField,
    Typography,
} from '@mui/material';
import { Formik, Form, FormikProps } from 'formik';
import React from 'react';
import { useTranslation } from 'react-i18next';
import * as Yup from 'yup';
import { LoadingIndicator } from '@vanoma/ui-components';
import { Package } from '@vanoma/types';
import { useCancelPackageMutation } from '../../../api';

interface Props {
    delivery: Package;
    setCurrentPage: React.Dispatch<React.SetStateAction<number>>;
    closeActionModal: () => void;
}

const Action: React.FC<Props> = ({
    delivery,
    setCurrentPage,
    closeActionModal,
}) => {
    const { t } = useTranslation();
    const [cancelPackage, { isLoading, error }] = useCancelPackageMutation();

    return (
        <>
            <Formik
                initialValues={{
                    reason: '',
                }}
                validationSchema={Yup.object().shape({
                    reason: Yup.string().required(
                        t(
                            'alertAndValidationMessages.reasonCancelledDeliveryRequired'
                        )
                    ),
                })}
                onSubmit={(values) => {
                    cancelPackage({
                        note: values.reason,
                        packageId: delivery.packageId,
                    })
                        .unwrap()
                        .then(() => {
                            closeActionModal();
                            setCurrentPage(1);
                        });
                }}
            >
                {({
                    values,
                    handleChange,
                    errors,
                    touched,
                }: FormikProps<{ reason: string }>) => (
                    <Form>
                        <Typography id="transition-modal-title" variant="h5">
                            {t('deliveries.cancelDeliveryModal.cancelDelivery')}
                        </Typography>
                        <Stack spacing={2} sx={{ alignItems: 'center', mt: 3 }}>
                            <TextField
                                label={t(
                                    'deliveries.cancelDeliveryModal.reason'
                                )}
                                name="reason"
                                value={values.reason}
                                onChange={handleChange}
                                error={!!(errors.reason && touched.reason)}
                                helperText={
                                    (touched.reason && errors.reason) ??
                                    undefined
                                }
                                size="small"
                                fullWidth
                            />
                            <Button
                                type="submit"
                                sx={{ height: 40 }}
                                fullWidth
                                disabled={isLoading}
                            >
                                {isLoading ? (
                                    <LoadingIndicator />
                                ) : (
                                    t('deliveries.cancelDeliveryModal.cancel')
                                )}
                            </Button>
                        </Stack>
                    </Form>
                )}
            </Formik>
            {error && (
                <FormHelperText sx={{ mt: 2 }} error>
                    {error}
                </FormHelperText>
            )}
        </>
    );
};

export default Action;
