package com.hpi.ClosedPositionsOptionController;

import static org.junit.Assert.assertTrue;

class TestString {
        private final String enumString;
        private final String actual;
        private final String expected;

        String message = "'%s' is not the expected value '%s'";

        public TestString(String enumString, String actual, String expected) {
            this.enumString = enumString;
            this.actual = actual;
            this.expected = expected;
        }

        public void doTest() {
            assertTrue(enumString + ": " + String.format(message, actual, expected),
                actual.equalsIgnoreCase(expected));
        }
    }
