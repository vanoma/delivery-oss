import React, { ReactElement } from 'react';
import moment from 'moment';
import { Delivery } from '../../../types';
import InfoPair from '../../InfoPair';

interface Props {
    delivery: Delivery;
}

const CreatedAt: React.FC<Props> = ({ delivery }): ReactElement => {
    const { createdAt } = delivery.package;

    return (
        <InfoPair
            property="Created at"
            value={moment(createdAt).format('ddd, MMM Do YYYY, h:mm A')}
        />
    );
};

export default CreatedAt;
