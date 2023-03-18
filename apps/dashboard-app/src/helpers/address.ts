import { Address } from '@vanoma/types';

// eslint-disable-next-line import/prefer-default-export
export const houseNumberAndStreetName = (address: Address): string => {
    return `${
        `${address.houseNumber ?? ''} ${address.streetName ?? ''}` ??
        address.district
    }`;
};
