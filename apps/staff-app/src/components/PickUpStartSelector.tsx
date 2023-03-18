import React from 'react';
import {
    Select,
    MenuItem,
    Box,
    ListItemText,
    ListItemIcon,
    SelectChangeEvent,
    LinearProgress,
} from '@mui/material';
import { makeStyles } from '@mui/styles';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import { CustomSnackBar } from '@vanoma/ui-components';
import { getTimeIntervals } from '@vanoma/helpers';
import { useGetBusinessHoursQuery } from '../api';

const useStyles = makeStyles({
    paper: {
        borderRadius: 16,
        maxHeight: 140,
        overflow: 'scroll',
    },
});

interface Props {
    selected: string;
    // eslint-disable-next-line no-unused-vars
    onSelect: (value: string) => void;
}

const PickUpStartSelector: React.FC<Props> = ({ selected, onSelect }) => {
    const classes = useStyles();
    const { data, isFetching, error } = useGetBusinessHoursQuery();

    return (
        <>
            <Select
                onChange={(e: SelectChangeEvent<string>) => {
                    onSelect(e.target.value);
                }}
                displayEmpty
                size="small"
                MenuProps={{
                    classes: {
                        paper: classes.paper,
                    },
                }}
                fullWidth
                value={selected}
            >
                <MenuItem value="">
                    <ListItemText>Select Time</ListItemText>
                </MenuItem>
                {data &&
                    data.length !== 0 &&
                    getTimeIntervals(data).map(({ label, value }) => (
                        <MenuItem value={value} key={value}>
                            <Box
                                sx={{
                                    display: 'flex',
                                    alignItems: 'center',
                                }}
                            >
                                <ListItemIcon sx={{ minWidth: 36 }}>
                                    <AccessTimeIcon />
                                </ListItemIcon>
                                <ListItemText>{label}</ListItemText>
                            </Box>
                        </MenuItem>
                    ))}
            </Select>
            {isFetching && <LinearProgress />}
            <CustomSnackBar message={error as string} severity="error" />
        </>
    );
};

export default PickUpStartSelector;
