package org.openservicebook;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ServiceBook {

    private final List<Car> cars = new ArrayList<>();

    public void addCar(Car car) {
        Objects.requireNonNull(car, "car");
        if (findCar(car.getVin()).isPresent()) {
            throw new IllegalArgumentException("a car with VIN or plate " + car.getVin() + " already exists");
        }
        if (findCar(car.getPlate()).isPresent()) {
            throw new IllegalArgumentException("a car with VIN or plate " + car.getPlate() + " already exists");
        }
        cars.add(car);
    }

    /** Finds a car by its VIN or its plate, ignoring case, spaces and dashes. */
    public Optional<Car> findCar(String vinOrPlate) {
        Objects.requireNonNull(vinOrPlate, "vinOrPlate");
        String key = Text.lookupKey(vinOrPlate);
        for (Car car : cars) {
            if (Text.lookupKey(car.getVin()).equals(key) || Text.lookupKey(car.getPlate()).equals(key)) {
                return Optional.of(car);
            }
        }
        return Optional.empty();
    }

    public List<Car> getCars() {
        return List.copyOf(cars);
    }
}
