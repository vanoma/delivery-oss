import { Package, Assignment } from '@vanoma/types';

export interface Delivery {
    package: Package;
    assignment: Assignment | null;
}

export type StatusColor =
    | 'primary'
    | 'secondary'
    | 'info'
    | 'success'
    | 'warning'
    | 'error';

export interface EventNotification extends Notification {
    data: {
        deliveryOrderId: string;
        packageId: string;
        createdAt: string;
        isRead: boolean;
        clickCount: number;
    };
    content: string;
}
