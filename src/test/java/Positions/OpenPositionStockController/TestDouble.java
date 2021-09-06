package Positions.OpenPositionStockController;

import static org.junit.Assert.assertEquals;

class TestDouble {
        private final Double enumDouble;
        private final Double actualDouble;
        private final Double expectedDouble;

        String message = "'%s' is not the expected value '%s'";

        public TestDouble(Double enumDouble, Double actualDouble, Double expectedDouble) {
            this.enumDouble = enumDouble;
            this.actualDouble = actualDouble;
            this.expectedDouble = expectedDouble;
        }

        public void doTest() {
            assertEquals(enumDouble.toString() + ": " + String.format(message, actualDouble, expectedDouble),
                actualDouble, expectedDouble, 0.001);
        }
    }