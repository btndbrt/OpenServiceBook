package org.openservicebook;

import java.time.LocalDate;
import java.util.Objects;

public final class Service {

    private final LocalDate date;
    private final int mileage;
    private final String description;

    public Service(LocalDate date, int mileage, String description) {
        this.date = Objects.requireNonNull(date, "date");
        if (mileage < 0) {
            throw new IllegalArgumentException("mileage must not be negative");
        }
        this.mileage = mileage;
        Objects.requireNonNull(description, "description");
        if (description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank");
        }
        this.description = description.strip();
    }

    public LocalDate getDate() {
        return date;
    }

    public int getMileage() {
        return mileage;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Service other
                && date.equals(other.date)
                && mileage == other.mileage
                && description.equals(other.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, mileage, description);
    }

    @Override
    public String toString() {
        return "Service[date=" + date + ", mileage=" + mileage + ", description=" + description + "]";
    }
}
