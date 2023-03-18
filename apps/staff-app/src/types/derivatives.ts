/**
 *  Types derived from {@link ./data.ts} types.
 */

import { MobileMoney } from '@vanoma/types';

export type NewMobileMoney = Pick<MobileMoney, 'phoneNumber'> &
    Partial<Omit<MobileMoney, 'phoneNumber'>>;
