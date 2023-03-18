/* eslint-disable no-nested-ternary */
import React, { memo, ReactElement, useEffect, useRef, useState } from 'react';
import { styled } from '@mui/material/styles';
import {
    ListItem,
    ListItemText,
    Box,
    Button,
    Paper,
    LinearProgress,
    Divider,
    alpha,
} from '@mui/material';
import LocationOnOutlinedIcon from '@mui/icons-material/LocationOnOutlined';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import {
    CustomList,
    CustomModal,
    LoadingIndicator,
    MapBase,
} from '@vanoma/ui-components';
import SmsOutlinedIcon from '@mui/icons-material/SmsOutlined';
import DoneIcon from '@mui/icons-material/Done';
import { Address, Contact, ReverseGeocode } from '@vanoma/types';
import Label from './Label';
import { useGetAddressesQuery, useReverseGeocodeMutation } from '../api';
import NewAddress, { SelectedLocation } from './NewAddress';
import { houseNumberAndStreetName } from '../helpers/address';
import vanomaMarker from '../../public/assets/vanoma-marker.png';
import '../locales/i18n';
import CustomSnackBar from './CustomSnackBar';
import { selectAddressId } from '../redux/slices/authenticationSlice';
import { parseGoogleResults } from '../helpers/google';
import {
    selectFromAddress,
    selectFromContact,
    selectNormalizedPickUpStart,
} from '../features/NewDelivery/slice';
import DeliveryLinkButton from './DeliveryLinkButton';

const DefaultPlaceHolder = styled('div')(() => ({
    width: 52.4,
}));

const ContactListItemText = styled(ListItemText)(() => ({
    width: '50%',
}));

const AddAddressButtonWrapper = styled(Paper)(({ theme }) => ({
    position: 'absolute',
    bottom: theme.spacing(3),
    right: theme.spacing(1.2),
}));

interface AddressSelectorProps {
    packageId?: string;
    contact: Contact;
    // eslint-disable-next-line no-unused-vars
    onAddressSelected: (value: Address) => void;
    resetDelivery?: () => void;
}

