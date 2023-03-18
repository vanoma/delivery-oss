import { localizePhoneNumber } from '@vanoma/helpers';
import React, { ReactElement } from 'react';
import { Delivery } from '../../../types';
import InfoPair from '../../InfoPair';

interface Props {
    delivery: Delivery;
}

const Agent: React.FC<Props> = ({ delivery }): ReactElement => {
    const { agent, customer } = delivery.package.deliveryOrder;

    return agent && agent.phoneNumber !== customer.phoneNumber ? (
        <InfoPair
            property="Agent"
            value={`${agent.fullName} (${localizePhoneNumber(
                agent.phoneNumber
            )})`}
        />
    ) : (
        <></>
    );
};

export default Agent;
