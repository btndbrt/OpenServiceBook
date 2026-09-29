package org.openservicebook;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Car {

    private final String vin;
    private final String make;
    private final String model;
    private final Year modelYear;
    private final List<Service> services = new ArrayList<>();

    public Car(String vin, String make, String model, Year modelYear) {
        this.vin = Text.requireSingleLine(vin, "vin");
        this.make = Text.requireSingleLine(make, "make");
        this.model = Text.requireSingleLine(model, "model");
        this.modelYear = Objects.requireNonNull(modelYear, "modelYear");
    }

    public String getVin() {
        return vin;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public Year getModelYear() {
        return modelYear;
    }

    public void addService(Service service) {
        services.add(Objects.requireNonNull(service, "service"));
    }

    public List<Service> getServices() {
        return List.copyOf(services);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Car other
                && vin.equals(other.vin)
                && make.equals(other.make)
                && model.equals(other.model)
                && modelYear.equals(other.modelYear);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vin, make, model, modelYear);
    }

    @Override
    public String toString() {
        return "Car[vin=" + vin + ", make=" + make + ", model=" + model + ", modelYear=" + modelYear + "]";
    }
}
