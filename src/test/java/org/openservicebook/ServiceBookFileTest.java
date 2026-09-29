package org.openservicebook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ServiceBookFileTest {

    // JUnit creates a fresh empty folder for each test and deletes it afterwards.
    @TempDir
    Path folder;

    @Test
    void missingFileLoadsAsEmptyBook() throws IOException {
        ServiceBook book = new ServiceBookFile(folder.resolve("servicebook.txt")).load();

        assertTrue(book.getCars().isEmpty());
    }

    @Test
    void savedBookLoadsBackTheSame() throws IOException {
        Car golf = new Car("WVWZZZ1JZXW000001", "ABC-123", "Volkswagen", "Golf", Year.of(1999));
        Service oilChange = new Service(LocalDate.of(2026, 3, 1), 110_000, "Oil change", Money.parse("89.90"));
        Service brakes = new Service(LocalDate.of(2026, 9, 1), 120_000, "Brake pads", Money.parse("245"));
        golf.addService(oilChange);
        golf.addService(brakes);
        Car accord = new Car("JHMCM56557C404453", "XYZ-789", "Honda", "Accord", Year.of(2007));
        ServiceBook book = new ServiceBook();
        book.addCar(golf);
        book.addCar(accord);
        ServiceBookFile file = new ServiceBookFile(folder.resolve("servicebook.txt"));

        file.save(book);
        ServiceBook loaded = file.load();

        assertEquals(List.of(golf, accord), loaded.getCars());
        assertEquals(List.of(oilChange, brakes), loaded.getCars().get(0).getServices());
        assertTrue(loaded.getCars().get(1).getServices().isEmpty());
    }

    @Test
    void writesOneTabSeparatedLinePerRecord() throws IOException {
        Car golf = new Car("WVWZZZ1JZXW000001", "ABC-123", "Volkswagen", "Golf", Year.of(1999));
        golf.addService(new Service(LocalDate.of(2026, 3, 1), 110_000, "Oil change", Money.parse("89.9")));
        ServiceBook book = new ServiceBook();
        book.addCar(golf);
        Path path = folder.resolve("servicebook.txt");

        new ServiceBookFile(path).save(book);

        assertEquals(List.of(
                "CAR\tWVWZZZ1JZXW000001\tABC-123\tVolkswagen\tGolf\t1999",
                "SERVICE\t2026-03-01\t110000\t89.90\tOil change"), Files.readAllLines(path));
    }

    @Test
    void replacesPreviousSave() throws IOException {
        ServiceBookFile file = new ServiceBookFile(folder.resolve("servicebook.txt"));
        ServiceBook first = new ServiceBook();
        first.addCar(new Car("WVWZZZ1JZXW000001", "ABC-123", "Volkswagen", "Golf", Year.of(1999)));
        file.save(first);
        Car accord = new Car("JHMCM56557C404453", "XYZ-789", "Honda", "Accord", Year.of(2007));
        ServiceBook second = new ServiceBook();
        second.addCar(accord);

        file.save(second);

        assertEquals(List.of(accord), file.load().getCars());
    }

    @Test
    void leavesNoTemporaryFileBehind() throws IOException {
        new ServiceBookFile(folder.resolve("servicebook.txt")).save(new ServiceBook());

        try (var files = Files.list(folder)) {
            assertEquals(List.of(folder.resolve("servicebook.txt")), files.toList());
        }
    }

    @Test
    void createsMissingFolders() throws IOException {
        Path path = folder.resolve("nested").resolve("servicebook.txt");

        new ServiceBookFile(path).save(new ServiceBook());

        assertTrue(Files.exists(path));
    }

    @Test
    void skipsBlankLines() throws IOException {
        Path path = folder.resolve("servicebook.txt");
        Files.writeString(path, "\nCAR\tWVWZZZ1JZXW000001\tABC-123\tVolkswagen\tGolf\t1999\n\n");

        assertEquals(1, new ServiceBookFile(path).load().getCars().size());
    }

    @Test
    void reportsLineNumberOfBadLine() throws IOException {
        Path path = folder.resolve("servicebook.txt");
        Files.writeString(path, "CAR\tWVWZZZ1JZXW000001\tABC-123\tVolkswagen\tGolf\t1999\n"
                + "SERVICE\t2026-13-45\t110000\t89.90\tOil change\n");

        IOException e = assertThrows(IOException.class, () -> new ServiceBookFile(path).load());
        assertTrue(e.getMessage().contains("line 2"), e.getMessage());
    }

    @Test
    void rejectsMalformedLines() throws IOException {
        List<String> badFiles = List.of(
                "SERVICE\t2026-03-01\t110000\t89.90\tOil change",
                "CAR\tWVWZZZ1JZXW000001\tABC-123\tVolkswagen\tGolf",
                "CAR\tWVWZZZ1JZXW000001\tABC-123\tVolkswagen\tGolf\tnineteen",
                "BOAT\tWVWZZZ1JZXW000001\tVolkswagen\tGolf\t1999",
                "CAR\tWVWZZZ1JZXW000001\tABC-123\tVolkswagen\tGolf\t1999\nSERVICE\t2026-03-01\tlots\t89.90\tOil change",
                "CAR\tWVWZZZ1JZXW000001\tABC-123\tVolkswagen\tGolf\t1999\nSERVICE\t2026-03-01\t110000\t-5\tOil change",
                "CAR\tWVWZZZ1JZXW000001\tABC-123\tVolkswagen\tGolf\t1999\nCAR\tWVWZZZ1JZXW000001\tABC-123\tVolkswagen\tPolo\t2001");
        Path path = folder.resolve("servicebook.txt");

        for (String content : badFiles) {
            Files.writeString(path, content);
            assertThrows(IOException.class, () -> new ServiceBookFile(path).load(), content);
        }
    }
}
