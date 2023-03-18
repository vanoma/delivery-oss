import React, { useState } from 'react';
import { Box, Button, TextField, Typography } from '@mui/material';
import { LoadingIndicator } from '@vanoma/ui-components';
import { useTranslation } from 'react-i18next';
import AddCircleOutlineOutlinedIcon from '@mui/icons-material/AddCircleOutlineOutlined';
import { PackageSize } from '@vanoma/types';
import Size from '../../Size';

const WhoIsPaying: React.FC<{
    loading: number;
    generateDeliveryLink: (
        // eslint-disable-next-line no-unused-vars
        isCustomerPaying: boolean,
        // eslint-disable-next-line no-unused-vars
        fromNote: string,
        // eslint-disable-next-line no-unused-vars
        size: PackageSize,
        // eslint-disable-next-line no-unused-vars
        phoneNumber?: string
    ) => void;
    phoneNumber?: string;
}> = ({ loading, generateDeliveryLink, phoneNumber }) => {
    const { t } = useTranslation();
    const [note, setNote] = useState('');
    const [showNote, setShowNote] = useState(false);
    const [size, setSize] = useState<PackageSize | null>(null);

    return (
        <>
            {showNote ? (
                <TextField
                    label={t(
                        'customers.linkGeneratorModal.pickupInstructionsOptional'
                    )}
                    name="name"
                    value={note}
                    onChange={(e) => setNote(e.target.value)}
                    size="small"
                    fullWidth
                />
            ) : (
                <Box display="flex" justifyContent="end">
                    <Button
                        variant="outlined"
                        size="small"
                        onClick={() => setShowNote(true)}
                        startIcon={<AddCircleOutlineOutlinedIcon />}
                        color="inherit"
                    >
                        {t('customers.linkGeneratorModal.more')}
                    </Button>
                </Box>
            )}
            <Size
                size={size}
                changePackageSize={(value) => setSize(value)}
                isEditing
            />
            {size && (
                <>
                    <Typography variant="h5" py={1} align="center">
                        {t('customers.linkGeneratorModal.whoIsPaying')}
                    </Typography>
                    <Box
                        sx={{
                            mt: 3,
                            display: 'flex',
                            justifyContent: 'space-between',
                        }}
                    >
                        <Button
                            variant="outlined"
                            size="small"
                            onClick={() =>
                                generateDeliveryLink(
                                    true,
                                    note,
                                    size,
                                    phoneNumber
                                )
                            }
                            disabled={loading !== 0}
                        >
                            {loading === 1 ? (
                                <LoadingIndicator />
                            ) : (
                                t('customers.linkGeneratorModal.me')
                            )}
                        </Button>
                        <Button
                            variant="outlined"
                            size="small"
                            onClick={() =>
                                generateDeliveryLink(
                                    false,
                                    note,
                                    size,
                                    phoneNumber
                                )
                            }
                            disabled={loading !== 0}
                        >
                            {loading === 2 ? (
                                <LoadingIndicator />
                            ) : (
                                t('customers.linkGeneratorModal.customer')
                            )}
                        </Button>
                    </Box>
                </>
            )}
        </>
    );
};

export default WhoIsPaying;
