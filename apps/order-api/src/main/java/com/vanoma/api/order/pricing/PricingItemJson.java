package com.vanoma.api.order.pricing;

import com.vanoma.api.order.maps.Coordinates;
import com.vanoma.api.order.packages.PackageSize;
import com.vanoma.api.utils.input.CoordinatesJson;

import java.io.Serializable;

public class PricingItemJson implements Serializable {

    private String packageId;
    private Double volume;

    private String size;
    private CoordinatesJson origin;
    private CoordinatesJson destination;

    public String getPackageId() {
        return packageId;
    }

    public PackageSize getSize() {
        return PackageSize.create(size);
    }

    public CoordinatesJson getOrigin() {
        return origin;
    }

    public CoordinatesJson getDestination() {
        return destination;
    }


    // Setters for testing
    public PricingItemJson setPackageId(String packageId) {
        this.packageId = packageId;
        return this;
    }

    public PricingItemJson setVolume(Double volume) {
        this.volume = volume;
        return this;
    }

    public PricingItemJson setOrigin(CoordinatesJson origin) {
        this.origin = origin;
        return this;
    }

    public PricingItemJson setDestination(CoordinatesJson destination) {
        this.destination = destination;
        return this;
    }

    public boolean hasSize() {
        return this.size != null;
    }
}
