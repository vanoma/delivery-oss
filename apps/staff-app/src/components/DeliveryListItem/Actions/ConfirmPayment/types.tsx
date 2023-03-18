export interface PaymentFormValue {
    paymentMethodId: string;
    operatorTransactionId: string;
    description: string;
    paymentTime: Date | undefined;
}
