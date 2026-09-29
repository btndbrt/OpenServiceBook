package org.openservicebook;

public final class OpenServiceBook {

    private OpenServiceBook() {
    }

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(ServiceBookFile.inHomeFolder(), System.out, System.err);
        System.exit(commandLine.run(args));
    }
}
