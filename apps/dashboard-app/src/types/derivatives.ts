/**
 *  Types derived from {@link ./data.ts} types.
 */

import { Contact, Address, Package, DeliveryOrder } from '@vanoma/types';
import { Nullable } from '../helpers/types';

export type NewPackage = Partial<Pick<DeliveryOrder, 'deliveryOrderId'>> &
    Nullable<Pick<Package, 'packageId' | 'size'>> &
    Pick<
        Package<Contact | null, Address | null>,
        | 'pickUpStart'
        | 'fromContact'
        | 'toContact'
        | 'fromAddress'
        | 'toAddress'
        | 'fromNote'
        | 'toNote'
    >;

export type DeliveryLinkPackage = Pick<
    Package<
        Partial<Pick<Contact, 'contactId' | 'phoneNumberOne'>>,
        Pick<Address, 'addressId'>
    >,
    | 'size'
    | 'pickUpStart'
    | 'fromContact'
    | 'fromAddress'
    | 'toContact'
    | 'fromNote'
>;