const AddressSelector = ({
    packageId,
    contact,
    onAddressSelected,
    resetDelivery,
}: AddressSelectorProps): ReactElement => {
    // eslint-disable-next-line no-undef
    const mapRef = useRef<google.maps.Map | null>(null);
    const { t } = useTranslation();

    const defaultAddressId = useSelector(selectAddressId);
    const fromContact = useSelector(selectFromContact);
    const fromAddress = useSelector(selectFromAddress);
    const pickUpStart = useSelector(selectNormalizedPickUpStart);

    const [openAddress, setOpenAddress] = useState<boolean>(false);
    const [location, setLocation] = useState<SelectedLocation | null>(null);
    const [reverseGeocodeResult, setReverseGeocodeResult] =
        useState<ReverseGeocode | null>(null);

    const [
        reverseGeocode,
        {
            isLoading: isReverseGeocodeLoading,
            error: reverseGeocodeError,
            reset: resetReverseGeocode,
        },
    ] = useReverseGeocodeMutation();

    const {
        data,
        error: addressesError,
        isLoading: isLoadingAddresses,
        refetch: refetchAddresses,
    } = useGetAddressesQuery({
        contactId: contact.contactId,
    });

    const selectLocation = (value: SelectedLocation): void => {
        setLocation(value);
        mapRef?.current?.panTo(value.position);
        mapRef?.current?.setZoom(18);

        if (!value.address) {
            const { suggestion } = value;
            const googleResults = suggestion
                ? parseGoogleResults(
                      suggestion.results!,
                      suggestion.description
                  )
                : null;

            if (googleResults) {
                setReverseGeocodeResult(googleResults);
            } else {
                const { position } = value;
                reverseGeocode({
                    latitude: position.lat,
                    longitude: position.lng,
                })
                    .unwrap()
                    .then(setReverseGeocodeResult);
            }
        }
    };

    const handleAddAddress = (): void => {
        if (location!.address) {
            onAddressSelected(location!.address);
        } else {
            setOpenAddress(true);
        }
    };

    const scrollToAddressSelector = (): void => {
        const element = document.getElementById('address-selector');
        setTimeout(() => {
            if (element) {
                element.scrollIntoView({
                    behavior: 'smooth',
                    block: 'end',
                });
            }
        }, 1000);
    };

    useEffect(scrollToAddressSelector, []);

    return (
        <>
            <Box sx={{ height: 400, mt: 2 }}>
                <MapBase
                    showSearchBox
                    googleMapsApiKey={process.env.GOOGLE_API_KEY!}
                    markers={
                        location !== null
                            ? [
                                  {
                                      icon: vanomaMarker,
                                      position: location.position,
                                  },
                              ]
                            : []
                    }
                    // eslint-disable-next-line no-undef
                    onLoadMap={(map: google.maps.Map) => {
                        mapRef.current = map;
                    }}
                    onUnmountMap={() => {
                        mapRef.current = null;
                    }}
                    // eslint-disable-next-line no-undef
                    onClickMap={(e: google.maps.MapMouseEvent) =>
                        selectLocation({
                            position: {
                                lat: e.latLng?.lat()!,
                                lng: e.latLng?.lng()!,
                            },
                        })
                    }
                    onSelectSuggestion={(suggestion) =>
                        selectLocation({
                            position: suggestion.coordinates,
                            suggestion,
                        })
                    }
                    clearAddressNameSuggestion={() => {}}
                >
                    {location ? (
                        <AddAddressButtonWrapper>
                            <Button
                                variant="outlined"
                                onClick={handleAddAddress}
                                startIcon={<DoneIcon />}
                            >
                                {isReverseGeocodeLoading ? (
                                    <LoadingIndicator />
                                ) : location.address ? (
                                    t('delivery.addressSelector.use')
                                ) : (
                                    t('delivery.addressSelector.save')
                                )}
                            </Button>
                        </AddAddressButtonWrapper>
                    ) : (
                        <></>
                    )}
                </MapBase>
            </Box>
            <CustomList sx={{ mt: -2.5, mb: 2 }} id="address-selector">
                {packageId !== undefined && (
                    <>
                        <Box
                            display="flex"
                            justifyContent="flex-end"
                            padding={3}
                        >
                            <DeliveryLinkButton
                                fromContactId={fromContact?.contactId!}
                                fromAddressId={fromAddress?.addressId!}
                                pickUpStart={pickUpStart}
                                buttonText={t('customers.contact.link')}
                                buttonVariant="outlined"
                                toContact={contact}
                                buttonFullWidth
                                startIcon={<SmsOutlinedIcon />}
                                resetDelivery={resetDelivery}
                            />
                        </Box>
                        {data && data.addresses.length !== 0 && <Divider />}
                    </>
                )}
                {isLoadingAddresses && <LinearProgress />}
                {data &&
                    data.addresses.map((address, indx) => (
                        <ListItem
                            button
                            divider={indx < data.totalCount - 1}
                            key={address.addressId}
                            onClick={() =>
                                selectLocation({
                                    position: {
                                        lat: address.latitude,
                                        lng: address.longitude,
                                    },
                                    address,
                                })
                            }
                            sx={(theme) => ({
                                backgroundColor:
                                    location?.address?.addressId ===
                                    address.addressId
                                        ? alpha(
                                              theme.palette.primary.light,
                                              0.05
                                          )
                                        : undefined,
                            })}
                        >
                            <LocationOnOutlinedIcon sx={{ mr: 2 }} />
                            <ContactListItemText
                                primary={address.addressName}
                                secondary={houseNumberAndStreetName(address)}
                            />
                            {address.addressId === defaultAddressId ? (
                                <Label color="primary">
                                    {t('delivery.addressSelector.default')}
                                </Label>
                            ) : (
                                <DefaultPlaceHolder />
                            )}
                            <Button
                                size="small"
                                variant="outlined"
                                sx={{ ml: 1 }}
                                color="inherit"
                            >
                                {t('delivery.addressSelector.preview')}
                            </Button>
                        </ListItem>
                    ))}
            </CustomList>
            <CustomModal
                open={openAddress}
                handleClose={() => setOpenAddress(false)}
            >
                <NewAddress
                    contactId={contact.contactId}
                    reverseGeocode={reverseGeocodeResult!}
                    selectedLocation={location!}
                    onAddressSelected={onAddressSelected}
                />
            </CustomModal>
            <CustomSnackBar
                message={addressesError as string}
                severity="error"
                onRetry={refetchAddresses}
            />
            <CustomSnackBar
                message={reverseGeocodeError as string}
                severity="error"
                onRetry={resetReverseGeocode}
            />
        </>
    );
};

export default memo(AddressSelector);
