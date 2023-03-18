import { TFunction } from 'i18next';
import * as Yup from 'yup';
import { RequiredStringSchema } from 'yup/lib/string';
import '../locales/i18n';

export const verificationCodeSchema = (
    t: TFunction
): RequiredStringSchema<string | undefined, Record<string, any>> =>
    Yup.string()
        .trim()
        .required(t('alertAndValidationMessages.verificationCodeRequired'))
        .matches(
            RegExp(`^[0-9]+$`),
            t('alertAndValidationMessages.verificationCodeMustBeNumber')
        )
        .test(
            'len',
            t('alertAndValidationMessages.verificationCodeValid'),
            (value) => (value !== undefined ? value.length === 6 : false)
        );

export const phoneNumberSchema = (
    t: TFunction,
    name?: string,
    validate: boolean = false
): RequiredStringSchema<string | undefined, Record<string, any>> =>
    Yup.string()
        .trim()
        .required(
            t('alertAndValidationMessages.phoneNumberRequired', {
                name: name !== undefined ? `${name} ` : '',
            })
        )
        .matches(
            RegExp(`^[0-9]+$`),
            t('alertAndValidationMessages.phoneNumberMustBeNumber')
        )
        .test(
            'len',
            t('alertAndValidationMessages.phoneNumberValid'),
            (value) => {
                if (validate) {
                    const iNs = ['8', '9', '2', '3'];
                    return value !== undefined
                        ? (value.startsWith('07') &&
                              iNs.includes(value[2]) &&
                              value.length === 10) ||
                              (value.startsWith('2507') &&
                                  iNs.includes(value[4]) &&
                                  value.length === 12)
                        : false;
                }
                return value !== undefined
                    ? (value.startsWith('07') && value.length === 10) ||
                          (value.startsWith('2507') && value.length === 12)
                    : false;
            }
        );
