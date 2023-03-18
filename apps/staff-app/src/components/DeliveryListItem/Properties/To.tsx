import { formatContactForPrivateView } from '@vanoma/helpers';
import React, { ReactElement } from 'react';
import { Delivery } from '../../../types';
import InfoPair from '../../InfoPair';

interface Props {
    delivery: Delivery;
    isValueBold?: boolean;
}

const To: React.FC<Props> = ({ delivery, isValueBold }): ReactElement => {
    return (
        <InfoPair
            property="To"
            value={formatContactForPrivateView(delivery.package.toContact)}
            isValueBold={isValueBold}
        />
    );
};

export default To;
