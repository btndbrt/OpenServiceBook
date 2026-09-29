package org.openservicebook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * Installs an {@code osb} command: copies the program's jar into the app folder and writes a small
 * shell script called {@code osb} into the bin folder that runs it.
 */
public final class Installer {

    private final Path appFolder;
    private final Path binFolder;

    public Installer(Path appFolder, Path binFolder) {
        this.appFolder = Objects.requireNonNull(appFolder, "appFolder");
        this.binFolder = Objects.requireNonNull(binFolder, "binFolder");
    }

    public void install(Path jar) throws IOException {
        if (!Files.isRegularFile(jar)) {
            throw new IllegalArgumentException("install only works when running from the jar file, not " + jar);
        }

        // Step 1: copy the jar
        Files.createDirectories(appFolder);
        Path installedJar = appFolder.resolve("osb.jar");
        Files.copy(jar, installedJar, StandardCopyOption.REPLACE_EXISTING);

        // Step 2: write the osb file
        Files.createDirectories(binFolder);
        Path launcher = binFolder.resolve("osb");
        Files.writeString(launcher, "#!/bin/sh\nexec java -jar \"" + installedJar + "\" \"$@\"\n");
        launcher.toFile().setExecutable(true);
    }
}
