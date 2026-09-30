package org.openservicebook;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ServiceBook {

    private final List<Car> cars = new ArrayList<>();

    public void addCar(Car car) {
        Objects.requireNonNull(car, "car");
        requireUnused(car.getVin(), null);
        requireUnused(car.getPlate(), null);
        cars.add(car);
    }

    /** Puts {@code edited} in the place of {@code old}. Its VIN and plate may match {@code old}, but no other car. */
    public void replaceCar(Car old, Car edited) {
        Objects.requireNonNull(edited, "edited");
        int index = indexOf(old);
        requireUnused(edited.getVin(), old);
        requireUnused(edited.getPlate(), old);
        cars.set(index, edited);
    }

    public void removeCar(Car car) {
        cars.remove(indexOf(car));
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

    // Throws if a car other than ignored already has this VIN or plate.
    private void requireUnused(String vinOrPlate, Car ignored) {
        Optional<Car> found = findCar(vinOrPlate);
        if (found.isPresent() && found.get() != ignored) {
            throw new IllegalArgumentException("a car with VIN or plate " + vinOrPlate + " already exists");
        }
    }

    // Compares with == rather than equals, so we always find this exact car object.
    private int indexOf(Car car) {
        Objects.requireNonNull(car, "car");
        for (int i = 0; i < cars.size(); i++) {
            if (cars.get(i) == car) {
                return i;
            }
        }
        throw new IllegalArgumentException("car is not in this service book: " + car.getVin());
    }
}
