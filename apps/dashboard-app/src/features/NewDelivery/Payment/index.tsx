/* eslint-disable no-nested-ternary */
import React, { useState } from 'react';
import { Box, Container, Typography } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import StepHead from '../StepHead';
import { useTypedDispatch } from '../../../helpers/reduxToolkit';
import {
    selectPricing,
    selectIsLoading,
    placeDeliveryOrder,
    resetState,
    selectDeliveryOrderId,
} from '../slice';
import StepContent from '../StepContent';
import ConfirmPlacement from '../ConfirmPlacement';
import { DELIVERIES, DELIVERIES_TAB } from '../../../routeNames';
import {
    selectAgentBranch,
    selectDefaultAddress,
    selectDefaultContact,
} from '../../../redux/slices/authenticationSlice';
import PriceInfo from './PriceInfo';
import PaymentInfo from './PaymentInfo';

interface PaymentProps {
    isEditing: boolean;
}

const Payment: React.FC<PaymentProps> = ({ isEditing }) => {
    const { t } = useTranslation();
    const dispatch = useTypedDispatch();
    const navigate = useNavigate();

    const [showOrderPlacement, setShowOrderPlacement] = useState(false);

    const pricing = useSelector(selectPricing);
    const isLoading = useSelector(selectIsLoading);
    const deliveryOrderId = useSelector(selectDeliveryOrderId);
    const defaultContact = useSelector(selectDefaultContact);
    const defaultAddress = useSelector(selectDefaultAddress);
    const agentBranch = useSelector(selectAgentBranch);

    const postDeliveryPlacement = (): void => {
        const deliveryOrderUrl = `${DELIVERIES}/${DELIVERIES_TAB.ACTIVE}#oid=${deliveryOrderId}`;
        dispatch(resetState({ defaultContact, defaultAddress, agentBranch }));
        navigate(deliveryOrderUrl);
    };

    return (
        <Container disableGutters>
            <StepHead
                title={t('delivery.payment.payment')}
                done={false}
                current={isEditing}
            />
            <StepContent in={isEditing}>
                <Box sx={{ px: 2 }}>
                    {pricing !== null ? (
                        <>
                            <PriceInfo pricing={pricing} />
                            <PaymentInfo
                                pricing={pricing}
                                isLoading={isLoading}
                                postDeliveryPlacement={postDeliveryPlacement}
                                setShowOrderPlacement={setShowOrderPlacement}
                            />
                        </>
                    ) : (
                        <Typography>
                            {!pricing &&
                                isLoading &&
                                t('delivery.payment.checkingThePrice')}
                            {!pricing &&
                                !isLoading &&
                                t('delivery.payment.cantCheckThePrice')}
                        </Typography>
                    )}
                </Box>
            </StepContent>
            <ConfirmPlacement
                open={showOrderPlacement}
                onCloseModal={() => setShowOrderPlacement(false)}
                onConfirmPlacement={() =>
                    dispatch(placeDeliveryOrder())
                        .unwrap()
                        .then(() => {
                            setShowOrderPlacement(false);
                            postDeliveryPlacement();
                        })
                }
            />
        </Container>
    );
};

export default Payment;
