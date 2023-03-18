import React, { ReactElement, useState, useEffect } from 'react';
import {
    Alert,
    AlertColor,
    IconButton,
    Slide,
    SlideProps,
    Snackbar,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import RefreshIcon from '@mui/icons-material/Refresh';

const CustomSnackBar = ({
    message,
    severity,
    onRetry,
    onReset,
}: {
    message: string | null;
    severity: AlertColor;
    onRetry?: () => void;
    onReset?: () => void;
}): ReactElement => {
    const [open, setOpen] = useState<boolean>(false);

    const onClose = (): void => {
        setOpen(false);
        if (onReset) {
            onReset();
        }
    };

    useEffect(() => {
        if (message !== null && message !== undefined) {
            setOpen(true);
        }
    }, [message]);

    return (
        <Snackbar
            open={open}
            autoHideDuration={6000}
            onClose={onClose}
            anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
            TransitionComponent={(props: Omit<SlideProps, 'direction'>) => (
                <Slide {...props} direction="down" />
            )}
        >
            <Alert
                severity={severity}
                variant="filled"
                sx={{ alignItems: 'center' }}
                action={
                    <>
                        {onRetry && (
                            <IconButton onClick={onRetry}>
                                <RefreshIcon />
                            </IconButton>
                        )}
                        <IconButton onClick={onClose}>
                            <CloseIcon />
                        </IconButton>
                    </>
                }
            >
                {message}
            </Alert>
        </Snackbar>
    );
};

export default CustomSnackBar;
