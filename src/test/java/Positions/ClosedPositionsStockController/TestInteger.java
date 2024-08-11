package Positions.ClosedPositionsStockController;

import static org.junit.Assert.assertTrue;

class TestInteger {
        private final Integer enumInteger;
        private final Integer actualInteger;
        private final Integer expectedInteger;

        String message = "'%s' is not the expected value '%s'";

        public TestInteger(Integer enumInteger, Integer actualInteger, Integer expectedInteger) {
            this.enumInteger = enumInteger;
            this.actualInteger = actualInteger;
            this.expectedInteger = expectedInteger;
        }

        public void doTest() {
            assertTrue(enumInteger.toString() + ": " + String.format(message, actualInteger, expectedInteger),
                actualInteger.equals(expectedInteger));
        }
    }
