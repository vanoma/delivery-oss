import React, { ReactElement, FC } from 'react';
import DeliveryListContainer from '../components/DeliveryListContainer';
import DeliveryListItem, { Properties } from '../components/DeliveryListItem';

interface Props {
    expandAll: boolean;
}

const RequestDeliveries: FC<Props> = ({ expandAll }): ReactElement => {
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
                        <div id={delivery.package.packageId} />
                        <Properties.From delivery={delivery} />
                        <Properties.To delivery={delivery} isValueBold />
                        <Properties.Size delivery={delivery} />
                        <Properties.Payer delivery={delivery} />
                        <Properties.Comment delivery={delivery} />
                        <Properties.Agent delivery={delivery} />
                        <Properties.Branch delivery={delivery} />
                        <Properties.DeliveryLink delivery={delivery} />
                    </>
                </DeliveryListItem>
            )}
        />
    );
};

export default RequestDeliveries;
