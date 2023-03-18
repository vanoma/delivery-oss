import { ClientType } from '@vanoma/types';
import React, { ReactElement, FC } from 'react';
import DeliveryListContainer from '../components/DeliveryListContainer';
import DeliveryListItem, { Properties } from '../components/DeliveryListItem';

interface Props {
    expandAll: boolean;
}

const ActiveDeliveries: FC<Props> = ({ expandAll }): ReactElement => {
    return (
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
                        <Properties.Size delivery={delivery} />
                        <Properties.Price delivery={delivery} />
                        <Properties.PlacedAt delivery={delivery} />
                        <Properties.PlacedBy delivery={delivery} />
                        <Properties.Branch delivery={delivery} />
                        <Properties.Agent delivery={delivery} />
                        {delivery.package.deliveryOrder.clientType ===
                            ClientType.DELIVERY_LINK && (
                            <Properties.CreatedAt delivery={delivery} />
                        )}
                        <Properties.Comment delivery={delivery} />
                    </>
                </DeliveryListItem>
            )}
        />
    );
};

export default ActiveDeliveries;
