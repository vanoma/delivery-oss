/* eslint-disable no-shadow */
import React, { useState } from 'react';
import {
    Box,
    IconButton,
    InputAdornment,
    TextField,
    ToggleButton,
    ToggleButtonGroup,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { onlyNumbers } from '../../helpers/string';

// eslint-disable-next-line no-unused-vars
export enum QueryType {
    // eslint-disable-next-line no-unused-vars
    PHONE_NUMBER = 'phoneNumber',
    // eslint-disable-next-line no-unused-vars
    BUSINESS_NAME = 'businessName',
}

const SearchBar: React.FC<{
    queryType: QueryType;
    setQueryType: React.Dispatch<React.SetStateAction<QueryType>>;
    setQueryValue: React.Dispatch<React.SetStateAction<string>>;
}> = ({ queryType, setQueryType, setQueryValue }) => {
    const [value, setValue] = useState('');

    const handleSearch = (): void => setQueryValue(value);

    const resetValue = (): void => {
        setQueryValue('');
        setValue('');
    };

    return (
        <Box sx={{ display: { xs: 'block', sm: 'flex' }, mb: 3 }}>
            <TextField
                autoFocus
                fullWidth
                size="small"
                placeholder={
                    queryType === QueryType.BUSINESS_NAME
                        ? 'E.g: John'
                        : 'E.g: 07XXXXXXXX'
                }
                InputProps={{
                    startAdornment: (
                        <InputAdornment position="start">
                            <ToggleButtonGroup
                                color="primary"
                                value={queryType}
                                exclusive
                                size="small"
                                onChange={(e, value) => {
                                    setQueryType(value);
                                    resetValue();
                                }}
                            >
                                <ToggleButton value={QueryType.BUSINESS_NAME}>
                                    NAME
                                </ToggleButton>
                                <ToggleButton value={QueryType.PHONE_NUMBER}>
                                    TEL
                                </ToggleButton>
                            </ToggleButtonGroup>
                        </InputAdornment>
                    ),
                    sx: { p: 0 },
                }}
                sx={{
                    mr: 2,
                    mb: { xs: 2, sm: 0 },
                    fontWeight: 'fontWeightBold',
                }}
                name="query"
                value={value}
                onChange={(e) =>
                    setValue(
                        queryType === QueryType.BUSINESS_NAME
                            ? e.target.value
                            : onlyNumbers(e.target.value)
                    )
                }
                onKeyPress={(e) => {
                    if (e.key === 'Enter') {
                        handleSearch();
                    }
                }}
            />
            <IconButton onClick={handleSearch}>
                <SearchIcon />
            </IconButton>
        </Box>
    );
};

export default SearchBar;
