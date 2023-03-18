import { PaymentStatus, PackageStatus } from '@vanoma/types';
import { StatusColor } from '../types';

// eslint-disable-next-line no-unused-vars
export const PAYMENT_STATUS_COLOR: { [key in PaymentStatus]: StatusColor } = {
    PAID: 'success',
    UNPAID: 'warning',
    PARTIAL: 'warning',
    NO_CHARGE: 'info',
};

// eslint-disable-next-line no-unused-vars
export const PACKAGE_STATUS_COLOR: { [key in PackageStatus]: StatusColor } = {
    COMPLETE: 'success',
    PLACED: 'warning',
    STARTED: 'warning',
    REQUEST: 'info',
    PENDING: 'warning',
    CANCELED: 'error',
    INCOMPLETE: 'error',
};
