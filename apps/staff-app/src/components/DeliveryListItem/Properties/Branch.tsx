import React, { ReactElement } from 'react';
import { Delivery } from '../../../types';
import InfoPair from '../../InfoPair';

interface Props {
    delivery: Delivery;
}

const Branch: React.FC<Props> = ({ delivery }): ReactElement => {
    const { branch } = delivery.package.deliveryOrder;

    return branch ? (
        <InfoPair property="Branch" value={branch.branchName} />
    ) : (
        <></>
    );
};

export default Branch;
