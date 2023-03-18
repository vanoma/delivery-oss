package com.vanoma.api.order.maps;

public interface INavigationDistanceApi {
    long getNavigationDistance(Coordinates origin, Coordinates destination);
}
