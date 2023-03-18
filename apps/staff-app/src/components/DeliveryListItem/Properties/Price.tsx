import React, { ReactElement } from 'react';
import { Delivery } from '../../../types';
import InfoPair from '../../InfoPair';

interface Props {
    delivery: Delivery;
}

const PlacedAt: React.FC<Props> = ({ delivery }): ReactElement => {
    return (
        <InfoPair
            property="Price"
            value={`RWF ${Math.ceil(delivery.package.totalAmount)}`}
        />
    );
};

export default PlacedAt;
