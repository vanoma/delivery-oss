import React, { ReactElement, useState, useEffect } from 'react';
import {
    ListItemText,
    Typography,
    Box,
    IconButton,
    Button,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import { localizePhoneNumber, removeSpaces } from '@vanoma/helpers';
import EditIcon from '@mui/icons-material/Edit';
import PaymentIcon from '@mui/icons-material/Payment';
import NewPaymentMethod from './NewPaymentMethod';
import { selectCustomerId } from '../redux/slices/authenticationSlice';
import Label from './Label';
import SelectedInfo from './SelectedInfo';
import ItemsSelector from './ItemsSelector';
import { useGetPaymentMethodsQuery } from '../api';
import { PaymentMethod } from '../types';
import CustomSnackBar from './CustomSnackBar';

const SelectPaymentMethod = ({
    onSelect,
    selectedPaymentMethod,
}: {
    selectedPaymentMethod: PaymentMethod | null;
    // eslint-disable-next-line no-unused-vars
    onSelect: (value: PaymentMethod | null) => void;
}): ReactElement => {
    const [filteredPaymentMethods, setFilteredPaymentMethods] = useState<
        PaymentMethod[]
    >([]);
    const [searchValue, setSearchValue] = useState('');
    const [openPaymentMethod, setOpenPaymentMethod] = React.useState(false);
    const { t } = useTranslation();
    const customerId = useSelector(selectCustomerId);
    const { data, error, refetch } = useGetPaymentMethodsQuery({
        customerId: customerId!,
    });

    const handlePaymentMethodOpen = (): void => setOpenPaymentMethod(true);
    const handlePaymentMethodClose = (): void => setOpenPaymentMethod(false);

    useEffect(() => {
        if (data) {
            setFilteredPaymentMethods(data.results);
        }
    }, [data]);

    const handleContactsSearch = (
        e: React.ChangeEvent<HTMLInputElement>
    ): void => {
        setSearchValue(e.target.value);
        if (data) {
            const { results } = data;
            const newFilteredPaymentMethods = results.filter((paymentMethod) =>
                localizePhoneNumber(paymentMethod.phoneNumber).startsWith(
                    removeSpaces(e.target.value)
                )
            );
            setFilteredPaymentMethods(newFilteredPaymentMethods);
        }
    };

    return (
        <>
            {selectedPaymentMethod ? (
                <SelectedInfo sx={{ justifyContent: 'space-between' }}>
                    <Typography>
                        {localizePhoneNumber(selectedPaymentMethod.phoneNumber)}
                    </Typography>
                    <IconButton onClick={() => onSelect(null)}>
                        <EditIcon />
                    </IconButton>
                </SelectedInfo>
            ) : (
                <>
                    <ItemsSelector<PaymentMethod>
                        data={filteredPaymentMethods}
                        keyExtractor={({ paymentMethodId }) => paymentMethodId}
                        renderItem={({ phoneNumber, isDefault }) => (
                            <>
                                <ListItemText
                                    primary={localizePhoneNumber(phoneNumber)}
                                />
                                {isDefault ? (
                                    <Label color="primary">
                                        {t('delivery.addressSelector.default')}
                                    </Label>
                                ) : (
                                    <Box width={52.4} />
                                )}
                            </>
                        )}
                        inputLabel={`${t(
                            'selectPaymentMethod.searchPaymentMethods'
                        )}...`}
                        inputValue={searchValue}
                        onInputChange={handleContactsSearch}
                        onItemClick={onSelect}
                        notFoundText={t(
                            'billing.selectPaymentMethod.paymentMethodNotFound'
                        )}
                        newItemButton={
                            <Button
                                variant="outlined"
                                size="small"
                                startIcon={<PaymentIcon />}
                                onClick={handlePaymentMethodOpen}
                                sx={{ mt: 1, mb: 2 }}
                            >
                                {t(
                                    'billing.selectPaymentMethod.newPaymentMethod'
                                )}
                            </Button>
                        }
                    />
                    <NewPaymentMethod
                        openNewPaymentMethod={openPaymentMethod}
                        handleNewPaymentMethodClose={handlePaymentMethodClose}
                        phoneNumberPreFill={searchValue}
                        onSelect={onSelect}
                    />
                    <CustomSnackBar
                        message={error as string}
                        severity="error"
                        onRetry={refetch}
                    />
                </>
            )}
        </>
    );
};

export default SelectPaymentMethod;
