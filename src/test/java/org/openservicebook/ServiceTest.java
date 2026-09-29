package org.openservicebook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class ServiceTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 1);
    private static final Money COST = Money.parse("89.90");

    @Test
    void stripsSurroundingWhitespaceFromDescription() {
        Service service = new Service(DATE, 120_000, " Oil change ", COST);

        assertEquals(DATE, service.getDate());
        assertEquals(120_000, service.getMileage());
        assertEquals("Oil change", service.getDescription());
        assertEquals(COST, service.getCost());
    }

    @Test
    void acceptsZeroMileage() {
        assertEquals(0, new Service(DATE, 0, "Pre-delivery inspection", COST).getMileage());
    }

    @Test
    void rejectsNegativeMileage() {
        assertThrows(IllegalArgumentException.class, () -> new Service(DATE, -1, "Oil change", COST));
    }

    @Test
    void rejectsBlankDescription() {
        assertThrows(IllegalArgumentException.class, () -> new Service(DATE, 120_000, " ", COST));
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> new Service(null, 120_000, "Oil change", COST));
        assertThrows(NullPointerException.class, () -> new Service(DATE, 120_000, null, COST));
        assertThrows(NullPointerException.class, () -> new Service(DATE, 120_000, "Oil change", null));
    }

    @Test
    void servicesWithSameValuesAreEqual() {
        Service a = new Service(DATE, 120_000, "Oil change", COST);
        Service b = new Service(DATE, 120_000, "Oil change", Money.parse("89.9"));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
