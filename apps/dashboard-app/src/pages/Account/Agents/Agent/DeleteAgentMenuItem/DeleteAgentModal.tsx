import { Box, Button, FormHelperText, Typography } from '@mui/material';
import { CustomModal, LoadingIndicator } from '@vanoma/ui-components';
import React from 'react';
import { useTranslation } from 'react-i18next';
import { useDeleteAgentMutation } from '../../../../../api';

const DeleteAgentModal: React.FC<{
    open: boolean;
    handleClose: () => void;
    agentId: string;
}> = ({ open, handleClose, agentId }) => {
    const { t } = useTranslation();
    const [deleteAgent, { isLoading, error }] = useDeleteAgentMutation();

    const handleDeleteAgent = (): void => {
        deleteAgent(agentId).unwrap().then(handleClose);
    };

    return (
        <CustomModal open={open} handleClose={handleClose}>
            <Typography variant="h5">
                {t('account.deleteAgentModal.deleteThisAgent')}?
            </Typography>
            <Box
                sx={{
                    mt: 3,
                    display: 'flex',
                    justifyContent: 'space-between',
                }}
            >
                <Button variant="outlined" size="small" onClick={handleClose}>
                    {t('account.deleteAgentModal.no')}
                </Button>
                <Button
                    variant="outlined"
                    size="small"
                    onClick={handleDeleteAgent}
                >
                    {isLoading ? (
                        <LoadingIndicator />
                    ) : (
                        t('account.deleteAgentModal.yes')
                    )}
                </Button>
            </Box>
            {error && (
                <FormHelperText sx={{ mt: 2 }} error>
                    {error}
                </FormHelperText>
            )}
        </CustomModal>
    );
};

export default DeleteAgentModal;
