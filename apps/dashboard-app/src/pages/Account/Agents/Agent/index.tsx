import React, { useRef, useState } from 'react';
import {
    Card,
    IconButton,
    ListItem,
    ListItemText,
    Typography,
} from '@mui/material';
import MoreHorizIcon from '@mui/icons-material/MoreHoriz';
import { MenuPopover } from '@vanoma/ui-components';
import { Agent } from '@vanoma/types';
import { localizePhoneNumber } from '@vanoma/helpers';
import { useTranslation } from 'react-i18next';
import EditAgentMenuItem from './EditAgentMenuItem';
import DeleteAgentMenuItem from './DeleteAgentMenuItem';
import Label from '../../../../components/Label';

const AgentView: React.FC<{
    agent: Agent;
}> = ({ agent }) => {
    const { t } = useTranslation();
    const [openMenu, setOpenMenu] = useState(false);
    const anchorRef = useRef(null);

    const handleMenuOpen = (): void => {
        setOpenMenu(true);
    };
    const handleMenuClose = (): void => {
        setOpenMenu(false);
    };
    return (
        <Card sx={{ mb: 2 }}>
            <ListItem>
                <ListItemText
                    primary={agent.fullName}
                    secondary={
                        <>
                            {localizePhoneNumber(agent.phoneNumber)}
                            {agent.branch && (
                                <Typography variant="body2">
                                    {agent.branch.branchName}
                                </Typography>
                            )}
                        </>
                    }
                />
                {agent.isRoot && (
                    <Label color="primary">{t('account.agents.main')}</Label>
                )}
                <IconButton
                    size="small"
                    sx={{ p: 0.5, ml: 1 }}
                    ref={anchorRef}
                    onClick={handleMenuOpen}
                >
                    <MoreHorizIcon />
                </IconButton>
            </ListItem>
            <MenuPopover
                open={openMenu}
                onClose={handleMenuClose}
                anchorRef={anchorRef}
                sx={{ py: 1.25 }}
            >
                <EditAgentMenuItem
                    agent={agent}
                    handleMenuClose={handleMenuClose}
                />
                <DeleteAgentMenuItem
                    agent={agent}
                    handleMenuClose={handleMenuClose}
                />
            </MenuPopover>
        </Card>
    );
};

export default AgentView;
