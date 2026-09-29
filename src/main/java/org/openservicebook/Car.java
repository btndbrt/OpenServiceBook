package org.openservicebook;

import java.time.Year;
import java.util.Objects;

public final class Car {

    private final String vin;
    private final String make;
    private final String model;
    private final Year modelYear;

    public Car(String vin, String make, String model, Year modelYear) {
        this.vin = requireNonBlank(vin, "vin");
        this.make = requireNonBlank(make, "make");
        this.model = requireNonBlank(model, "model");
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

    private static String requireNonBlank(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.strip();
    }
}
