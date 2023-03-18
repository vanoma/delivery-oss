import { ListItemIcon, ListItemText, MenuItem } from '@mui/material';
import { Agent } from '@vanoma/types';
import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import DeleteIcon from '@mui/icons-material/Delete';
import DeleteAgentModal from './DeleteAgentModal';

const DeleteAgentMenuItem: React.FC<{
    agent: Agent;
    handleMenuClose: () => void;
}> = ({ agent, handleMenuClose }) => {
    const { t } = useTranslation();
    const [openDeleteAgent, setOpenDeleteAgent] = useState(false);

    const handleDeleteAgentOpen = (): void => {
        setOpenDeleteAgent(true);
    };
    const handleDeleteAgentClose = (): void => {
        setOpenDeleteAgent(false);
        handleMenuClose();
    };

    return (
        <>
            <MenuItem
                sx={{ py: 1, px: 2.5, height: 48 }}
                onClick={handleDeleteAgentOpen}
                disabled={agent.isRoot}
            >
                <ListItemIcon>
                    <DeleteIcon />
                </ListItemIcon>
                <ListItemText
                    primaryTypographyProps={{
                        variant: 'body2',
                    }}
                >
                    {t('account.agents.delete')}
                </ListItemText>
            </MenuItem>
            <DeleteAgentModal
                open={openDeleteAgent}
                handleClose={handleDeleteAgentClose}
                agentId={agent.agentId}
            />
        </>
    );
};

export default DeleteAgentMenuItem;
