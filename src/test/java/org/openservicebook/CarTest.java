package org.openservicebook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Year;

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
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> new Car(null, "Volkswagen", "Golf", Year.of(1999)));
        assertThrows(NullPointerException.class, () -> new Car("WVWZZZ1JZXW000001", null, "Golf", Year.of(1999)));
        assertThrows(NullPointerException.class, () -> new Car("WVWZZZ1JZXW000001", "Volkswagen", null, Year.of(1999)));
        assertThrows(NullPointerException.class, () -> new Car("WVWZZZ1JZXW000001", "Volkswagen", "Golf", null));
    }

    @Test
    void carsWithSameValuesAreEqual() {
        Car a = new Car("WVWZZZ1JZXW000001", "Volkswagen", "Golf", Year.of(1999));
        Car b = new Car("WVWZZZ1JZXW000001", "Volkswagen", "Golf", Year.of(1999));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
