import React, { ReactElement } from 'react';
import moment from 'moment';
import { Delivery } from '../../../types';
import InfoPair from '../../InfoPair';

interface Props {
    delivery: Delivery;
}

const PlacedAt: React.FC<Props> = ({ delivery }): ReactElement => {
    const { placedAt } = delivery.package.deliveryOrder;

    return (
        <InfoPair
            property="Placed at"
            value={
                placedAt !== null
                    ? moment(placedAt).format('ddd, MMM Do YYYY, h:mm A')
                    : 'N/A'
            }
        />
    );
};

export default PlacedAt;
