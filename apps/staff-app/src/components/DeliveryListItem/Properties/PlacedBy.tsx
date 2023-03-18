import { localizePhoneNumber } from '@vanoma/helpers';
import React, { ReactElement } from 'react';
import { Delivery } from '../../../types';
import InfoPair from '../../InfoPair';

interface Props {
    delivery: Delivery;
}

const Payer: React.FC<Props> = ({ delivery }): ReactElement => {
    const { customer } = delivery.package.deliveryOrder;
    return (
        <InfoPair
            property="Placed by"
            value={`${customer.businessName} (${localizePhoneNumber(
                customer.phoneNumber
            )})`}
        />
    );
};

export default Payer;
