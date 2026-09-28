package org.openservicebook;

public final class OpenServiceBook {

    private OpenServiceBook() {
    }

    public static void main(String[] args) {
        System.out.println(greeting());
    }

    static String greeting() {
        return "OpenServiceBook";
    }
}
