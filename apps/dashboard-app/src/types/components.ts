/**
 * Shared components types.
 */

import * as Yup from 'yup';

export interface AuthStep<T> {
    label: string;
    body: { title: string; inputLabel: string; buttonText: string };
    validationSchema: Yup.SchemaOf<unknown>;
    initialValues: T;
    inputName: keyof T;
    handleSubmit: (
        // eslint-disable-next-line no-unused-vars
        values: T,
        // eslint-disable-next-line no-unused-vars
        callback: () => void
    ) => void;
}

export interface EventNotification {
    packageEventId: string;
    deliveryOrderId: string;
    packageId: string;
    eventName: string;
    createdAt: string;
    text: {
        en: string;
        fr: string;
        rw: string;
    };
    isRead: boolean;
    clickCount: number;
}
