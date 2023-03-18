import React, { useState } from 'react';
import { useSelector } from 'react-redux';
import { Address, Contact, Package, PackageStatus } from '@vanoma/types';
import { useGetPackagesQuery } from '../api';
import DeliveryListContainer, {
    PAGE_SIZE,
} from '../components/DeliveryListContainer';
import { selectCustomerId } from '../redux/slices/authenticationSlice';
import { DELIVERIES_TAB } from '../routeNames';
import DeliveryListItem from '../components/DeliveryListItem';

const CompleteDeliveries: React.FC<{
    selectedBranchId: string | undefined;
}> = ({ selectedBranchId }) => {
    const [currentPage, setCurrentPage] = useState(1);

    const customerId = useSelector(selectCustomerId);
    const { data, error, isFetching, refetch } = useGetPackagesQuery(
        {
            customerId: customerId!,
            status: PackageStatus.COMPLETE,
            page: currentPage - 1,
            size: PAGE_SIZE,
            branchId: selectedBranchId,
        },
        { refetchOnMountOrArgChange: true }
    );

    return (
        <DeliveryListContainer<Package<Contact, Address>>
            isFetching={isFetching}
            data={data}
            error={error}
            refetch={refetch}
            currentPage={currentPage}
            setCurrentPage={setCurrentPage}
            tab={DELIVERIES_TAB.COMPLETE}
            renderItem={(pkg) => (
                <DeliveryListItem
                    tab={DELIVERIES_TAB.COMPLETE}
                    delivery={pkg}
                    progress={0}
                    isFetching={isFetching}
                />
            )}
        />
    );
};

export default CompleteDeliveries;
