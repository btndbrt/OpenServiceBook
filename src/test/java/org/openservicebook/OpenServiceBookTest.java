package org.openservicebook;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class OpenServiceBookTest {

    @Test
    void greetingNamesTheApplication() {
        assertEquals("OpenServiceBook", OpenServiceBook.greeting());
    }
}
