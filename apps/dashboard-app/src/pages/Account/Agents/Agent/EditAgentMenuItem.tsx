import React, { useState } from 'react';
import { ListItemIcon, ListItemText, MenuItem } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import { useTranslation } from 'react-i18next';
import { Agent } from '@vanoma/types';
import AgentFormModal from './AgentFormModal';

const EditAgentMenuItem: React.FC<{
    agent: Agent;
    handleMenuClose: () => void;
}> = ({ agent, handleMenuClose }) => {
    const { t } = useTranslation();
    const [openModal, setOpenModal] = useState(false);

    const handleOpenModal = (): void => {
        setOpenModal(true);
    };
    const handleClose = (): void => {
        setOpenModal(false);
        handleMenuClose();
    };

    return (
        <>
            <MenuItem
                sx={{ py: 1, px: 2.5, height: 48 }}
                onClick={handleOpenModal}
            >
                <ListItemIcon>
                    <EditIcon />
                </ListItemIcon>
                <ListItemText
                    primaryTypographyProps={{
                        variant: 'body2',
                    }}
                >
                    {t('account.agents.edit')}
                </ListItemText>
            </MenuItem>
            <AgentFormModal
                handleClose={handleClose}
                agent={agent}
                open={openModal}
            />
        </>
    );
};

export default EditAgentMenuItem;
