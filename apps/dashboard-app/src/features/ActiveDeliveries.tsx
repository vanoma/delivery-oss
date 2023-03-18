import React, { useState } from 'react';
import DoNotDisturbIcon from '@mui/icons-material/DoNotDisturb';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import { Address, Contact, Package, PackageStatus } from '@vanoma/types';
import { useGetPackagesQuery } from '../api';
import { selectCustomerId } from '../redux/slices/authenticationSlice';
import DeliveryListContainer, {
    PAGE_SIZE,
} from '../components/DeliveryListContainer';
import { DELIVERIES_TAB } from '../routeNames';
import DeliveryListItem, { Actions } from '../components/DeliveryListItem';

const ActiveDeliveries: React.FC<{ selectedBranchId: string | undefined }> = ({
    selectedBranchId,
}) => {
    const { t } = useTranslation();
    const [currentPage, setCurrentPage] = useState(1);

    const customerId = useSelector(selectCustomerId);
    const { data, error, isFetching, refetch } = useGetPackagesQuery(
        {
            customerId: customerId!,
            status: PackageStatus.PLACED,
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
            tab={DELIVERIES_TAB.ACTIVE}
            renderItem={(pkg, progress) => (
                <DeliveryListItem
                    tab={DELIVERIES_TAB.ACTIVE}
                    delivery={pkg}
                    progress={progress}
                    isFetching={isFetching}
                    actions={[
                        {
                            label: t('deliveries.order.cancel'),
                            icon: <DoNotDisturbIcon />,
                            render: (closeModal) => (
                                <Actions.CancelDelivery
                                    delivery={pkg}
                                    setCurrentPage={setCurrentPage}
                                    closeActionModal={closeModal}
                                />
                            ),
                        },
                    ]}
                />
            )}
        />
    );
};

export default ActiveDeliveries;
