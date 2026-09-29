package org.openservicebook;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Saves a service book as plain text, one tab-separated record per line.
 * Each SERVICE line belongs to the CAR line above it:
 *
 * <pre>
 * CAR      vin   make   model   modelYear
 * SERVICE  date  mileage  cost  description
 * </pre>
 */
public final class ServiceBookFile {

    private static final String CAR = "CAR";
    private static final String SERVICE = "SERVICE";
    private static final String SEPARATOR = "\t";

    private final Path path;

    public ServiceBookFile(Path path) {
        this.path = Objects.requireNonNull(path, "path");
    }

    public static ServiceBookFile inHomeFolder() {
        return new ServiceBookFile(Path.of(System.getProperty("user.home"), ".openservicebook", "servicebook.txt"));
    }

    public ServiceBook load() throws IOException {
        ServiceBook book = new ServiceBook();
        if (!Files.exists(path)) {
            return book;
        }

        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        Car currentCar = null;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split(SEPARATOR, -1);
            try {
                if (fields[0].equals(CAR) && fields.length == 5) {
                    currentCar = new Car(fields[1], fields[2], fields[3], Year.of(Integer.parseInt(fields[4])));
                    book.addCar(currentCar);
                } else if (fields[0].equals(SERVICE) && fields.length == 5) {
                    if (currentCar == null) {
                        throw new IllegalArgumentException("service comes before any car");
                    }
                    currentCar.addService(new Service(
                            LocalDate.parse(fields[1]), Integer.parseInt(fields[2]), fields[4], Money.parse(fields[3])));
                } else {
                    throw new IllegalArgumentException("unrecognised line");
                }
            } catch (IllegalArgumentException | DateTimeException e) {
                throw new IOException(path + " line " + (i + 1) + ": " + e.getMessage(), e);
            }
        }
        return book;
    }

    public void save(ServiceBook book) throws IOException {
        Objects.requireNonNull(book, "book");
        List<String> lines = new ArrayList<>();
        for (Car car : book.getCars()) {
            lines.add(String.join(SEPARATOR, CAR, car.getVin(), car.getMake(), car.getModel(),
                    Integer.toString(car.getModelYear().getValue())));
            for (Service service : car.getServices()) {
                lines.add(String.join(SEPARATOR, SERVICE, service.getDate().toString(),
                        Integer.toString(service.getMileage()), service.getCost().toString(), service.getDescription()));
            }
        }

        Path folder = path.toAbsolutePath().getParent();
        Files.createDirectories(folder);

        // Write everything to a temporary file first, then swap it in with a single rename.
        // If the program dies mid-write, only the temporary file is damaged; the real file is untouched.
        Path temporary = folder.resolve(path.getFileName() + ".tmp");
        Files.write(temporary, lines, StandardCharsets.UTF_8);
        Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }
}
