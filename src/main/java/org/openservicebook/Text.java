package org.openservicebook;

import java.util.Locale;
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

    // Loose form used to compare VINs and plates: "abc 123", "ABC-123" and "ABC123" all become "ABC123".
    static String lookupKey(String value) {
        return value.replace(" ", "").replace("-", "").toUpperCase(Locale.ROOT);
    }
}
