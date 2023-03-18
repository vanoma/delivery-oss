package com.vanoma.api.order.orders;

import com.vanoma.api.order.packages.PackageJson;
import com.vanoma.api.utils.exceptions.InvalidParameterException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DeliveryOrderJson implements Serializable {
    private String agentId;
    private List<PackageJson> packages = new ArrayList<>();

    public DeliveryOrderJson() {
    }

    public String getAgentId() {
        return agentId;
    }

    public List<PackageJson> getPackages() {
        return packages;
    }

    public void validate() {
        if (packages == null || packages.size() == 0) {
            throw new InvalidParameterException("crud.package.notFound");
        }

        for (PackageJson pkg : packages) {
            pkg.validate();
        }
    }
}
