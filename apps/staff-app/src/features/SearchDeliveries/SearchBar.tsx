/* eslint-disable no-shadow */
import React from 'react';
import {
    InputAdornment,
    ToggleButtonGroup,
    ToggleButton,
    TextField,
    Box,
    Select,
    MenuItem,
    IconButton,
} from '@mui/material';
import { makeStyles } from '@mui/styles';
import SearchIcon from '@mui/icons-material/Search';
import { prefixNumberWithCountryCode } from '@vanoma/helpers';
import { PackageStatus } from '@vanoma/types';
import { useTypedDispatch } from '../../redux/typedHooks';
import {
    searchDeliveries,
    QueryType,
    SearchParams,
} from '../../redux/slices/deliveriesSlice';
import { onlyNumbers } from '../../helpers/string';

export interface SearchValues {
    query: string;
    queryType: string;
    status: string;
    searched: boolean;
}

const useStyles = makeStyles({
    paper: {
        borderRadius: 16,
    },
});

const SearchBar: React.FC<{
    packageStatus: PackageStatus | null;
    queryType: QueryType | null;
    queryValue: string | null;
    setPackageStatus: React.Dispatch<React.SetStateAction<PackageStatus>>;
    setQueryType: React.Dispatch<React.SetStateAction<QueryType | null>>;
    setQueryValue: React.Dispatch<React.SetStateAction<string>>;
}> = ({
    packageStatus,
    queryType,
    queryValue,
    setPackageStatus,
    setQueryType,
    setQueryValue,
}) => {
    const classes = useStyles();
    const dispatch = useTypedDispatch();

    const handleSearch = ({
        packageStatus,
        queryType,
        queryValue,
    }: SearchParams): void => {
        dispatch(
            searchDeliveries({
                packageStatus:
                    (packageStatus as string) !== 'ALL' ? packageStatus : null,
                queryType,
                queryValue,
            })
        );
    };

    return (
        <Box sx={{ display: { xs: 'block', sm: 'flex' }, mb: 3 }}>
            <Select
                value={packageStatus}
                onChange={(e) =>
                    setPackageStatus(e.target.value as PackageStatus)
                }
                displayEmpty
                size="small"
                MenuProps={{
                    classes: { paper: classes.paper },
                }}
            >
                {['ALL', ...Object.keys(PackageStatus)].map((status) => (
                    <MenuItem value={status} key={status}>
                        {status}
                    </MenuItem>
                ))}
            </Select>
            <TextField
                autoFocus
                fullWidth
                size="small"
                placeholder={
                    queryType === QueryType.PHONE_NUMBER
                        ? 'E.g: 07XXXXXXXX'
                        : 'E.g: XXXXXXXXXXXXX'
                }
                InputProps={{
                    startAdornment: (
                        <InputAdornment position="start">
                            <ToggleButtonGroup
                                color="primary"
                                value={queryType}
                                exclusive
                                size="small"
                                onChange={(e, value) => setQueryType(value)}
                            >
                                <ToggleButton value={QueryType.PHONE_NUMBER}>
                                    TEL
                                </ToggleButton>
                                <ToggleButton value={QueryType.TRACKING_NUMBER}>
                                    TN
                                </ToggleButton>
                            </ToggleButtonGroup>
                        </InputAdornment>
                    ),
                    sx: { p: 0 },
                }}
                sx={{
                    mx: 2,
                    mb: { xs: 2, sm: 0 },
                    fontWeight: 'fontWeightBold',
                }}
                name="query"
                value={queryValue}
                onChange={(e) => {
                    const value = onlyNumbers(e.target.value);
                    setQueryValue(value);
                }}
                onKeyPress={(e) => {
                    if (e.key === 'Enter') {
                        handleSearch({
                            packageStatus,
                            queryType,
                            queryValue:
                                queryType === QueryType.PHONE_NUMBER &&
                                queryValue
                                    ? prefixNumberWithCountryCode(queryValue)
                                    : queryValue,
                        });
                    }
                }}
            />
            <IconButton
                onClick={() => {
                    handleSearch({
                        packageStatus,
                        queryType,
                        queryValue:
                            queryType === QueryType.PHONE_NUMBER && queryValue
                                ? prefixNumberWithCountryCode(queryValue)
                                : queryValue,
                    });
                }}
            >
                <SearchIcon />
            </IconButton>
        </Box>
    );
};

export default SearchBar;
