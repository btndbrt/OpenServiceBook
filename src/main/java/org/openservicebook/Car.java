package org.openservicebook;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Car {

    private final String vin;
    private final String plate;
    private final String make;
    private final String model;
    private final Year modelYear;
    private final List<Service> services = new ArrayList<>();

    public Car(String vin, String plate, String make, String model, Year modelYear) {
        this.vin = Text.requireSingleLine(vin, "vin");
        this.plate = Text.requireSingleLine(plate, "plate");
        if (Text.lookupKey(this.plate).isEmpty()) {
            throw new IllegalArgumentException("plate must contain letters or digits");
        }
        this.make = Text.requireSingleLine(make, "make");
        this.model = Text.requireSingleLine(model, "model");
        this.modelYear = Objects.requireNonNull(modelYear, "modelYear");
    }

    public String getVin() {
        return vin;
    }

    public String getPlate() {
        return plate;
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

    // index counts from 0, like a List; the command line turns the user's service number into it.
    public void replaceService(int index, Service service) {
        services.set(index, Objects.requireNonNull(service, "service"));
    }

    public void removeService(int index) {
        services.remove(index);
    }

    public List<Service> getServices() {
        return List.copyOf(services);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Car other
                && vin.equals(other.vin)
                && plate.equals(other.plate)
                && make.equals(other.make)
                && model.equals(other.model)
                && modelYear.equals(other.modelYear);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vin, plate, make, model, modelYear);
    }

    @Override
    public String toString() {
        return "Car[vin=" + vin + ", plate=" + plate + ", make=" + make + ", model=" + model
                + ", modelYear=" + modelYear + "]";
    }
}
