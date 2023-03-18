import React, { ReactElement, FC } from 'react';
import DeliveryListContainer from '../components/DeliveryListContainer';
import DeliveryListItem, { Properties } from '../components/DeliveryListItem';

interface Props {
    expandAll: boolean;
}

const PendingDeliveries: FC<Props> = ({ expandAll }): ReactElement => {
    return (
        <DeliveryListContainer
            renderItem={(delivery) => (
                <DeliveryListItem
                    delivery={delivery}
                    expandAll={expandAll}
                    key={delivery.package.packageId}
                    isRequest
                >
                    <>
                        <Properties.From delivery={delivery} />
                        <Properties.To delivery={delivery} />
                        <Properties.PlacedBy delivery={delivery} />
                        <Properties.Agent delivery={delivery} />
                        <Properties.Branch delivery={delivery} />
                        <Properties.Comment delivery={delivery} />
                    </>
                </DeliveryListItem>
            )}
        />
    );
};

export default PendingDeliveries;
