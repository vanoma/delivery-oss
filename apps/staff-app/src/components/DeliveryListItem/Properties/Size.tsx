import React, { ReactElement } from 'react';
import { Delivery } from '../../../types';
import InfoPair from '../../InfoPair';

interface Props {
    delivery: Delivery;
}

const Size: React.FC<Props> = ({ delivery }): ReactElement => {
    return <InfoPair property="Size" value={delivery.package.size} />;
};

export default Size;
