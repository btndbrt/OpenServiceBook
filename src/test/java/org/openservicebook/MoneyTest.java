package org.openservicebook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void printsTwoDecimals() {
        assertEquals("89.90", Money.parse("89.90").toString());
        assertEquals("89.90", Money.parse("89.9").toString());
        assertEquals("90.00", Money.parse("90").toString());
        assertEquals("22.05", Money.parse("22.05").toString());
        assertEquals("0.00", Money.parse("0").toString());
    }

    @Test
    void ignoresSurroundingWhitespace() {
        assertEquals(Money.parse("89.90"), Money.parse(" 89.90 "));
    }

    @Test
    void amountsWithDifferentDecimalsAreEqual() {
        Money a = Money.parse("90");
        Money b = Money.parse("90.00");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(Money.parse("90.0"), b);
    }

    @Test
    void differentAmountsAreNotEqual() {
        assertNotEquals(Money.parse("22.5"), Money.parse("22.05"));
    }

    @Test
    void acceptsLargestAmount() {
        assertEquals("21474836.47", Money.parse("21474836.47").toString());
    }

    @Test
    void rejectsAmountsThatAreTooLarge() {
        assertThrows(IllegalArgumentException.class, () -> Money.parse("21474836.48"));
        assertThrows(IllegalArgumentException.class, () -> Money.parse("99999999999999999999"));
    }

    @Test
    void rejectsInvalidText() {
        assertThrows(IllegalArgumentException.class, () -> Money.parse("-5.00"));
        assertThrows(IllegalArgumentException.class, () -> Money.parse("89.999"));
        assertThrows(IllegalArgumentException.class, () -> Money.parse("89,90"));
        assertThrows(IllegalArgumentException.class, () -> Money.parse("1.2.3"));
        assertThrows(IllegalArgumentException.class, () -> Money.parse(".90"));
        assertThrows(IllegalArgumentException.class, () -> Money.parse("90."));
        assertThrows(IllegalArgumentException.class, () -> Money.parse("89.x"));
        assertThrows(IllegalArgumentException.class, () -> Money.parse("abc"));
        assertThrows(IllegalArgumentException.class, () -> Money.parse(""));
    }

    @Test
    void rejectsNull() {
        assertThrows(NullPointerException.class, () -> Money.parse(null));
    }
}
