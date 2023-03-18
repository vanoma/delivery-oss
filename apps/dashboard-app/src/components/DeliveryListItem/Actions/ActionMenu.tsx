import React, { ReactElement, useState, useRef } from 'react';
import {
    IconButton,
    Box,
    MenuItem,
    ListItemIcon,
    ListItemText,
} from '@mui/material';
import MoreHorizIcon from '@mui/icons-material/MoreHoriz';
import { CustomModal, MenuPopover } from '@vanoma/ui-components';

export interface ActionItem {
    icon: ReactElement;
    label: string;
    // eslint-disable-next-line no-unused-vars
    render: (closeModal: () => void) => ReactElement;
}

interface Props {
    actions: ActionItem[];
}

const Actions: React.FC<Props> = ({ actions }): ReactElement => {
    const [openMenu, setOpenMenu] = useState(false);
    const [selectedAction, setSelectedAction] = useState<ActionItem | null>(
        null
    );

    const anchorRef = useRef(null);

    const handleOpenMenu = (): void => setOpenMenu(true);
    const handleCloseMenu = (): void => setOpenMenu(false);
    const handleCloseModal = (): void => setSelectedAction(null);

    return (
        <>
            <Box
                sx={{
                    flexGrow: 1,
                    justifyContent: 'flex-end',
                    display: 'flex',
                    marginLeft: 'auto',
                }}
            >
                <IconButton
                    size="small"
                    sx={{ p: 0.5 }}
                    ref={anchorRef}
                    onClick={handleOpenMenu}
                >
                    <MoreHorizIcon />
                </IconButton>
                <MenuPopover
                    open={openMenu}
                    onClose={handleCloseMenu}
                    anchorRef={anchorRef}
                    sx={{ py: 1.25 }}
                >
                    {actions.map((action) => (
                        <MenuItem
                            key={action.label}
                            onClick={() => {
                                handleCloseMenu();
                                setSelectedAction(action);
                            }}
                            sx={{ py: 1, px: 2.5, height: 48 }}
                        >
                            <ListItemIcon>{action.icon}</ListItemIcon>
                            <ListItemText
                                primaryTypographyProps={{
                                    variant: 'body2',
                                }}
                            >
                                {action.label}
                            </ListItemText>
                        </MenuItem>
                    ))}
                </MenuPopover>
            </Box>
            {selectedAction !== null && (
                <CustomModal open handleClose={handleCloseModal}>
                    {selectedAction.render(handleCloseModal)}
                </CustomModal>
            )}
        </>
    );
};

export default Actions;
