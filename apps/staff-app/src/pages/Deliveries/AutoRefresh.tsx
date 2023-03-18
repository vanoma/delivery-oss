import React from 'react';
import { MenuItem, Select } from '@mui/material';
import { makeStyles } from '@mui/styles';

const useStyles = makeStyles({
    paper: {
        borderRadius: 16,
    },
});

const AutoRefresh: React.FC<{
    refreshInterval: number;
    // eslint-disable-next-line no-unused-vars
    setRefreshInterval: (v: number) => void;
}> = ({ refreshInterval, setRefreshInterval }) => {
    const classes = useStyles();

    return (
        <Select
            value={refreshInterval}
            onChange={(e) => {
                const { value } = e.target;
                if (value !== null) {
                    setRefreshInterval(Number.parseInt(value.toString(), 10));
                }
            }}
            displayEmpty
            size="small"
            MenuProps={{
                classes: { paper: classes.paper },
            }}
        >
            <MenuItem value={0}>Never</MenuItem>
            <MenuItem value={1}>1 min</MenuItem>
            <MenuItem value={2}>2 min</MenuItem>
            <MenuItem value={5}>5 min</MenuItem>
        </Select>
    );
};

export default AutoRefresh;
