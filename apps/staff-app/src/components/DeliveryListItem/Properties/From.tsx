import { formatContactForPrivateView } from '@vanoma/helpers';
import React, { ReactElement } from 'react';
import { Delivery } from '../../../types';
import InfoPair from '../../InfoPair';

interface Props {
    delivery: Delivery;
}

const Payer: React.FC<Props> = ({ delivery }): ReactElement => {
    return (
        <InfoPair
            property="From"
            value={formatContactForPrivateView(delivery.package.fromContact)}
        />
    );
};

export default Payer;
