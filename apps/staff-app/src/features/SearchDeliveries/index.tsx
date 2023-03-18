import React, { FC, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { prefixNumberWithCountryCode } from '@vanoma/helpers';
import { PackageStatus } from '@vanoma/types';
import {
    searchDeliveries,
    QueryType,
} from '../../redux/slices/deliveriesSlice';
import DeliveryListItem, {
    Properties,
} from '../../components/DeliveryListItem';
import DeliveryListContainer from '../../components/DeliveryListContainer';
import SearchBar from './SearchBar';
import { useTypedDispatch } from '../../redux/typedHooks';

const parseStatus = (value: string | null): PackageStatus | null => {
    if (value) {
        const status = value.toLocaleUpperCase() as PackageStatus;
        if (Object.values(PackageStatus).includes(status)) {
            return status;
        }
    }
    return null;
};

interface Props {
    expandAll: boolean;
}

const SearchDeliveries: FC<Props> = ({ expandAll }) => {
    const [searchParams] = useSearchParams();
    const [packageStatus, setPackageStatus] = useState<PackageStatus>(
        PackageStatus.COMPLETE
    );
    const [queryType, setQueryType] = useState<QueryType | null>(
        QueryType.PHONE_NUMBER
    );
    const [queryValue, setQueryValue] = useState<string>('');

    const dispatch = useTypedDispatch();
    const tn = searchParams.get('tn');
    const tel = searchParams.get('tel');
    const status = parseStatus(searchParams.get('status'));

    useEffect(() => {
        if (tn || tel || status) {
            dispatch(
                searchDeliveries({
                    packageStatus,
                    // Prioritizing tracking number over phone number
                    queryType: tn
                        ? QueryType.TRACKING_NUMBER
                        : QueryType.PHONE_NUMBER,
                    queryValue:
                        tn ?? (tel ? prefixNumberWithCountryCode(tel!) : null),
                })
            );
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [tn, status, tel]);

    useEffect(() => {
        setQueryValue(tn ?? (tel ? prefixNumberWithCountryCode(tel!) : ''));
        if (tn || tel) {
            setQueryType(
                tn ? QueryType.TRACKING_NUMBER : QueryType.PHONE_NUMBER
            );
            setPackageStatus(status ?? ('ALL' as PackageStatus));
        } else if (status) {
            setPackageStatus(status);
            setQueryType(null);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return (
        <>
            <SearchBar
                packageStatus={packageStatus}
                queryType={queryType}
                queryValue={queryValue}
                setPackageStatus={setPackageStatus}
                setQueryType={setQueryType}
                setQueryValue={setQueryValue}
            />
            <DeliveryListContainer
                renderItem={(delivery) => (
                    <DeliveryListItem
                        delivery={delivery}
                        expandAll={expandAll}
                        key={delivery.package.packageId}
                    >
                        <>
                            <Properties.From delivery={delivery} />
                            <Properties.TrackingLink delivery={delivery} />
                            <Properties.Price delivery={delivery} />
                            <Properties.CreatedAt delivery={delivery} />
                            <Properties.PlacedBy delivery={delivery} />
                            <Properties.Branch delivery={delivery} />
                            <Properties.Comment delivery={delivery} />
                        </>
                    </DeliveryListItem>
                )}
            />
        </>
    );
};

export default SearchDeliveries;
