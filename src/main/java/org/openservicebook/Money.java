package org.openservicebook;

import java.util.Locale;
import java.util.Objects;

public final class Money {

    // Stored as whole cents so amounts are exact; never exposed outside this class.
    private final int cents;

    private Money(int cents) {
        this.cents = cents;
    }

    public static Money parse(String text) {
        Objects.requireNonNull(text, "text");
        String[] parts = text.strip().split("\\.", -1);
        if (parts.length > 2) {
            throw invalid(text);
        }

        String whole = parts[0];
        String fraction = parts.length == 2 ? parts[1] : "";
        if (!isDigits(whole)) {
            throw invalid(text);
        }
        if (parts.length == 2 && (fraction.length() > 2 || !isDigits(fraction))) {
            throw invalid(text);
        }
        if (fraction.length() == 1) {
            fraction += "0";
        }

        try {
            int wholeCents = Math.multiplyExact(Integer.parseInt(whole), 100);
            int fractionCents = fraction.isEmpty() ? 0 : Integer.parseInt(fraction);
            return new Money(Math.addExact(wholeCents, fractionCents));
        } catch (NumberFormatException | ArithmeticException e) {
            throw new IllegalArgumentException("amount is too large: " + text, e);
        }
    }

    private static boolean isDigits(String s) {
        if (s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private static IllegalArgumentException invalid(String text) {
        return new IllegalArgumentException("not a valid amount: " + text);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Money other && cents == other.cents;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(cents);
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%d.%02d", cents / 100, cents % 100);
    }
}
