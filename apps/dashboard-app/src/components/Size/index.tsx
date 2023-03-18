import React from 'react';
import {
    Box,
    ToggleButton,
    ToggleButtonGroup,
    Typography,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import { PackageSize } from '@vanoma/types';
import Small from './Small';
import Medium from './Medium';
import Large from './Large';

const Size: React.FC<{
    size: PackageSize | null;
    // eslint-disable-next-line no-unused-vars
    changePackageSize: (value: PackageSize) => void;
    isEditing: boolean;
}> = ({ size, changePackageSize, isEditing }) => {
    const { t } = useTranslation();

    return isEditing ? (
        <Box m={2} minHeight={328}>
            <Box mb={2}>
                <Typography variant="body2" mb={1}>
                    {t('delivery.package.packageSize')}
                </Typography>
                <ToggleButtonGroup
                    color="primary"
                    value={size}
                    exclusive
                    size="small"
                    onChange={(e, value) =>
                        value ? changePackageSize(value) : {}
                    }
                >
                    <ToggleButton value={PackageSize.SMALL}>
                        {t(
                            `delivery.package.${PackageSize.SMALL.toLowerCase()}`
                        )}
                    </ToggleButton>
                    <ToggleButton value={PackageSize.MEDIUM}>
                        {t(
                            `delivery.package.${PackageSize.MEDIUM.toLowerCase()}`
                        )}
                    </ToggleButton>
                    <ToggleButton value={PackageSize.LARGE}>
                        {t(
                            `delivery.package.${PackageSize.LARGE.toLowerCase()}`
                        )}
                    </ToggleButton>
                </ToggleButtonGroup>
            </Box>
            {size === PackageSize.SMALL && <Small />}
            {size === PackageSize.MEDIUM && <Medium />}
            {size === PackageSize.LARGE && <Large />}
            {size === null && (
                <Typography
                    minHeight={220}
                    display="flex"
                    justifyContent="center"
                    alignItems="center"
                >
                    {t('delivery.package.selectPackageSize')}
                </Typography>
            )}
        </Box>
    ) : (
        <Box display="flex" gap={1} mt={2} px={2}>
            <Typography color="text.secondary">
                {t('delivery.package.packageSize')}:
            </Typography>
            <Typography>
                {t(`delivery.package.${size!.toLowerCase()}`)}
            </Typography>
        </Box>
    );
};

export default Size;
