import React from 'react';
import { Autocomplete, Box, Paper, Typography } from '@mui/material';
import { CustomTextField } from '@vanoma/ui-components';
import { useSelector } from 'react-redux';
import { selectLastUsedNotes } from './slice';

const Instructions: React.FC<{
    note: string | null;
    isEditing: boolean;
    label: string;
    placeholder: string;
    // eslint-disable-next-line no-unused-vars
    onChange: (v: string) => void;
    property: string;
    pt: number;
    pb: number;
    packageId?: string;
}> = ({
    note,
    isEditing,
    label,
    placeholder,
    onChange,
    property,
    pt,
    pb,
    packageId,
}) => {
    const lastUsedNote = useSelector(selectLastUsedNotes)[packageId ?? ''];

    return (
        <Box px={2} pt={pt} pb={pb}>
            {isEditing ? (
                <>
                    <Typography variant="body2">{label}</Typography>
                    <Autocomplete
                        freeSolo
                        options={(lastUsedNote ? [lastUsedNote] : []).map(
                            (option) => option
                        )}
                        size="small"
                        value={note}
                        onChange={(e, value) => onChange(value!)}
                        onInputChange={(e, value) => onChange(value)}
                        renderInput={(params) => (
                            <CustomTextField
                                {...params}
                                variant="outlined"
                                label={placeholder}
                                sx={{ mt: 1 }}
                            />
                        )}
                        PaperComponent={(props) => (
                            <Paper {...props} elevation={4} />
                        )}
                    />
                </>
            ) : (
                note && (
                    <Box display="flex" gap={1} mt={1}>
                        <Typography color="text.secondary">
                            {property}:
                        </Typography>
                        <Typography>{note}</Typography>
                    </Box>
                )
            )}
        </Box>
    );
};

export default Instructions;
