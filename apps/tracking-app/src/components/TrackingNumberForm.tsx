import React, { useEffect, useState } from 'react';
import { Stack, TextField } from '@mui/material';
import { Button } from 'gatsby-theme-material-ui';
import { Form, Formik, FormikProps } from 'formik';
import { LoadingIndicator } from '@vanoma/ui-components';
import { useTranslation } from 'react-i18next';

const TrackingNumberForm: React.FC<{
    trackingNumber: string | null | undefined;
    // eslint-disable-next-line no-unused-vars
    handleTrackingNumberChange: (value: string) => void;
    isLoading: boolean;
    marginBottom: number;
}> = ({
    trackingNumber,
    handleTrackingNumberChange,
    isLoading,
    marginBottom,
}) => {
    // Holding TextField initial value to avoid label issue
    // of not moving up and get out of the value in the
    // field caused by undefined value of trackingNumber
    const [initialValue, setInitialValue] = useState('');

    const { t } = useTranslation();

    useEffect(() => {
        if (trackingNumber) {
            setInitialValue(trackingNumber);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return (
        <Formik
            initialValues={{
                trackingNumber: initialValue,
            }}
            onSubmit={(values: { trackingNumber: string }) =>
                handleTrackingNumberChange(values.trackingNumber)
            }
            enableReinitialize
        >
            {({
                values,
                handleChange,
            }: FormikProps<{ trackingNumber: string }>) => (
                <Form>
                    <Stack
                        direction="row"
                        justifyContent="center"
                        spacing={2}
                        mb={marginBottom}
                    >
                        <TextField
                            label={t('trackingNumber')}
                            name="trackingNumber"
                            value={values.trackingNumber}
                            onChange={handleChange}
                            size="small"
                            sx={{ flexGrow: { xs: 1, sm: 0 } }}
                            disabled={isLoading}
                        />
                        <Button
                            type="submit"
                            size="small"
                            sx={{ px: 2 }}
                            disabled={isLoading}
                        >
                            {isLoading ? <LoadingIndicator /> : t('track')}
                        </Button>
                    </Stack>
                </Form>
            )}
        </Formik>
    );
};

export default TrackingNumberForm;
