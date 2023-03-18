import {
    Card,
    Container,
    Pagination,
    Skeleton,
    Typography,
} from '@mui/material';
import { prefixNumberWithCountryCode } from '@vanoma/helpers';
import { CustomSnackBar } from '@vanoma/ui-components';
import React, { useState } from 'react';
import { useGetCustomersQuery } from '../../api';
import Customer from './Customer';
import SearchBar, { QueryType } from './SearchBar';

const PAGE_SIZE = 10;

const Customers: React.FC = () => {
    const [page, setPage] = useState(1);
    const [queryType, setQueryType] = useState(QueryType.BUSINESS_NAME);
    const [queryValue, setQueryValue] = useState('');

    const { data, isFetching, error, refetch } = useGetCustomersQuery({
        page: page - 1,
        size: PAGE_SIZE,
        sort: 'createdAt,desc',
        businessName:
            queryType === QueryType.BUSINESS_NAME ? queryValue : undefined,
        phoneNumber:
            queryType === QueryType.PHONE_NUMBER && queryValue
                ? prefixNumberWithCountryCode(queryValue)
                : undefined,
    });

    return (
        <Container>
            <Typography sx={{ mt: 1.5, mb: 3 }} variant="h4">
                Customers
            </Typography>
            <SearchBar
                queryType={queryType}
                setQueryType={setQueryType}
                setQueryValue={setQueryValue}
            />
            {isFetching &&
                [...new Array(10)].map((e, index) => (
                    // eslint-disable-next-line react/no-array-index-key
                    <Card sx={{ mb: 2 }} key={index}>
                        <Skeleton
                            variant="rectangular"
                            animation="wave"
                            height={186}
                            sx={{ borderRadius: 0.5 }}
                        />
                    </Card>
                ))}
            {!isFetching &&
                data &&
                data.count > 0 &&
                data.results.map((customer) => (
                    <Customer customer={customer} key={customer.customerId} />
                ))}
            {data && data.count > 0 && (
                <Pagination
                    count={Math.ceil(data.count / PAGE_SIZE)}
                    page={page}
                    variant="outlined"
                    color="primary"
                    onChange={(e, value) => setPage(value)}
                    disabled={isFetching}
                />
            )}
            <CustomSnackBar
                message={error as string}
                severity="error"
                onRetry={refetch}
            />
        </Container>
    );
};

export default Customers;
