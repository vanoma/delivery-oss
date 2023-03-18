import { PaymentMethodType } from '@vanoma/types';

export interface PaymentMethod {
    paymentMethodId: string;
    type: PaymentMethodType;
    isDefault: boolean;
    phoneNumber: string;
    shortCode?: string;
}
