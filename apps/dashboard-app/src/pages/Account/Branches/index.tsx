import React from 'react';
import { Box, Typography } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import { selectBranches } from '../../../redux/slices/authenticationSlice';
import BranchView from './Branch';
import AddBranchButton from './AddBranchButton';

const Branches: React.FC = () => {
    const { t } = useTranslation();

    const branches = useSelector(selectBranches);

    return (
        <>
            <Box
                display="flex"
                justifyContent="space-between"
                alignItems="center"
            >
                <Typography variant="h5">
                    {t('account.branches.branches')}
                </Typography>
                <AddBranchButton />
            </Box>
            <Box>
                {branches.length > 0 &&
                    branches.map((branch) => (
                        <BranchView branch={branch} key={branch.branchId} />
                    ))}
            </Box>
            {branches.length === 0 && (
                <Typography sx={{ pb: 4, pt: 2 }} align="center">
                    {t('account.branches.branchesNotFound')}
                </Typography>
            )}
        </>
    );
};

export default Branches;
