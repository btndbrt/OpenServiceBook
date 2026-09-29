package org.openservicebook;

import java.io.IOException;
import java.io.PrintStream;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Runs one command per program start, e.g. {@code add-car <vin> <make> <model> <year>},
 * loading the service book from its file and saving it back after a change.
 */
public final class CommandLine {

    private static final String USAGE = String.join(System.lineSeparator(),
            "usage:",
            "  osb add-car <vin> <plate> <make> <model> <year>",
            "  osb add-service <vin-or-plate> <date> <mileage> <cost> <description>",
            "  osb list-cars",
            "  osb services <vin-or-plate>");

    private final ServiceBookFile file;
    private final PrintStream out;
    private final PrintStream err;

    public CommandLine(ServiceBookFile file, PrintStream out, PrintStream err) {
        this.file = Objects.requireNonNull(file, "file");
        this.out = Objects.requireNonNull(out, "out");
        this.err = Objects.requireNonNull(err, "err");
    }

    /** Runs the command in {@code args} and returns the exit code: 0 on success, 1 on any error. */
    public int run(String[] args) {
        if (args.length == 0) {
            err.println(USAGE);
            return 1;
        }
        try {
            switch (args[0]) {
                case "add-car" -> addCar(args);
                case "add-service" -> addService(args);
                case "list-cars" -> listCars(args);
                case "services" -> services(args);
                case "help" -> out.println(USAGE);
                default -> throw new IllegalArgumentException("unknown command: " + args[0] + System.lineSeparator() + USAGE);
            }
            return 0;
        } catch (IllegalArgumentException | IOException e) {
            err.println("error: " + e.getMessage());
            return 1;
        }
    }

    private void addCar(String[] args) throws IOException {
        requireArgumentCount(args, 6, "osb add-car <vin> <plate> <make> <model> <year>");
        Car car = new Car(args[1], args[2], args[3], args[4], parseYear(args[5]));

        ServiceBook book = file.load();
        book.addCar(car);
        file.save(book);
        out.println("Added " + describe(car));
    }

    private void addService(String[] args) throws IOException {
        if (args.length < 6) {
            throw new IllegalArgumentException("usage: osb add-service <vin-or-plate> <date> <mileage> <cost> <description>");
        }
        // Everything after the cost is the description, so "Oil change" works with or without quotes.
        String description = String.join(" ", Arrays.copyOfRange(args, 5, args.length));
        Service service = new Service(parseDate(args[2]), parseMileage(args[3]), description, Money.parse(args[4]));

        ServiceBook book = file.load();
        Car car = findCar(book, args[1]);
        car.addService(service);
        file.save(book);
        out.println("Added service to " + describe(car) + ": " + service.getDate() + " " + service.getDescription());
    }

    private void listCars(String[] args) throws IOException {
        requireArgumentCount(args, 1, "osb list-cars");
        List<Car> cars = file.load().getCars();
        if (cars.isEmpty()) {
            out.println("No cars yet. Add one with: osb add-car <vin> <plate> <make> <model> <year>");
            return;
        }
        for (Car car : cars) {
            out.println(describe(car));
        }
    }

    private void services(String[] args) throws IOException {
        requireArgumentCount(args, 2, "osb services <vin-or-plate>");
        Car car = findCar(file.load(), args[1]);
        List<Service> services = car.getServices();
        if (services.isEmpty()) {
            out.println("No services recorded for " + describe(car));
            return;
        }
        for (Service service : services) {
            out.printf("%s  %8d  %10s  %s%n",
                    service.getDate(), service.getMileage(), service.getCost(), service.getDescription());
        }
    }

    private static Car findCar(ServiceBook book, String vinOrPlate) {
        return book.findCar(vinOrPlate)
                .orElseThrow(() -> new IllegalArgumentException("no car with VIN or plate " + vinOrPlate));
    }

    private static String describe(Car car) {
        return car.getMake() + " " + car.getModel() + " (" + car.getModelYear() + "), plate " + car.getPlate()
                + ", VIN " + car.getVin();
    }

    private static void requireArgumentCount(String[] args, int count, String usage) {
        if (args.length != count) {
            throw new IllegalArgumentException("usage: " + usage);
        }
    }

    private static Year parseYear(String text) {
        try {
            return Year.of(Integer.parseInt(text));
        } catch (NumberFormatException | DateTimeException e) {
            throw new IllegalArgumentException("year must be a number like 1999, got: " + text);
        }
    }

    private static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("date must look like 2026-09-01, got: " + text);
        }
    }

    private static int parseMileage(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("mileage must be a whole number, got: " + text);
        }
    }
}
