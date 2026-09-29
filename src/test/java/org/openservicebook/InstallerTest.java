package org.openservicebook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class InstallerTest {

    @TempDir
    Path folder;

    // A stand-in for the real jar: the installer only copies it, so any file works.
    private Path fakeJar(String content) throws IOException {
        Path jar = folder.resolve("program.jar");
        Files.writeString(jar, content);
        return jar;
    }

    @Test
    void copiesTheJarIntoTheAppFolder() throws IOException {
        Installer installer = new Installer(folder.resolve("app"), folder.resolve("bin"));

        installer.install(fakeJar("version 1"));

        assertEquals("version 1", Files.readString(folder.resolve("app").resolve("osb.jar")));
    }

    @Test
    void writesAnExecutableLauncherThatRunsTheCopiedJar() throws IOException {
        Installer installer = new Installer(folder.resolve("app"), folder.resolve("bin"));

        installer.install(fakeJar("version 1"));

        Path launcher = folder.resolve("bin").resolve("osb");
        Path installedJar = folder.resolve("app").resolve("osb.jar");
        assertEquals("#!/bin/sh\nexec java -jar \"" + installedJar + "\" \"$@\"\n", Files.readString(launcher));
        assertTrue(Files.isExecutable(launcher));
    }

    @Test
    void installingAgainReplacesTheOldCopy() throws IOException {
        Installer installer = new Installer(folder.resolve("app"), folder.resolve("bin"));
        installer.install(fakeJar("version 1"));

        installer.install(fakeJar("version 2"));

        assertEquals("version 2", Files.readString(folder.resolve("app").resolve("osb.jar")));
    }

    @Test
    void refusesAFolderInsteadOfAJar() {
        Installer installer = new Installer(folder.resolve("app"), folder.resolve("bin"));

        assertThrows(IllegalArgumentException.class, () -> installer.install(folder));
    }
}
