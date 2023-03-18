import React, { useState } from 'react';
import { useSelector } from 'react-redux';
import { Address, Contact, Package, PackageStatus } from '@vanoma/types';
import { useGetPackagesQuery } from '../../api';
import { selectCustomerId } from '../../redux/slices/authenticationSlice';
import DeliveryListContainer, {
    PAGE_SIZE,
} from '../../components/DeliveryListContainer';
import DeliveryListItem from './DeliveryListItem';
import { DELIVERIES_TAB } from '../../routeNames';

const RequestDeliveries: React.FC<{ selectedBranchId: string | undefined }> = ({
    selectedBranchId,
}) => {
    const [currentPage, setCurrentPage] = useState(1);

    const customerId = useSelector(selectCustomerId);
    const { data, error, isFetching, refetch } = useGetPackagesQuery(
        {
            customerId: customerId!,
            // TEMPORARY FETCHING PENDING ORDERS TOO BECAUSE WE NUKED PENDING TAB
            status: [PackageStatus.REQUEST, PackageStatus.PENDING],
            page: currentPage - 1,
            size: PAGE_SIZE,
            branchId: selectedBranchId,
        },
        {
            pollingInterval: 60000,
            refetchOnMountOrArgChange: true,
        }
    );

    return (
        <DeliveryListContainer<Package<Contact, Address>>
            isFetching={isFetching}
            data={data}
            error={error}
            refetch={refetch}
            currentPage={currentPage}
            setCurrentPage={setCurrentPage}
            tab={DELIVERIES_TAB.REQUEST}
            renderItem={(pkg, progress) => (
                <DeliveryListItem
                    progress={progress}
                    isFetching={isFetching}
                    pkg={pkg}
                    setCurrentPage={setCurrentPage}
                />
            )}
        />
    );
};

export default RequestDeliveries;
