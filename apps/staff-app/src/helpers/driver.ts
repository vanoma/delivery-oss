import { Driver } from '@vanoma/types';
import { StatusColor } from '../types';

// eslint-disable-next-line import/prefer-default-export
export const getDriverStatusColor = (driver: Driver): StatusColor => {
    if (!driver.isAvailable) {
        return 'error';
    }

    if (driver.assignmentCount > 0) {
        return 'warning';
    }

    return 'success';
};
