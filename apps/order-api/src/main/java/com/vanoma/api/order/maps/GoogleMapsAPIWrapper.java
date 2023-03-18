package com.vanoma.api.order.maps;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.GeocodingApiRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.model.*;
import com.vanoma.api.utils.exceptions.InvalidParameterException;
import com.vanoma.api.utils.httpwrapper.HttpResult;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GoogleMapsAPIWrapper implements IMapsAPI {

    private final static String KIGALI = "kigali";
    private final static String RWANDA = "rwanda";
    private final static String RW_REGION = "rw";
    private final static String MAPS_LOCALITY = "LOCALITY";
    private final static String MAPS_UNNAMED_ROAD = "Unnamed Road";
    private final static String MAPS_ROUTE = "ROUTE";
    private final static String MAPS_STREET_NUMBER = "STREET_NUMBER";
    private final static String MAPS_SUBLOCALITY_LEVEL_1 = "SUBLOCALITY_LEVEL_1";
    private final static String MAPS_ADMINISTRATIVE_AREA_LEVEL_1 = "ADMINISTRATIVE_AREA_LEVEL_1";
    private final static String MAPS_ADMINISTRATIVE_AREA_LEVEL_2 = "ADMINISTRATIVE_AREA_LEVEL_2";

    private static GoogleMapsAPIWrapper instance = null;
    private String apiKey;
    private GeoApiContext context;

    public GoogleMapsAPIWrapper(String apiKey) {
        this.apiKey = apiKey;
    }

    private GeoApiContext getContext() {
        this.context = new GeoApiContext.Builder()
                .apiKey(this.apiKey)
                .build();
        return this.context;
    }


    public static GoogleMapsAPIWrapper getInstance(String apiKey) {
        if (instance != null) return instance;
        instance = new GoogleMapsAPIWrapper(apiKey);
        return instance;
    }

    public HttpResult geocode(AddressLine addressLine) {
        String line = getStringAddressLine(addressLine);
        List<GeocodingResult> results = getAddressComponentsFromAddressLine(line);
        if (results.isEmpty()) throwGeocodeFailureError();
        GeocodingResult result = getMostAccurateResult(results);
        if (!isCountryRwanda(result.addressComponents)) throwOutOfRwandaError();
        if (!isProvinceKigali(result.addressComponents)) throwOutOfKigaliError();

        Map<String, Object> address = getAddressAttributes(result, results);
        address.put("houseNumber", addressLine.getHouseNumber()); // Return same house number.
        return new HttpResult(address, HttpStatus.OK.value());
    }

    private String getStringAddressLine(AddressLine addressLine) {
        if (addressLine.getStreetName() == null) {
            throw new InvalidParameterException("utils.geocode.missingStreetName");
        }
        String stringAddress = addressLine.getStreetName();
        if (addressLine.getHouseNumber() != null) {
            stringAddress = String.format("%s %s", addressLine.getHouseNumber(), addressLine.getStreetName());
        }
        return stringAddress;
    }

    private List<GeocodingResult> getAddressComponentsFromAddressLine(String addressLine) {
        List<GeocodingResult> results = new ArrayList<>();
        try {
            GeocodingApiRequest geocodingApiRequest = GeocodingApi.geocode(this.getContext(), addressLine);
            geocodingApiRequest = geocodingApiRequest.region(RW_REGION);
            GeocodingResult[] rawResults = geocodingApiRequest.await();
            results = Arrays.asList(rawResults);
        } catch (ApiException | InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            this.shutdownContextThread();
        }
        return results;
    }

    public HttpResult reverseGeocode(Coordinates coordinates) {
        List<GeocodingResult> results = getAddressComponents(coordinates);
        if (results.isEmpty()) throwReverseGeocodeFailureError();
        GeocodingResult geocodingResult = getMostAccurateResult(results);
        if (!isCountryRwanda(geocodingResult.addressComponents)) throwOutOfRwandaError();
        if (!isProvinceKigali(geocodingResult.addressComponents)) throwOutOfKigaliError();

        Map<String, Object> address = getAddressAttributes(geocodingResult, coordinates, results);
        address.put("houseNumber", null); // To avoid inaccurate house numbers
        return new HttpResult(address, HttpStatus.OK.value());
    }

    private HttpResult throwOutOfRwandaError() {
        throw new InvalidParameterException("crud.address.outOfRwanda");
    }

    private HttpResult throwOutOfKigaliError() {
        throw new InvalidParameterException("crud.address.outOfKigali");
    }

    private Map<String, Object> getAddressAttributes(GeocodingResult mostAccurate, List<GeocodingResult> allResults) {
        Map<String, Object> response = getStreetAddressAndDistrict(mostAccurate, allResults);
        response.put("latitude", mostAccurate.geometry.location.lat);
        response.put("longitude", mostAccurate.geometry.location.lng);
        return response;
    }

    private Map<String, Object> getStreetAddressAndDistrict(GeocodingResult mostAccurate, List<GeocodingResult> allResults) {
        Map<String, Object> response = new HashMap<>();
        response.put("houseNumber", getHouseNumber(mostAccurate.addressComponents));
        response.put("streetName", getStreetName(mostAccurate.addressComponents));
        String district = getDistrict(mostAccurate.addressComponents);
        if (district == null) {
            district = getDistrict(allResults);
        }
        response.put("district", district);
        return response;
    }

    private Map<String, Object> getAddressAttributes(GeocodingResult mostAccurate, Coordinates coordinates, List<GeocodingResult> allResults) {
        Map<String, Object> response = getStreetAddressAndDistrict(mostAccurate, allResults);
        response.put("latitude", coordinates.getLat());
        response.put("longitude", coordinates.getLng());
        return response;
    }

    private List<GeocodingResult> getAddressComponents(Coordinates coordinates) {
        List<GeocodingResult> geocodingResults = new ArrayList<>();
        try {
            LatLng latLng = new LatLng(coordinates.getLat(), coordinates.getLng());
            GeocodingResult[] results = GeocodingApi.reverseGeocode(this.getContext(), latLng).await();
            geocodingResults = Arrays.asList(results);
        } catch (ApiException | InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            this.shutdownContextThread();
        }
        return geocodingResults;
    }

    private GeocodingResult getMostAccurateResult(List<GeocodingResult> geocodingResults) {
        if (geocodingResults.size() == 1) return geocodingResults.get(0);

        GeocodingResult mostAccurate = geocodingResults.get(0);
        for (GeocodingResult result : geocodingResults) {
            AddressComponent addressComponent = Arrays.asList(result.addressComponents).get(0);
            if (hasStreetName(addressComponent)) {
                mostAccurate = result;
                if (hasHouseNumber(addressComponent)) {
                    break;
                }
            }
        }
        return mostAccurate;
    }

    private boolean hasStreetName(AddressComponent component) {
        List<String> cTypes = convertTypesArrayToStringList(component.types);
        return cTypes.contains(AddressType.ROUTE.name());

    }

    private boolean hasHouseNumber(AddressComponent component) {
        return convertTypesArrayToStringList(component.types).contains(AddressType.STREET_NUMBER.name());
    }

    private boolean isCountryRwanda(AddressComponent[] addressComponents) {
        List<AddressComponent> listComponents = Arrays.asList(addressComponents);
        String countryName = listComponents.get(listComponents.size() - 1).longName;
        if (countryName == null) return false;
        // Some results do not have the country object -- they just stop at ADMINISTRATIVE_AREA_LEVEL_1
        return RWANDA.equalsIgnoreCase(countryName) || countryName.toLowerCase().startsWith(KIGALI);
    }

    private boolean isProvinceKigali(AddressComponent[] addressComponents) {
        boolean isKigali = false;
        for (AddressComponent c : addressComponents) {
            List<String> cTypes = convertTypesArrayToStringList(c.types);

            if (cTypes.contains(MAPS_LOCALITY) &&
                    KIGALI.equalsIgnoreCase(c.longName)) {
                isKigali = true;
            } else if (cTypes.contains(MAPS_ADMINISTRATIVE_AREA_LEVEL_1) &&
                    c.longName.toLowerCase().contains(KIGALI.toLowerCase())) {
                isKigali = true;
            }
            if (isKigali) break;
        }
        return isKigali;
    }

    private String getHouseNumber(AddressComponent[] addressComponents) {
        String houseNumber = null;
        for (AddressComponent c : addressComponents) {
            List<String> cTypes = convertTypesArrayToStringList(c.types);
            if (cTypes.contains(MAPS_STREET_NUMBER)) {
                houseNumber = c.shortName;
                break;
            }
        }
        return houseNumber;
    }

    private List<String> convertTypesArrayToStringList(AddressComponentType[] types) {
        return Arrays.stream(types).map(Enum::name).collect(Collectors.toList());
    }

    private String getStreetName(AddressComponent[] addressComponents) {
        String streetName = null;
        for (AddressComponent c : addressComponents) {
            List<String> cTypes = convertTypesArrayToStringList(c.types);
            if (cTypes.contains(MAPS_ROUTE)) {
                streetName = c.shortName;
                break;
            }
        }
        if (MAPS_UNNAMED_ROAD.equalsIgnoreCase(streetName)) {
            streetName = null;
        }
        return streetName;
    }

    private String getDistrict(AddressComponent[] addressComponents) {
        String district = null;
        for (AddressComponent c : addressComponents) {
            List<String> cTypes = convertTypesArrayToStringList(c.types);
            if (cTypes.contains(MAPS_ADMINISTRATIVE_AREA_LEVEL_2) && RwandaDistricts.isValid(c.longName)) {
                district = c.longName;
                break;
            }
        }
        return district;
    }

    private String getDistrict(List<GeocodingResult> results) {
        String district = null;

        for (GeocodingResult result : results) {
            List<AddressComponent> addressComponents = Arrays.asList(result.addressComponents);
            if (district == null) {
                district = getDistrict(result.addressComponents);
                if (containsType(addressComponents, MAPS_SUBLOCALITY_LEVEL_1)) break;
            }
        }
        return district;
    }

    private boolean containsType(List<AddressComponent> addressComponents, String mapsType) {
        for (AddressComponent c : addressComponents) {
            List<String> cTypes = convertTypesArrayToStringList(c.types);
            if (cTypes.contains(mapsType)) return true;
        }
        return false;
    }

    private void throwReverseGeocodeFailureError() {
        throw new InvalidParameterException("crud.address.reverseGeocodingFailure");
    }

    private void throwGeocodeFailureError() {
        throw new InvalidParameterException("crud.address.geocodingFailure");
    }

    private void shutdownContextThread() {
        // From docs: Call the shutdown() method of GeoApiContext, otherwise the thread will remain instantiated in memory
        this.context.shutdown();
    }
}
