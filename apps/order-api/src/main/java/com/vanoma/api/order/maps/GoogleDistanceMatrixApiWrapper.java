package com.vanoma.api.order.maps;

import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.LatLng;
import com.vanoma.api.utils.exceptions.InvalidParameterException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GoogleDistanceMatrixApiWrapper implements INavigationDistanceApi {

    private GeoApiContext context;
    private String apiKey;

    public GoogleDistanceMatrixApiWrapper(String apiKey) {
        this.apiKey = apiKey;
    }

    private GeoApiContext getContext() {
        this.context = new GeoApiContext.Builder()
                .apiKey(this.apiKey)
                .build();
        return this.context;
    }

    @Override
    public long getNavigationDistance(Coordinates origin, Coordinates destination) {
        try {
            DistanceMatrixApiRequest request = DistanceMatrixApi.newRequest(this.getContext());
            DistanceMatrix matrix = request.origins(new LatLng(origin.getLat(), origin.getLng()))
                    .destinations(new LatLng(destination.getLat(), destination.getLng()))
                    .await();
            return getDistance(matrix);

        } catch (ApiException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            ;
        } finally {
            this.shutdownContextThread();
        }
        return 0;
    }

    private long getDistance(DistanceMatrix matrix) {
        List<List<DistanceMatrixElement>> elements = Arrays.asList(matrix.rows)
                .stream()
                .map(r -> Arrays.asList(r.elements))
                .collect(Collectors.toList());
        long distance = 0;
        for (List<DistanceMatrixElement> elementList : elements) {
            for (DistanceMatrixElement element : elementList) {
                distance = element.distance.inMeters;
                break;
            }
        }
        if (distance == 0) throw new InvalidParameterException("utils.matrixApi.navigationDistanceNotFound");
        return distance;
    }

    private void shutdownContextThread() {
        // From docs: Call the shutdown() method of GeoApiContext, otherwise the thread will remain instantiated in memory
        this.context.shutdown();
    }
}
