package com.vanoma.api.order.maps;

public class Coordinates {
    private Double lat;
    private Double lng;

    public Coordinates() {
    }


    public Double getLat() {
        return lat;
    }

    public Coordinates setLat(Double lat) {
        this.lat = lat;
        return this;
    }

    public Double getLng() {
        return lng;
    }

    public Coordinates setLng(Double lng) {
        this.lng = lng;
        return this;
    }
}
