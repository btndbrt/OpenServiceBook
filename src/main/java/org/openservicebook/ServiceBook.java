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
            throw new IllegalArgumentException("a car with VIN " + car.getVin() + " already exists");
        }
        cars.add(car);
    }

    public Optional<Car> findCar(String vin) {
        Objects.requireNonNull(vin, "vin");
        for (Car car : cars) {
            if (car.getVin().equals(vin.strip())) {
                return Optional.of(car);
            }
        }
        return Optional.empty();
    }

    public List<Car> getCars() {
        return List.copyOf(cars);
    }
}
