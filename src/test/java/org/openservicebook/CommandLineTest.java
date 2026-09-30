package org.openservicebook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandLineTest {

    private static final String VIN = "WVWZZZ1JZXW000001";

    @TempDir
    Path folder;

    private ServiceBookFile file;
    private Installer installer;

    // Instead of the real screen, the command line prints into these buffers so the tests can read what it printed.
    private ByteArrayOutputStream out;
    private ByteArrayOutputStream err;

    @BeforeEach
    void setUp() {
        file = new ServiceBookFile(folder.resolve("servicebook.txt"));
        installer = new Installer(folder.resolve("app"), folder.resolve("bin"));
        out = new ByteArrayOutputStream();
        err = new ByteArrayOutputStream();
    }

    private int runCommand(String... args) {
        out.reset();
        err.reset();
        CommandLine commandLine = new CommandLine(file, installer,
                new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(err, true, StandardCharsets.UTF_8));
        return commandLine.run(args);
    }

    private String out() {
        return out.toString(StandardCharsets.UTF_8);
    }

    private String err() {
        return err.toString(StandardCharsets.UTF_8);
    }

    @Test
    void addCarSavesItToTheFile() throws IOException {
        assertEquals(0, runCommand("add-car", VIN, "ABC-123", "Volkswagen", "Golf", "1999"));

        assertTrue(out().contains("Added Volkswagen Golf (1999), plate ABC-123, VIN " + VIN), out());
        assertEquals(1, file.load().getCars().size());
    }

    @Test
    void listCarsShowsEveryCar() {
        runCommand("add-car", VIN, "ABC-123", "Volkswagen", "Golf", "1999");
        runCommand("add-car", "JHMCM56557C404453", "XYZ-789", "Honda", "Accord", "2007");

        assertEquals(0, runCommand("list-cars"));

        assertEquals(List.of(
                "Volkswagen Golf (1999), plate ABC-123, VIN " + VIN,
                "Honda Accord (2007), plate XYZ-789, VIN JHMCM56557C404453"), out().lines().toList());
    }

    @Test
    void listCarsWithNoCarsSaysSo() {
        assertEquals(0, runCommand("list-cars"));

        assertTrue(out().startsWith("No cars yet."), out());
    }

    @Test
    void addServiceThenShowHistory() throws IOException {
        runCommand("add-car", VIN, "ABC-123", "Volkswagen", "Golf", "1999");

        assertEquals(0, runCommand("add-service", VIN, "2026-03-01", "110000", "89.9", "Oil change"));
        assertEquals(0, runCommand("add-service", VIN, "2026-09-01", "120000", "245", "Brake pads"));
        assertEquals(0, runCommand("services", VIN));

        assertEquals(List.of(
                "  1  2026-03-01    110000       89.90  Oil change",
                "  2  2026-09-01    120000      245.00  Brake pads"), out().lines().toList());
        assertEquals(2, file.load().findCar(VIN).orElseThrow().getServices().size());
    }

    @Test
    void plateCanBeUsedInsteadOfVin() throws IOException {
        runCommand("add-car", VIN, "ABC-123", "Volkswagen", "Golf", "1999");

        assertEquals(0, runCommand("add-service", "abc123", "2026-03-01", "110000", "89.90", "Oil change"));
        assertEquals(0, runCommand("services", "ABC 123"));

        assertEquals(List.of("  1  2026-03-01    110000       89.90  Oil change"), out().lines().toList());
    }

    @Test
    void editCarChangesOneFieldAndKeepsServices() throws IOException {
        runCommand("add-car", VIN, "ABC-123", "Volkswagen", "Golf", "1999");
        runCommand("add-service", VIN, "2026-03-01", "110000", "89.90", "Oil change");

        assertEquals(0, runCommand("edit-car", "ABC-123", "plate", "XYZ-999"));
        assertEquals(0, runCommand("edit-car", "XYZ-999", "year", "2000"));
        assertEquals(0, runCommand("edit-car", "XYZ-999", "model", "Golf", "GTI"));

        assertTrue(out().contains("Updated Volkswagen Golf GTI (2000), plate XYZ-999"), out());
        Car car = file.load().findCar(VIN).orElseThrow();
        assertEquals("XYZ-999", car.getPlate());
        assertEquals(1, car.getServices().size());
        assertTrue(file.load().findCar("ABC-123").isEmpty());
    }

    @Test
    void editCarRejectsPlateOfAnotherCar() throws IOException {
        runCommand("add-car", VIN, "ABC-123", "Volkswagen", "Golf", "1999");
        runCommand("add-car", "JHMCM56557C404453", "XYZ-789", "Honda", "Accord", "2007");

        assertEquals(1, runCommand("edit-car", "ABC-123", "plate", "xyz 789"));
        assertTrue(err().contains("already exists"), err());
        assertEquals("ABC-123", file.load().findCar(VIN).orElseThrow().getPlate());
    }

    @Test
    void editCarRejectsUnknownFieldAndBadYear() {
        runCommand("add-car", VIN, "ABC-123", "Volkswagen", "Golf", "1999");

        assertEquals(1, runCommand("edit-car", VIN, "colour", "red"));
        assertTrue(err().contains("unknown car field: colour"), err());
        assertEquals(1, runCommand("edit-car", VIN, "year", "old"));
        assertTrue(err().contains("year must be a number"), err());
        assertEquals(1, runCommand("edit-car", VIN, "plate"));
        assertTrue(err().contains("usage: osb edit-car"), err());
    }

    @Test
    void editServiceChangesOneField() throws IOException {
        runCommand("add-car", VIN, "ABC-123", "Volkswagen", "Golf", "1999");
        runCommand("add-service", VIN, "2026-03-01", "110000", "89.90", "Oil change");
        runCommand("add-service", VIN, "2026-09-01", "120000", "245", "Brake pads");

        assertEquals(0, runCommand("edit-service", "ABC-123", "2", "cost", "250"));
        assertEquals(0, runCommand("edit-service", "ABC-123", "2", "description", "Front", "brake", "pads"));

        assertTrue(out().contains("2026-09-01    120000      250.00  Front brake pads"), out());
        List<Service> services = file.load().findCar(VIN).orElseThrow().getServices();
        assertEquals("Oil change", services.get(0).getDescription());
        assertEquals("Front brake pads", services.get(1).getDescription());
        assertEquals(Money.parse("250"), services.get(1).getCost());
    }

    @Test
    void editServiceRejectsBadNumberFieldOrValue() {
        runCommand("add-car", VIN, "ABC-123", "Volkswagen", "Golf", "1999");
        runCommand("add-service", VIN, "2026-03-01", "110000", "89.90", "Oil change");

        assertEquals(1, runCommand("edit-service", VIN, "2", "cost", "10"));
        assertTrue(err().contains("no service number 2"), err());
        assertEquals(1, runCommand("edit-service", VIN, "first", "cost", "10"));
        assertTrue(err().contains("service number must be a whole number"), err());
        assertEquals(1, runCommand("edit-service", VIN, "1", "price", "10"));
        assertTrue(err().contains("unknown service field: price"), err());
        assertEquals(1, runCommand("edit-service", VIN, "1", "date", "yesterday"));
        assertTrue(err().contains("date must look like"), err());
    }

    @Test
    void deleteCarRemovesItAndItsServices() throws IOException {
        runCommand("add-car", VIN, "ABC-123", "Volkswagen", "Golf", "1999");
        runCommand("add-car", "JHMCM56557C404453", "XYZ-789", "Honda", "Accord", "2007");
        runCommand("add-service", VIN, "2026-03-01", "110000", "89.90", "Oil change");

        assertEquals(0, runCommand("delete-car", "abc123"));

        assertTrue(out().contains("Deleted Volkswagen Golf (1999)") && out().contains("its 1 service(s)"), out());
        List<Car> cars = file.load().getCars();
        assertEquals(1, cars.size());
        assertEquals("XYZ-789", cars.get(0).getPlate());
    }

    @Test
    void deleteServiceRemovesOnlyThatOne() throws IOException {
        runCommand("add-car", VIN, "ABC-123", "Volkswagen", "Golf", "1999");
        runCommand("add-service", VIN, "2026-03-01", "110000", "89.90", "Oil change");
        runCommand("add-service", VIN, "2026-09-01", "120000", "245", "Brake pads");

        assertEquals(0, runCommand("delete-service", VIN, "1"));

        List<Service> services = file.load().findCar(VIN).orElseThrow().getServices();
        assertEquals(1, services.size());
        assertEquals("Brake pads", services.get(0).getDescription());
        assertEquals(1, runCommand("delete-service", VIN, "2"));
        assertTrue(err().contains("no service number 2"), err());
    }

    @Test
    void deletingUnknownCarIsAnError() {
        assertEquals(1, runCommand("delete-car", VIN));
        assertTrue(err().contains("no car with VIN or plate " + VIN), err());
    }

    @Test
    void descriptionWithoutQuotesIsJoined() throws IOException {
        runCommand("add-car", VIN, "ABC-123", "Volkswagen", "Golf", "1999");

        assertEquals(0, runCommand("add-service", VIN, "2026-03-01", "110000", "89.90", "Oil", "and", "filter"));

        Service service = file.load().findCar(VIN).orElseThrow().getServices().get(0);
        assertEquals("Oil and filter", service.getDescription());
    }

    @Test
    void servicesWithNoHistorySaysSo() {
        runCommand("add-car", VIN, "ABC-123", "Volkswagen", "Golf", "1999");

        assertEquals(0, runCommand("services", VIN));

        assertTrue(out().startsWith("No services recorded for"), out());
    }

    @Test
    void unknownVinIsAnError() {
        assertEquals(1, runCommand("services", VIN));
        assertTrue(err().contains("no car with VIN or plate " + VIN), err());

        assertEquals(1, runCommand("add-service", VIN, "2026-03-01", "110000", "89.90", "Oil change"));
        assertTrue(err().contains("no car with VIN or plate " + VIN), err());
    }

    @Test
    void duplicateVinIsAnError() {
        runCommand("add-car", VIN, "ABC-123", "Volkswagen", "Golf", "1999");

        assertEquals(1, runCommand("add-car", VIN, "DEF-456", "Volkswagen", "Polo", "2001"));
        assertTrue(err().contains("already exists"), err());
    }

    @Test
    void badValuesAreErrorsAndNothingIsSaved() throws IOException {
        assertEquals(1, runCommand("add-car", VIN, "ABC-123", "Volkswagen", "Golf", "nineteen"));
        assertTrue(err().contains("year must be a number"), err());
        runCommand("add-car", VIN, "ABC-123", "Volkswagen", "Golf", "1999");

        assertEquals(1, runCommand("add-service", VIN, "01/03/2026", "110000", "89.90", "Oil change"));
        assertTrue(err().contains("date must look like"), err());
        assertEquals(1, runCommand("add-service", VIN, "2026-03-01", "110k", "89.90", "Oil change"));
        assertTrue(err().contains("mileage must be a whole number"), err());
        assertEquals(1, runCommand("add-service", VIN, "2026-03-01", "-5", "89.90", "Oil change"));
        assertTrue(err().contains("mileage must not be negative"), err());
        assertEquals(1, runCommand("add-service", VIN, "2026-03-01", "110000", "89,90", "Oil change"));
        assertTrue(err().contains("not a valid amount"), err());

        assertTrue(file.load().findCar(VIN).orElseThrow().getServices().isEmpty());
    }

    @Test
    void wrongNumberOfValuesPrintsUsage() {
        assertEquals(1, runCommand("add-car", VIN, "ABC-123", "Volkswagen", "Golf"));
        assertTrue(err().contains("usage: osb add-car"), err());

        assertEquals(1, runCommand("add-service", VIN, "2026-03-01", "110000", "89.90"));
        assertTrue(err().contains("usage: osb add-service"), err());

        assertEquals(1, runCommand("services"));
        assertTrue(err().contains("usage: osb services"), err());
    }

    @Test
    void noCommandPrintsUsage() {
        assertEquals(1, runCommand());
        assertTrue(err().contains("osb list-cars"), err());
    }

    @Test
    void helpPrintsUsage() {
        assertEquals(0, runCommand("help"));
        assertTrue(out().contains("osb add-service"), out());
    }

    @Test
    void unknownCommandIsAnError() {
        assertEquals(1, runCommand("fly"));
        assertTrue(err().contains("unknown command: fly"), err());
    }

    @Test
    void doesNotCreateTheFileWhenOnlyReading() {
        runCommand("list-cars");

        assertFalse(Files.exists(folder.resolve("servicebook.txt")));
    }

    // Tests run from compiled class folders, not from a jar, so install must refuse and create nothing.
    @Test
    void installRefusesWhenNotRunningFromAJar() {
        assertEquals(1, runCommand("install"));

        assertTrue(err().contains("install only works when running from the jar file"), err());
        assertFalse(Files.exists(folder.resolve("bin")));
    }
}
