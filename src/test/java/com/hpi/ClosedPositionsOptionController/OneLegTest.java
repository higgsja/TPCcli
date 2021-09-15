package com.hpi.ClosedPositionsOptionController;

import com.hpi.TPCCMcontrollers.*;
import com.hpi.TPCCMprefs.*;
import com.hpi.appcontrollers.*;
import com.hpi.appcontrollers.positions.ClosedPositionsOptionController;
import com.hpi.entities.*;
import com.hpi.hpiUtils.CMHPIUtils;
import java.sql.Date;
import lombok.*;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@AllArgsConstructor
public class OneLegTest {

    private static ClosedPositionsOptionController cpoc;

//    public OneLegTest() {
//    }
    @BeforeClass
    public static void setUpClass() {
        CMDBModel.getInstance();
        CMDBModel.setUserId(816);
        ClosedPositionsOptionController.getInstance();
        cpoc = ClosedPositionsOptionController.getInstance();
        CMDBController.initDBConnection();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    
    @Ignore
    @Test
    public void clearPositions(){
        DbOfxController.getInstance().doClearPositions();
    }

    @Ignore
    @Test
    public void OneLegSingleTransaction() {
        //use this as template to construct more tests
        FIFOClosedTransactionModel fctm;
        fctm = FIFOClosedTransactionModel.builder()
                .dmAcctId(1)
            .dmAcctId(1)
            .joomlaId(816)
            .fiTId("210526_3864_0")
            .transactionGrp(5201)
            .equityId("NCLH  210618C00030500")
            .ticker("NCLH")
            .dateOpen(Date.valueOf("2021-05-26"))
            .dateClose(Date.valueOf("2021-05-28"))
            .units(-2.0)
            .priceOpen(2.19)
            .priceClose(-2.43)
            .dateExpire(Date.valueOf("2021-06-18"))
            .positionType("LONG")
            .totalOpen(-438.23)
            .totalClose(485.76)
            .equityType("OPTION")
            .gainPct(10.8459)
            .transactionType("BUYTOOPEN")
            .bComplete(false)
            .build();

        cpoc.getFifoClosedTransactionModels().add(fctm);

        cpoc.doFctm2Pctm();

        cpoc.doPctm2Pcm();

        cpoc.doPositionsTacticId();

        cpoc.doPositionName();

        assertEquals("Position legs not the expected value of '1.0'",
            cpoc.getPositionClosedTransactionModels().size(), 1.0, 0.001);

        assertEquals("Position units not the expected value of '-2.0'",
            cpoc.getPositionClosedModels().get(0).getUnits(), -2.0, 0.001);

        assertTrue("Position Ticker not the expected value of 'NCLH'",
            cpoc.getPositionClosedModels().get(0).getTicker().equalsIgnoreCase("nclh"));

        assertTrue("TacticId is not the expected value of 'LONG'",
            cpoc.getPositionClosedModels().get(0).getTacticId().equals(PositionOpenModel.TACTICID_LONG));

        assertEquals("Position PriceOpen '" + cpoc.getPositionClosedModels().get(0).getPriceOpen()
                         + "' not the expected value of '2.19115'",
            cpoc.getPositionClosedModels().get(0).getPriceOpen(), 2.19115, .001);

        assertEquals("Position Price '" + cpoc.getPositionClosedModels().get(0).getPrice()
                         + "' not the expected value of '-2.4288'",
            cpoc.getPositionClosedModels().get(0).getPrice(), -2.4288, .001);

        assertEquals("Position Gain not the expected value of '47.53'",
            //                cpoc.getPositionClosedModels().get(0).getGain(), 979.52, 0.001);
            cpoc.getPositionClosedModels().get(0).getGain(), 47.53, 0.001);

        assertEquals("Position GainPct not the expected value of '10.8459'",
            //                cpoc.getPositionClosedModels().get(0).getGain(), 979.52, 0.001);
            cpoc.getPositionClosedModels().get(0).getGainPct(), 10.8459, 0.001);

        assertTrue("Position GMTDtTradeOpen '" + cpoc.getPositionClosedModels().get(0).getDateOpen().toString()
                       + "' not the expected value of '2021-05-26T00:00'",
            cpoc.getPositionClosedModels().get(0).getDateOpen()
                .equals(CMHPIUtils.convertStringToLocalDateTime("2021-05-26 00:00:00")));

        assertTrue("Position GMTDtTradeClose '" + cpoc.getPositionClosedModels().get(0).getDateClose().toString()
                       + "' not the expected value of '2021-05-28T00:00'",
            cpoc.getPositionClosedModels().get(0).getDateClose()
                .equals(CMHPIUtils.convertStringToLocalDateTime("2021-05-28 00:00:00")));

        assertTrue("Position name is not the expected value of 'NCLH 18Jun21 30.5 Call'",
            cpoc.getPositionClosedModels().get(0).getPositionName()
                .equalsIgnoreCase("NCLH 18Jun21 30.5 Call"));
        //do not leave uncommented
//        cpoc.doSQL();
    }

    @Ignore
    @Test
    public void OneLeg2TransactionsA() {
        //transaction is a same day buy sell
        FIFOClosedTransactionModel fctm;
        fctm = FIFOClosedTransactionModel.builder()
            .dmAcctId(1)
            .joomlaId(816)
            .fiTId("201111_4404_0")
            .transactionGrp(5254)
            .equityId("AAPL  210219C00115000")
            .ticker("AAPL")
            .dateOpen(Date.valueOf("2020-11-11"))
            .dateClose(Date.valueOf("2020-11-11"))
            .units(3.0)
            .priceOpen(10.1)
            .priceClose(-10.17)
            .dateExpire(Date.valueOf("2021-02-19"))
            .positionType("SHORT")
            .totalOpen(3029.58)
            .totalClose(-3051.34)
            .equityType("OPTION")
            .gainPct(-0.7183)
            .transactionType("SELLTOOPEN")
            .bComplete(false)
            .build();

        cpoc.getFifoClosedTransactionModels().add(fctm);

        cpoc.doFctm2Pctm();

        cpoc.doPctm2Pcm();

        cpoc.doPositionsTacticId();

        assertTrue("Positions count not the expected value of '1'",
            cpoc.getPositionClosedModels().size() == 1);

        assertEquals("Position legs not the expected value of '1.0'",
            cpoc.getPositionClosedTransactionModels().size(), 1.0, 0.001);

        assertEquals("Position units not the expected value of '3.0'",
            cpoc.getPositionClosedModels().get(0).getUnits(), 3.0, 0.001);

        assertTrue("Position Ticker not the expected value of 'AAPL'",
            cpoc.getPositionClosedModels().get(0).getTicker().equalsIgnoreCase("aapl"));

        assertTrue("TacticId is not the expected value of 'SHORT'",
            cpoc.getPositionClosedModels().get(0).getTacticId().equals(PositionOpenModel.TACTICID_SHORT));

        assertEquals("Position PriceOpen '" + cpoc.getPositionClosedModels().get(0).getPriceOpen()
                         + "' not the expected value of '10.0986'",
            cpoc.getPositionClosedModels().get(0).getPriceOpen(), 10.0986, .001);

        assertEquals("Position Price '" + cpoc.getPositionClosedModels().get(0).getPrice()
                         + "' not the expected value of '-10.1711'",
            cpoc.getPositionClosedModels().get(0).getPrice(), -10.1711, .001);

        assertEquals("Position Gain not the expected value of '-21.76'",
            //                cpoc.getPositionClosedModels().get(0).getGain(), 979.52, 0.001);
            cpoc.getPositionClosedModels().get(0).getGain(), -21.76, 0.001);

        assertEquals("Position GainPct not the expected value of '-0.71825'",
            //                cpoc.getPositionClosedModels().get(0).getGain(), 979.52, 0.001);
            cpoc.getPositionClosedModels().get(0).getGainPct(), -0.71825, 0.001);

        assertTrue("Position GMTDtTradeOpen '" + cpoc.getPositionClosedModels().get(0).getDateOpen().toString()
                       + "' not the expected value of '2020-11-11T00:00'",
            cpoc.getPositionClosedModels().get(0).getDateOpen()
                .equals(CMHPIUtils.convertStringToLocalDateTime("2020-11-11 00:00:00")));

        assertTrue("Position GMTDtTradeClose '" + cpoc.getPositionClosedModels().get(0).getDateClose().toString()
                       + "' not the expected value of '2020-11-11T00:00'",
            cpoc.getPositionClosedModels().get(0).getDateClose()
                .equals(CMHPIUtils.convertStringToLocalDateTime("2020-11-11 00:00:00")));

        cpoc.doPositionName();

        assertTrue("Position name is not the expected value of 'AAPL 19Feb21 115.0 Call'",
            cpoc.getPositionClosedModels().get(0).getPositionName()
                .equalsIgnoreCase("AAPL 19Feb21 115.0 Call"));
        //do not leave uncommented
//        cpoc.doSQL();
    }

    @Ignore
    @Test
    public void OneLeg2TransactionsB() {
        //first 2 transactions same day buy sell
        //second is additional short position in the 
        //  same equity on the same day
        FIFOClosedTransactionModel fctm;
        fctm = FIFOClosedTransactionModel.builder()
            .dmAcctId(1)
            .joomlaId(816)
            .fiTId("201111_4404_0")
            .transactionGrp(5254)
            .equityId("AAPL  210219C00115000")
            .ticker("AAPL")
            .dateOpen(Date.valueOf("2020-11-11"))
            .dateClose(Date.valueOf("2020-11-11"))
            .units(3.0)
            .priceOpen(10.1)
            .priceClose(-10.17)
            .dateExpire(Date.valueOf("2021-02-19"))
            .positionType("SHORT")
            .totalOpen(3029.58)
            .totalClose(-3051.34)
            .equityType("OPTION")
            .gainPct(-0.7183)
            .transactionType("SELLTOOPEN")
            .bComplete(false)
            .build();

        cpoc.getFifoClosedTransactionModels().add(fctm);

        fctm = FIFOClosedTransactionModel.builder()
            .dmAcctId(1)
            .joomlaId(816)
            .fiTId("201111_4404_01")
            .transactionGrp(5255)
            .equityId("AAPL  210219C00115000")
            .ticker("AAPL")
            .dateOpen(Date.valueOf("2020-11-11"))
            .dateClose(Date.valueOf("2020-11-11"))
            .units(3.0)
            .priceOpen(10.1)
            .priceClose(-10.17)
            .dateExpire(Date.valueOf("2021-02-19"))
            .positionType("SHORT")
            .totalOpen(3029.58)
            .totalClose(-3051.34)
            .equityType("OPTION")
            .gainPct(-0.7183)
            .transactionType("SELLTOOPEN")
            .bComplete(false)
            .build();

        cpoc.getFifoClosedTransactionModels().add(fctm);

        fctm = FIFOClosedTransactionModel.builder()
            .dmAcctId(1)
            .joomlaId(816)
            .fiTId("201111_4406_0")
            .transactionGrp(5253)
            .equityId("AAPL  210219C00115000")
            .ticker("AAPL")
            .dateOpen(Date.valueOf("2020-11-11"))
            .dateClose(Date.valueOf("2020-12-17"))
            .units(-3.0)
            .priceOpen(10.15)
            .priceClose(-16.9)
            .dateExpire(Date.valueOf("2021-02-19"))
            .positionType("LONG")
            .totalOpen(-3045.34)
            .totalClose(5069.53)
            .equityType("OPTION")
            .gainPct(66.4684)
            .transactionType("BUYTOOPEN")
            .bComplete(false)
            .build();

        cpoc.getFifoClosedTransactionModels().add(fctm);

        cpoc.doFctm2Pctm();

        cpoc.doPctm2Pcm();

        cpoc.doPositionsTacticId();

        cpoc.doPositionName();

        String[][] stringTests = {
            {"a", cpoc.getPositionClosedModels().get(0).getPositionName(), "AAPL 19Feb21 115.0 Call"},
            {"b", cpoc.getPositionClosedModels().get(1).getPositionName(), "AAPL 19Feb21 115.0 Call"},
            {"c", cpoc.getPositionClosedModels().get(0).getTicker(), "aapl"},
            {"d", cpoc.getPositionClosedModels().get(0).getPositionName(), "AAPL 19Feb21 115.0 Call"},
            {"e", cpoc.getPositionClosedModels().get(1).getPositionName(), "AAPL 19Feb21 115.0 Call"}

        };

        Integer[][] integerTests = {
            {1, cpoc.getPositionClosedTransactionModels().size(), 2},
            {2, cpoc.getPositionClosedModels().size(), 2},
            {11, cpoc.getPositionClosedModels().get(0).getTacticId(), PositionOpenModel.TACTICID_SHORT},
            {21, cpoc.getPositionClosedModels().get(1).getTacticId(), PositionOpenModel.TACTICID_LONG}

        };

        Double[][] doubleTests = {
            {1.0, cpoc.getPositionClosedModels().get(0).getUnits(), 6.0},
            {2.0, cpoc.getPositionClosedModels().get(0).getPriceOpen(), 10.0986},
            {3.0, cpoc.getPositionClosedModels().get(0).getPrice(), -10.1711},
            {4.0, cpoc.getPositionClosedModels().get(0).getGain(), -43.52},
            {5.0, cpoc.getPositionClosedModels().get(0).getGainPct(), -0.71825},
            {11.0, cpoc.getPositionClosedModels().get(1).getUnits(), -3.0},
            {12.0, cpoc.getPositionClosedModels().get(1).getPriceOpen(), 10.1511},
            {13.0, cpoc.getPositionClosedModels().get(1).getPrice(), -16.8984},
            {14.0, cpoc.getPositionClosedModels().get(1).getGain(), 2024.19},
            {15.0, cpoc.getPositionClosedModels().get(1).getGainPct(), 66.4684}

        };

        for (String[] test : stringTests) {
            (new StringTest(test[0], test[1], test[2])).doTest();
        }

        for (Integer[] test : integerTests) {
            (new IntegerTest(test[0], test[1], test[2])).doTest();
        }

        for (Double[] test : doubleTests) {
            (new DoubleTest(test[0], test[1], test[2])).doTest();
        }

        assertTrue("Position GMTDtTradeOpen '" + cpoc.getPositionClosedModels().get(0).getDateOpen().toString()
                       + "' not the expected value of '2020-11-11T00:00'",
            cpoc.getPositionClosedModels().get(0).getDateOpen()
                .equals(CMHPIUtils.convertStringToLocalDateTime("2020-11-11 00:00:00")));

        assertTrue("Position GMTDtTradeClose '" + cpoc.getPositionClosedModels().get(0).getDateClose().toString()
                       + "' not the expected value of '2020-11-11T00:00'",
            cpoc.getPositionClosedModels().get(0).getDateClose()
                .equals(CMHPIUtils.convertStringToLocalDateTime("2020-11-11 00:00:00")));

        assertTrue("Position GMTDtTradeOpen '" + cpoc.getPositionClosedModels().get(0).getDateOpen().toString()
                       + "' not the expected value of '2020-11-11T00:00'",
            cpoc.getPositionClosedModels().get(1).getDateOpen()
                .equals(CMHPIUtils.convertStringToLocalDateTime("2020-11-11 00:00:00")));

        assertTrue("Position GMTDtTradeClose '" + cpoc.getPositionClosedModels().get(0).getDateClose().toString()
                       + "' not the expected value of '2020-11-11T00:00'",
            cpoc.getPositionClosedModels().get(1).getDateClose()
                .equals(CMHPIUtils.convertStringToLocalDateTime("2020-12-17 00:00:00")));

        //do not leave uncommented
//        cpoc.doSQL();
    }

    private class StringTest {
        private final String enumString;
        private final String actual;
        private final String expected;

        String message = "'%s' is not the expected value '%s'";

        public StringTest(String enumString, String actual, String expected) {
            this.enumString = enumString;
            this.actual = actual;
            this.expected = expected;
        }

        public void doTest() {
            assertTrue(enumString + ": " + String.format(message, actual, expected),
                actual.equalsIgnoreCase(expected));
        }
    }

    private class IntegerTest {
        private final Integer enumInteger;
        private final Integer actualInteger;
        private final Integer expectedInteger;

        String message = "'%s' is not the expected value '%s'";

        public IntegerTest(Integer enumInteger, Integer actualInteger, Integer expectedInteger) {
            this.enumInteger = enumInteger;
            this.actualInteger = actualInteger;
            this.expectedInteger = expectedInteger;
        }

        public void doTest() {
            assertTrue(enumInteger.toString() + ": " + String.format(message, actualInteger, expectedInteger),
                actualInteger.equals(expectedInteger));
        }
    }

    private class DoubleTest {
        private final Double enumDouble;
        private final Double actualDouble;
        private final Double expectedDouble;

        String message = "'%s' is not the expected value '%s'";

        public DoubleTest(Double enumDouble, Double actualDouble, Double expectedDouble) {
            this.enumDouble = enumDouble;
            this.actualDouble = actualDouble;
            this.expectedDouble = expectedDouble;
        }

        public void doTest() {
            assertEquals(enumDouble.toString() + ": " + String.format(message, actualDouble, expectedDouble),
                actualDouble, expectedDouble, 0.001);
        }
    }
}
