package org.openservicebook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Year;
import java.util.List;

import org.junit.jupiter.api.Test;

class ServiceBookTest {

    private static final Car GOLF = new Car("WVWZZZ1JZXW000001", "Volkswagen", "Golf", Year.of(1999));
    private static final Car ACCORD = new Car("JHMCM56557C404453", "Honda", "Accord", Year.of(2007));

    @Test
    void startsEmpty() {
        assertTrue(new ServiceBook().getCars().isEmpty());
    }

    @Test
    void keepsCarsInTheOrderTheyWereAdded() {
        ServiceBook book = new ServiceBook();
        book.addCar(GOLF);
        book.addCar(ACCORD);

        assertEquals(List.of(GOLF, ACCORD), book.getCars());
    }

    @Test
    void findsCarByVin() {
        ServiceBook book = new ServiceBook();
        book.addCar(GOLF);
        book.addCar(ACCORD);

        assertSame(ACCORD, book.findCar("JHMCM56557C404453").orElseThrow());
        assertSame(ACCORD, book.findCar(" JHMCM56557C404453 ").orElseThrow());
        assertTrue(book.findCar("UNKNOWN").isEmpty());
    }

    @Test
    void rejectsDuplicateVin() {
        ServiceBook book = new ServiceBook();
        book.addCar(GOLF);

        assertThrows(IllegalArgumentException.class,
                () -> book.addCar(new Car("WVWZZZ1JZXW000001", "Volkswagen", "Polo", Year.of(2001))));
    }

    @Test
    void carsCannotBeAddedThroughTheGetter() {
        ServiceBook book = new ServiceBook();

        assertThrows(UnsupportedOperationException.class, () -> book.getCars().add(GOLF));
    }
}
