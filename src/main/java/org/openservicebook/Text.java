package org.openservicebook;

import java.util.Objects;

final class Text {

    private Text() {
    }

    // Tabs and line breaks are forbidden because the save file uses them to separate fields and records.
    static String requireSingleLine(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        if (value.contains("\t") || value.contains("\n") || value.contains("\r")) {
            throw new IllegalArgumentException(name + " must not contain tabs or line breaks");
        }
        return value.strip();
    }
}
