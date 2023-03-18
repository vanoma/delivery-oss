/* eslint-disable camelcase */
import { ReverseGeocode } from '@vanoma/types';

const MAPS_ROUTE = 'route';
const MAPS_STREET_NUMBER = 'street_number';
const MAPS_UNNAMED_ROAD = 'Unnamed Road';

const getHouseNameFromAddressComponents = (
    // eslint-disable-next-line no-undef
    addressComponents: google.maps.GeocoderAddressComponent[]
): string | null => {
    const index = addressComponents.findIndex((component) =>
        component.types.includes(MAPS_STREET_NUMBER)
    );
    if (index !== -1) return addressComponents[index].short_name;
    return null;
};

const getShortStreetNameFromAddressComponents = (
    // eslint-disable-next-line no-undef
    addressComponents: google.maps.GeocoderAddressComponent[]
): string | null => {
    const index = addressComponents.findIndex(
        (component) =>
            component.types.includes(MAPS_ROUTE) &&
            component.short_name !== MAPS_UNNAMED_ROAD
    );
    if (index !== -1) return addressComponents[index].short_name;
    return null;
};

const getHouseNumberFromAutoFilledAddress = (address: string): string => {
    if (address.match(/^\d/)) {
        const [firstToken] = address.split(' ');
        if (firstToken.length <= 3) {
            return firstToken;
        }
    }
    return '';
};

const getLongStreetNameFromAddressComponents = (
    // eslint-disable-next-line no-undef
    addressComponents: google.maps.GeocoderAddressComponent[]
): string | null => {
    for (let i = 0; i < addressComponents.length; i += 1) {
        if (
            addressComponents[i].types.includes(MAPS_ROUTE) &&
            addressComponents[i].short_name !== MAPS_UNNAMED_ROAD
        ) {
            return addressComponents[i].long_name;
        }
    }
    return null;
};

const hasPlaceName = (
    // eslint-disable-next-line no-undef
    placeDetails: google.maps.GeocoderResult,
    addressLine: string
): boolean => {
    const { address_components } = placeDetails;
    const houseNumber = getHouseNameFromAddressComponents(address_components);
    const longStreetName =
        getLongStreetNameFromAddressComponents(address_components);
    let defaultPlaceName = longStreetName;
    if (houseNumber != null) {
        defaultPlaceName = `${houseNumber} ${longStreetName}`;
    }

    return addressLine !== defaultPlaceName;
};

// eslint-disable-next-line import/prefer-default-export
export const parseGoogleResults = (
    // eslint-disable-next-line no-undef
    placeDetails: google.maps.GeocoderResult,
    addressLine: string
): ReverseGeocode | null => {
    // eslint-disable-next-line camelcase
    const { address_components, geometry } = placeDetails!;
    return {
        houseNumber: getHouseNameFromAddressComponents(address_components!)
            ? getHouseNameFromAddressComponents(address_components!)
            : getHouseNumberFromAutoFilledAddress(addressLine),
        streetName: getShortStreetNameFromAddressComponents(
            address_components!
        ),
        placeName: hasPlaceName(placeDetails!, addressLine)
            ? addressLine
            : null,
        district: '',
        latitude: geometry?.location?.lat()!,
        longitude: geometry?.location?.lng()!,
    };
};
