import React from 'react';
import {
    Box,
    FormHelperText,
    MenuItem,
    Select,
    SelectChangeEvent,
    SxProps,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import { makeStyles } from '@mui/styles';
import { Theme } from '@mui/system';
import { selectBranches } from '../redux/slices/authenticationSlice';

const useStyles = makeStyles({
    paper: {
        borderRadius: 16,
        maxHeight: 140,
        overflow: 'scroll',
    },
});

const BranchSelector: React.FC<{
    value: string;
    // eslint-disable-next-line no-unused-vars
    onChange: (v: string) => void;
    touched?: boolean;
    error?: string;
    disabled?: boolean;
    allBranches?: boolean;
    sx?: SxProps<Theme>;
}> = ({ value, onChange, touched, error, disabled, allBranches, sx }) => {
    const { t } = useTranslation();
    const classes = useStyles();

    const branches = useSelector(selectBranches);

    return (
        <>
            {branches && branches.length > 0 && (
                <Box>
                    <Select
                        onChange={(e: SelectChangeEvent<string>) =>
                            onChange(e.target.value)
                        }
                        displayEmpty
                        size="small"
                        MenuProps={{
                            classes: {
                                paper: classes.paper,
                            },
                        }}
                        fullWidth
                        value={value}
                        error={!!(error && touched)}
                        disabled={disabled}
                        sx={sx}
                    >
                        {value === '' && (
                            <MenuItem value={value} disabled>
                                {t('account.newAgentModal.selectBranch')}
                            </MenuItem>
                        )}
                        {allBranches && (
                            <MenuItem value="all">
                                {t('billing.payBalance.allBranches')}
                            </MenuItem>
                        )}
                        {branches ? (
                            branches.map(({ branchName, branchId }) => (
                                <MenuItem value={branchId} key={branchId}>
                                    {branchName}
                                </MenuItem>
                            ))
                        ) : (
                            <MenuItem />
                        )}
                    </Select>
                    {touched && error && (
                        <FormHelperText error sx={{ mt: 0.5, mx: 1.75 }}>
                            {error}
                        </FormHelperText>
                    )}
                </Box>
            )}
        </>
    );
};

export default BranchSelector;
