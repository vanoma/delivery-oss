import React, { ReactElement, useState, useEffect } from 'react';
import {
    Theme,
    IconButton,
    Stack,
    TextField,
    useMediaQuery,
    LinearProgress,
    InputAdornment,
} from '@mui/material';
import { CustomSnackBar } from '@vanoma/ui-components';
import EditIcon from '@mui/icons-material/Edit';
import CancelIcon from '@mui/icons-material/Cancel';
import { useUpdatePackageMutation } from '../../../api';
import { Delivery } from '../../../types';
import InfoPair from '../../InfoPair';

interface Props {
    delivery: Delivery;
}

const Comment: React.FC<Props> = ({ delivery }): ReactElement => {
    const [note, setNote] = useState('');
    const [isEditing, setIsEditing] = useState(false);

    const isSmall = useMediaQuery((theme: Theme) =>
        theme.breakpoints.down('sm')
    );

    const [updatePackage, { isLoading, error }] = useUpdatePackageMutation();

    useEffect(() => {
        if (delivery.package.staffNote) {
            setNote(delivery.package.staffNote);
        }
    }, [delivery.package.staffNote]);

    return (
        <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} mt={0}>
            {isEditing ? (
                <>
                    <TextField
                        label="Comment"
                        name="note"
                        multiline
                        maxRows={4}
                        value={note}
                        onChange={(e) => {
                            if (e.target.value.length < 100) {
                                setNote(e.target.value);
                            }
                        }}
                        size="small"
                        disabled={isLoading}
                        fullWidth={isSmall}
                        sx={{ width: { sm: 320 } }}
                        onKeyUp={(e) => {
                            const trimmedValue = note.trim();
                            if (e.key === 'Enter' && trimmedValue) {
                                updatePackage({
                                    packageId: delivery.package.packageId,
                                    staffNote: trimmedValue,
                                })
                                    .unwrap()
                                    .then(() => setIsEditing(false));
                            }
                        }}
                        InputProps={{
                            endAdornment: (
                                <InputAdornment position="start">
                                    <IconButton
                                        onClick={() => setIsEditing(false)}
                                        edge="end"
                                        sx={{ p: 0.7 }}
                                    >
                                        <CancelIcon />
                                    </IconButton>
                                </InputAdornment>
                            ),
                        }}
                    />
                    {isLoading && <LinearProgress />}
                </>
            ) : (
                <Stack spacing={1} direction="row" alignItems="center">
                    <InfoPair property="Comment" value={note ?? 'N/A'} />
                    <IconButton
                        sx={{ p: 0.5 }}
                        onClick={() => setIsEditing(true)}
                    >
                        <EditIcon fontSize="small" />
                    </IconButton>
                </Stack>
            )}
            <CustomSnackBar message={error as string} severity="error" />
        </Stack>
    );
};

export default Comment;
