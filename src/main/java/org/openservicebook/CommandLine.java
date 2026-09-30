package org.openservicebook;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
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
            "  osb services <vin-or-plate>",
            "  osb edit-car <vin-or-plate> <field> <new-value>",
            "  osb edit-service <vin-or-plate> <number> <field> <new-value>",
            "  osb delete-car <vin-or-plate>",
            "  osb delete-service <vin-or-plate> <number>",
            "  osb install",
            "",
            "car fields: vin, plate, make, model, year",
            "service fields: date, mileage, cost, description",
            "service numbers are the ones shown by: osb services <vin-or-plate>");

    private final ServiceBookFile file;
    private final Installer installer;
    private final PrintStream out;
    private final PrintStream err;

    public CommandLine(ServiceBookFile file, Installer installer, PrintStream out, PrintStream err) {
        this.file = Objects.requireNonNull(file, "file");
        this.installer = Objects.requireNonNull(installer, "installer");
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
                case "edit-car" -> editCar(args);
                case "edit-service" -> editService(args);
                case "delete-car" -> deleteCar(args);
                case "delete-service" -> deleteService(args);
                case "install" -> install(args);
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
        // The number in front of each service is what edit-service and delete-service use to pick it.
        for (int i = 0; i < services.size(); i++) {
            out.printf("%3d  %s%n", i + 1, describe(services.get(i)));
        }
    }

    private void editCar(String[] args) throws IOException {
        if (args.length < 4) {
            throw new IllegalArgumentException("usage: osb edit-car <vin-or-plate> <field> <new-value>");
        }
        String field = args[2];
        String value = String.join(" ", Arrays.copyOfRange(args, 3, args.length));

        ServiceBook book = file.load();
        Car old = findCar(book, args[1]);
        // A Car can't be changed, so build a new one that copies every field except the edited one.
        Car edited = switch (field) {
            case "vin" -> new Car(value, old.getPlate(), old.getMake(), old.getModel(), old.getModelYear());
            case "plate" -> new Car(old.getVin(), value, old.getMake(), old.getModel(), old.getModelYear());
            case "make" -> new Car(old.getVin(), old.getPlate(), value, old.getModel(), old.getModelYear());
            case "model" -> new Car(old.getVin(), old.getPlate(), old.getMake(), value, old.getModelYear());
            case "year" -> new Car(old.getVin(), old.getPlate(), old.getMake(), old.getModel(), parseYear(value));
            default -> throw new IllegalArgumentException(
                    "unknown car field: " + field + " (use vin, plate, make, model or year)");
        };
        for (Service service : old.getServices()) {
            edited.addService(service);
        }
        book.replaceCar(old, edited);
        file.save(book);
        out.println("Updated " + describe(edited));
    }

    private void editService(String[] args) throws IOException {
        if (args.length < 5) {
            throw new IllegalArgumentException("usage: osb edit-service <vin-or-plate> <number> <field> <new-value>");
        }
        String field = args[3];
        String value = String.join(" ", Arrays.copyOfRange(args, 4, args.length));

        ServiceBook book = file.load();
        Car car = findCar(book, args[1]);
        int index = parseServiceNumber(args[2], car);
        Service old = car.getServices().get(index);
        // Same idea as editCar: a new Service with one field changed.
        Service edited = switch (field) {
            case "date" -> new Service(parseDate(value), old.getMileage(), old.getDescription(), old.getCost());
            case "mileage" -> new Service(old.getDate(), parseMileage(value), old.getDescription(), old.getCost());
            case "cost" -> new Service(old.getDate(), old.getMileage(), old.getDescription(), Money.parse(value));
            case "description" -> new Service(old.getDate(), old.getMileage(), value, old.getCost());
            default -> throw new IllegalArgumentException(
                    "unknown service field: " + field + " (use date, mileage, cost or description)");
        };
        car.replaceService(index, edited);
        file.save(book);
        out.println("Updated service " + args[2] + " of " + describe(car) + ":");
        out.println(describe(edited));
    }

    private void deleteCar(String[] args) throws IOException {
        requireArgumentCount(args, 2, "osb delete-car <vin-or-plate>");
        ServiceBook book = file.load();
        Car car = findCar(book, args[1]);
        book.removeCar(car);
        file.save(book);
        out.println("Deleted " + describe(car) + " and its " + car.getServices().size() + " service(s)");
    }

    private void deleteService(String[] args) throws IOException {
        requireArgumentCount(args, 3, "osb delete-service <vin-or-plate> <number>");
        ServiceBook book = file.load();
        Car car = findCar(book, args[1]);
        int index = parseServiceNumber(args[2], car);
        Service service = car.getServices().get(index);
        car.removeService(index);
        file.save(book);
        out.println("Deleted service from " + describe(car) + ":");
        out.println(describe(service));
    }

    private void install(String[] args) throws IOException {
        requireArgumentCount(args, 1, "osb install");
        installer.install(runningJar());
        out.println("Installed. You can now run: osb help");
        out.println("(If \"osb\" is not found, add ~/.local/bin to your PATH.)");
    }

    // The file the OpenServiceBook class was loaded from: the jar, when started with java -jar.
    private static Path runningJar() {
        try {
            return Path.of(OpenServiceBook.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("could not find the program's jar file", e);
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

    private static String describe(Service service) {
        return String.format("%s  %8d  %10s  %s",
                service.getDate(), service.getMileage(), service.getCost(), service.getDescription());
    }

    // Turns the number shown by "osb services" (counting from 1) into a list index (counting from 0).
    private static int parseServiceNumber(String text, Car car) {
        int number;
        try {
            number = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("service number must be a whole number, got: " + text);
        }
        int count = car.getServices().size();
        if (number < 1 || number > count) {
            throw new IllegalArgumentException("no service number " + number + " for " + describe(car)
                    + " (it has " + count + "; see osb services " + car.getPlate() + ")");
        }
        return number - 1;
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
