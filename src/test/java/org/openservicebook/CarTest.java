package org.openservicebook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import org.junit.jupiter.api.Test;

class CarTest {

    private static final String VIN = "WVWZZZ1JZXW000001";
    private static final String PLATE = "ABC-123";

    @Test
    void stripsSurroundingWhitespace() {
        Car car = new Car(" WVWZZZ1JZXW000001 ", " ABC-123 ", " Volkswagen ", " Golf ", Year.of(1999));

        assertEquals(VIN, car.getVin());
        assertEquals(PLATE, car.getPlate());
        assertEquals("Volkswagen", car.getMake());
        assertEquals("Golf", car.getModel());
        assertEquals(Year.of(1999), car.getModelYear());
    }

    @Test
    void rejectsBlankText() {
        assertThrows(IllegalArgumentException.class, () -> new Car(" ", PLATE, "Volkswagen", "Golf", Year.of(1999)));
        assertThrows(IllegalArgumentException.class, () -> new Car(VIN, " ", "Volkswagen", "Golf", Year.of(1999)));
        assertThrows(IllegalArgumentException.class, () -> new Car(VIN, PLATE, "", "Golf", Year.of(1999)));
        assertThrows(IllegalArgumentException.class, () -> new Car(VIN, PLATE, "Volkswagen", "\t", Year.of(1999)));
    }

    @Test
    void rejectsPlateWithoutLettersOrDigits() {
        assertThrows(IllegalArgumentException.class, () -> new Car(VIN, "-", "Volkswagen", "Golf", Year.of(1999)));
        assertThrows(IllegalArgumentException.class, () -> new Car(VIN, "- -", "Volkswagen", "Golf", Year.of(1999)));
    }

    @Test
    void rejectsTabsAndLineBreaks() {
        assertThrows(IllegalArgumentException.class, () -> new Car(VIN, PLATE, "Volks\twagen", "Golf", Year.of(1999)));
        assertThrows(IllegalArgumentException.class, () -> new Car(VIN, PLATE, "Volkswagen", "Golf\nGTI", Year.of(1999)));
        assertThrows(IllegalArgumentException.class, () -> new Car("WVWZZZ1JZXW\r000001", PLATE, "Volkswagen", "Golf", Year.of(1999)));
        assertThrows(IllegalArgumentException.class, () -> new Car(VIN, "ABC\t123", "Volkswagen", "Golf", Year.of(1999)));
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> new Car(null, PLATE, "Volkswagen", "Golf", Year.of(1999)));
        assertThrows(NullPointerException.class, () -> new Car(VIN, null, "Volkswagen", "Golf", Year.of(1999)));
        assertThrows(NullPointerException.class, () -> new Car(VIN, PLATE, null, "Golf", Year.of(1999)));
        assertThrows(NullPointerException.class, () -> new Car(VIN, PLATE, "Volkswagen", null, Year.of(1999)));
        assertThrows(NullPointerException.class, () -> new Car(VIN, PLATE, "Volkswagen", "Golf", null));
    }

    @Test
    void startsWithNoServices() {
        Car car = new Car(VIN, PLATE, "Volkswagen", "Golf", Year.of(1999));

        assertTrue(car.getServices().isEmpty());
    }

    @Test
    void keepsServicesInTheOrderTheyWereAdded() {
        Car car = new Car(VIN, PLATE, "Volkswagen", "Golf", Year.of(1999));
        Service oilChange = new Service(LocalDate.of(2026, 3, 1), 110_000, "Oil change", Money.parse("89.90"));
        Service brakes = new Service(LocalDate.of(2026, 9, 1), 120_000, "Brake pads", Money.parse("245.00"));

        car.addService(oilChange);
        car.addService(brakes);

        assertEquals(List.of(oilChange, brakes), car.getServices());
    }

    @Test
    void rejectsNullService() {
        Car car = new Car(VIN, PLATE, "Volkswagen", "Golf", Year.of(1999));

        assertThrows(NullPointerException.class, () -> car.addService(null));
    }

    @Test
    void servicesCannotBeModifiedThroughTheGetter() {
        Car car = new Car(VIN, PLATE, "Volkswagen", "Golf", Year.of(1999));
        Service oilChange = new Service(LocalDate.of(2026, 3, 1), 110_000, "Oil change", Money.parse("89.90"));

        assertThrows(UnsupportedOperationException.class, () -> car.getServices().add(oilChange));
    }

    @Test
    void carsWithSameValuesAreEqual() {
        Car a = new Car(VIN, PLATE, "Volkswagen", "Golf", Year.of(1999));
        Car b = new Car(VIN, PLATE, "Volkswagen", "Golf", Year.of(1999));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
