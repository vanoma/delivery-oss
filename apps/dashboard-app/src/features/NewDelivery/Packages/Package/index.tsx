import React from 'react';
import { Box, Button, Paper } from '@mui/material';
import { Address, Contact, Package } from '@vanoma/types';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import Stop from '../../Stop';
import Size from '../../../../components/Size';
import {
    deletePackage,
    selectIsLoading,
    updateSize,
    updateToNote,
} from '../../slice';
import { useTypedDispatch } from '../../../../helpers/reduxToolkit';
import Instructions from '../../Instructions';

const PackageView: React.FC<{
    pkg: Package<Contact | null, Address | null>;
    isEditing: boolean;
    isLast: boolean;
    canDelete: boolean;
    resetDelivery: () => void;
}> = ({ pkg, isEditing, isLast, canDelete, resetDelivery }) => {
    const { t } = useTranslation();
    const dispatch = useTypedDispatch();
    const { toContact, toAddress, size, toNote } = pkg;

    const isLoading = useSelector(selectIsLoading);

    return (
        <Paper
            sx={{
                borderRadius: 0.5,
                overflow: 'hidden',
                mb: isLast ? undefined : 1.25,
            }}
        >
            <Stop
                packageId={pkg.packageId}
                contact={toContact}
                address={toAddress}
                isEditing={isEditing}
                resetDelivery={resetDelivery}
            />
            {toAddress && (
                <>
                    <Size
                        size={size}
                        changePackageSize={(value) =>
                            dispatch(
                                updateSize({
                                    size: value,
                                    packageId: pkg.packageId,
                                })
                            )
                        }
                        isEditing={isEditing}
                    />
                    <Instructions
                        isEditing={isEditing}
                        note={toNote}
                        label={t(
                            'delivery.package.dropOffInstructionsOptional'
                        )}
                        placeholder={t('delivery.to.egRingTheDoorbell')}
                        onChange={(value) =>
                            dispatch(
                                updateToNote({
                                    toNote: value,
                                    packageId: pkg.packageId,
                                })
                            )
                        }
                        property={t('delivery.package.dropOffInstructions')}
                        pt={0}
                        pb={2}
                        packageId={pkg.packageId}
                    />
                </>
            )}
            {isEditing && canDelete && (
                <Box display="flex" justifyContent="flex-end" p={2} pt={0}>
                    <Button
                        variant="outlined"
                        color="inherit"
                        size="small"
                        disabled={isLoading}
                        onClick={() => dispatch(deletePackage(pkg.packageId))}
                    >
                        {t('delivery.stop.remove')}
                    </Button>
                </Box>
            )}
        </Paper>
    );
};

export default PackageView;
