import * as Yup from 'yup';
import { RequiredStringSchema } from 'yup/lib/string';

export const phoneNumberSchema = (
    name?: string
): RequiredStringSchema<string | undefined, Record<string, any>> =>
    Yup.string()
        .trim()
        .required(
            `Please enter ${name !== undefined ? `${name} ` : ''}phone number`
        )
        .matches(RegExp(`^[0-9]+$`), 'Phone number must be a number')
        .test('len', 'Phone number is invalid', (value) => {
            return value !== undefined
                ? (value.startsWith('07') && value.length === 10) ||
                      (value.startsWith('2507') && value.length === 12)
                : false;
        });

export const verificationCodeSchema = (): RequiredStringSchema<
    string | undefined,
    Record<string, any>
> =>
    Yup.string()
        .trim()
        .required('Please enter verification code')
        .matches(RegExp(`^[0-9]+$`), 'Verification code must be a number')
        .test('len', 'Verification code must be 6 numbers', (value) =>
            value !== undefined ? value.length === 6 : false
        );
