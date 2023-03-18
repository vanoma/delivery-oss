import React, { ReactElement, useRef, useState } from 'react';
import {
    Modal,
    Backdrop,
    Fade,
    Card,
    Box,
    Stack,
    Typography,
    Alert,
} from '@mui/material';
import { styled } from '@mui/material/styles';
import { useTranslation } from 'react-i18next';
import { MapBase } from '@vanoma/ui-components';
import { useSelector } from 'react-redux';
import { ReverseGeocode } from '@vanoma/types';
import { parseGoogleResults } from '../../helpers/google';
import NewAddress, { SelectedLocation } from '../../components/NewAddress';
import vanomaMarker from '../../../public/assets/vanoma-marker.png';
import { useReverseGeocodeMutation } from '../../api';
import {
    getDefaultData,
    selectContactId,
} from '../../redux/slices/authenticationSlice';
import { useTypedDispatch } from '../../helpers/reduxToolkit';

const PopUp = styled(Card)(({ theme }) => ({
    position: 'absolute' as const,
    top: '50%',
    left: '50%',
    transform: 'translate(-50%, -50%)',
    width: '80%',
    maxWidth: theme.spacing(80),
    borderRadius: theme.spacing(2),
    outline: 0,
    [theme.breakpoints.down('sm')]: {
        width: theme.spacing(40),
    },
}));

const AccountInfo = (): ReactElement => {
    const [location, setLocation] = useState<SelectedLocation | null>(null);
    // eslint-disable-next-line no-undef
    const mapRef = useRef<google.maps.Map | null>(null);
    const [reverseGeocodeResult, setReverseGeocodeResult] =
        useState<ReverseGeocode | null>(null);
    // const [addressNameSuggestion, setAddressNameSuggestion] = useState<
    //     string | null
    // >(null);
    const [mapTypeId, setMapTypeId] = useState<string | undefined>();
    const { t } = useTranslation();
    const dispatch = useTypedDispatch();

    const [
        reverseGeocode,
        { error: reverseGeocodeError, reset: resetReverseGeocode },
    ] = useReverseGeocodeMutation();

    const defaultContactId = useSelector(selectContactId);

    const selectLocation = (value: SelectedLocation): void => {
        setLocation(value);
        mapRef?.current?.panTo(value.position);

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

    return (
        <Modal
            aria-labelledby="transition-modal-title"
            aria-describedby="transition-modal-description"
            open
            closeAfterTransition
            BackdropComponent={Backdrop}
            BackdropProps={{
                timeout: 500,
            }}
            sx={{ overflow: 'scroll' }}
        >
            <Fade in>
                <PopUp>
                    <Typography variant="h5" p={{ xs: 1, md: 2 }}>
                        {t(
                            'overview.defaultContactModal.selectBusinessLocation'
                        )}
                    </Typography>
                    <Box sx={{ height: { xs: 320, md: 400 } }}>
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
                                setMapTypeId(mapRef.current?.getMapTypeId());
                            }}
                            onUnmountMap={() => {
                                mapRef.current = null;
                                setMapTypeId(undefined);
                            }}
                            // eslint-disable-next-line no-undef
                            onClickMap={(e: google.maps.MapMouseEvent) => {
                                selectLocation({
                                    position: {
                                        lat: e.latLng?.lat()!,
                                        lng: e.latLng?.lng()!,
                                    },
                                });
                                reverseGeocode({
                                    latitude: e.latLng?.lat()!,
                                    longitude: e.latLng?.lng()!,
                                })
                                    .unwrap()
                                    .then(setReverseGeocodeResult);
                            }}
                            onSelectSuggestion={(suggestion) =>
                                selectLocation({
                                    position: suggestion.coordinates,
                                    suggestion,
                                })
                            }
                            clearAddressNameSuggestion={
                                () => {}
                                // setAddressNameSuggestion(null)
                            }
                            mapTypeId={mapTypeId}
                            onMapTypeIdChanged={() => {
                                setMapTypeId(mapRef.current?.getMapTypeId());
                            }}
                        />
                    </Box>
                    {reverseGeocodeResult && (
                        <Stack spacing={2} p={{ xs: 1, md: 2 }}>
                            <NewAddress
                                contactId={defaultContactId!}
                                reverseGeocode={reverseGeocodeResult}
                                selectedLocation={location!}
                                onAddressSelected={() => {
                                    dispatch(getDefaultData());
                                }}
                                isDefault
                            />
                        </Stack>
                    )}
                    {reverseGeocodeError && (
                        <Alert
                            severity="error"
                            onClose={resetReverseGeocode}
                            sx={{ mt: 2 }}
                        >
                            {reverseGeocodeError}
                        </Alert>
                    )}
                </PopUp>
            </Fade>
        </Modal>
    );
};

export default AccountInfo;
