import { Skeleton, ToggleButton, ToggleButtonGroup } from '@mui/material';
import React, { ReactElement } from 'react';
import { useTranslation } from 'react-i18next';

const BillTimeToggle = ({
    range,
    setRange,
    setRangeDate,
    isLoadingAndNoBill,
}: {
    range: 'all' | 'endingAt';
    setRange: React.Dispatch<React.SetStateAction<'all' | 'endingAt'>>;
    setRangeDate: React.Dispatch<React.SetStateAction<Date | undefined>>;
    isLoadingAndNoBill: boolean;
}): ReactElement => {
    const { t } = useTranslation();

    return isLoadingAndNoBill ? (
        <Skeleton
            width={186.11}
            height={38.28}
            variant="rectangular"
            animation="wave"
            sx={{ borderRadius: 19 }}
        />
    ) : (
        <ToggleButtonGroup
            color="primary"
            value={range}
            exclusive
            onChange={(e, value) => {
                setRange(value);
                setRangeDate(undefined);
            }}
            size="small"
        >
            <ToggleButton value="all">
                {t('billing.payBalance.allUnpaid')}
            </ToggleButton>
            <ToggleButton value="endingAt">
                {t('billing.payBalance.onlyUntil')}
            </ToggleButton>
        </ToggleButtonGroup>
    );
};

export default BillTimeToggle;
