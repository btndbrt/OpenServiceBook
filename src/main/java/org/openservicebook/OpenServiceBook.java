package org.openservicebook;

import java.nio.file.Path;

public final class OpenServiceBook {

    private OpenServiceBook() {
    }

    public static void main(String[] args) {
        Path home = Path.of(System.getProperty("user.home"));
        Installer installer = new Installer(home.resolve(".openservicebook"), home.resolve(".local").resolve("bin"));
        CommandLine commandLine = new CommandLine(ServiceBookFile.inHomeFolder(), installer, System.out, System.err);
        System.exit(commandLine.run(args));
    }
}
