import React, { ReactElement } from 'react';
import { Delivery } from '../../../types';
import InfoPair from '../../InfoPair';

interface Props {
    delivery: Delivery;
}

const Payer: React.FC<Props> = ({ delivery }): ReactElement => {
    const { isCustomerPaying } = delivery.package.deliveryOrder;

    return (
        <InfoPair
            property="Payer"
            value={isCustomerPaying ? 'Business' : 'Client'}
        />
    );
};

export default Payer;
