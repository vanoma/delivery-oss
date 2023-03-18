import React, { ReactNode } from 'react';
import {
    Box,
    TextField,
    Theme,
    SxProps,
    Typography,
    ListItem,
    LinearProgress,
} from '@mui/material';
import { CustomList } from '@vanoma/ui-components';

interface Props<T> {
    data: T[];
    // eslint-disable-next-line no-unused-vars
    keyExtractor: (item: T) => string;
    // eslint-disable-next-line no-unused-vars
    renderItem: (item: T) => React.ReactNode;
    sx?: SxProps<Theme>;
    inputLabel: string;
    inputValue: string;
    // eslint-disable-next-line no-unused-vars
    onInputChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    // eslint-disable-next-line no-unused-vars
    onItemClick: (item: T) => void;
    notFoundText: string;
    newItemButton?: ReactNode;
    isLoading?: boolean;
}

const ItemsSelector = <T,>({
    data,
    keyExtractor,
    renderItem,
    sx,
    inputLabel,
    inputValue,
    onInputChange,
    onItemClick,
    notFoundText,
    newItemButton,
    isLoading,
}: Props<T>): JSX.Element => {
    return (
        <Box sx={sx}>
            <TextField
                label={inputLabel}
                value={inputValue}
                onChange={onInputChange}
                size="small"
                fullWidth
                sx={{ mb: 1 }}
            />
            {newItemButton}
            {isLoading && <LinearProgress />}
            <CustomList sx={{ m: 0 }}>
                {data.length !== 0 || isLoading ? (
                    data.map((item, index) => (
                        <ListItem
                            key={keyExtractor(item)}
                            button
                            divider={index < data.length - 1}
                            onClick={() => onItemClick(item)}
                        >
                            {renderItem(item)}
                        </ListItem>
                    ))
                ) : (
                    <Typography align="center" sx={{ p: 3 }}>
                        {notFoundText}
                    </Typography>
                )}
            </CustomList>
        </Box>
    );
};

export default ItemsSelector;
