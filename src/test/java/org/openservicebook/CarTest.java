package org.openservicebook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import org.junit.jupiter.api.Test;

class CarTest {

    @Test
    void stripsSurroundingWhitespace() {
        Car car = new Car(" WVWZZZ1JZXW000001 ", " Volkswagen ", " Golf ", Year.of(1999));

        assertEquals("WVWZZZ1JZXW000001", car.getVin());
        assertEquals("Volkswagen", car.getMake());
        assertEquals("Golf", car.getModel());
        assertEquals(Year.of(1999), car.getModelYear());
    }

    @Test
    void rejectsBlankText() {
        assertThrows(IllegalArgumentException.class, () -> new Car(" ", "Volkswagen", "Golf", Year.of(1999)));
        assertThrows(IllegalArgumentException.class, () -> new Car("WVWZZZ1JZXW000001", "", "Golf", Year.of(1999)));
        assertThrows(IllegalArgumentException.class, () -> new Car("WVWZZZ1JZXW000001", "Volkswagen", "\t", Year.of(1999)));
    }

    @Test
    void rejectsTabsAndLineBreaks() {
        assertThrows(IllegalArgumentException.class, () -> new Car("WVWZZZ1JZXW000001", "Volks\twagen", "Golf", Year.of(1999)));
        assertThrows(IllegalArgumentException.class, () -> new Car("WVWZZZ1JZXW000001", "Volkswagen", "Golf\nGTI", Year.of(1999)));
        assertThrows(IllegalArgumentException.class, () -> new Car("WVWZZZ1JZXW\r000001", "Volkswagen", "Golf", Year.of(1999)));
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> new Car(null, "Volkswagen", "Golf", Year.of(1999)));
        assertThrows(NullPointerException.class, () -> new Car("WVWZZZ1JZXW000001", null, "Golf", Year.of(1999)));
        assertThrows(NullPointerException.class, () -> new Car("WVWZZZ1JZXW000001", "Volkswagen", null, Year.of(1999)));
        assertThrows(NullPointerException.class, () -> new Car("WVWZZZ1JZXW000001", "Volkswagen", "Golf", null));
    }

    @Test
    void startsWithNoServices() {
        Car car = new Car("WVWZZZ1JZXW000001", "Volkswagen", "Golf", Year.of(1999));

        assertTrue(car.getServices().isEmpty());
    }

    @Test
    void keepsServicesInTheOrderTheyWereAdded() {
        Car car = new Car("WVWZZZ1JZXW000001", "Volkswagen", "Golf", Year.of(1999));
        Service oilChange = new Service(LocalDate.of(2026, 3, 1), 110_000, "Oil change", Money.parse("89.90"));
        Service brakes = new Service(LocalDate.of(2026, 9, 1), 120_000, "Brake pads", Money.parse("245.00"));

        car.addService(oilChange);
        car.addService(brakes);

        assertEquals(List.of(oilChange, brakes), car.getServices());
    }

    @Test
    void rejectsNullService() {
        Car car = new Car("WVWZZZ1JZXW000001", "Volkswagen", "Golf", Year.of(1999));

        assertThrows(NullPointerException.class, () -> car.addService(null));
    }

    @Test
    void servicesCannotBeModifiedThroughTheGetter() {
        Car car = new Car("WVWZZZ1JZXW000001", "Volkswagen", "Golf", Year.of(1999));
        Service oilChange = new Service(LocalDate.of(2026, 3, 1), 110_000, "Oil change", Money.parse("89.90"));

        assertThrows(UnsupportedOperationException.class, () -> car.getServices().add(oilChange));
    }

    @Test
    void carsWithSameValuesAreEqual() {
        Car a = new Car("WVWZZZ1JZXW000001", "Volkswagen", "Golf", Year.of(1999));
        Car b = new Car("WVWZZZ1JZXW000001", "Volkswagen", "Golf", Year.of(1999));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
