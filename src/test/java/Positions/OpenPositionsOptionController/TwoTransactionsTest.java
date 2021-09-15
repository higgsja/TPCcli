package Positions.OpenPositionsOptionController;

import com.hpi.TPCCMcontrollers.*;
import com.hpi.TPCCMprefs.*;
import com.hpi.appcontrollers.positions.OpenPositionsOptionController;
import com.hpi.entities.*;
import com.hpi.hpiUtils.CMHPIUtils;
import java.sql.Date;
import lombok.*;
import org.junit.*;
import static org.junit.Assert.assertTrue;

@AllArgsConstructor
public class TwoTransactionsTest {

    private static OpenPositionsOptionController opoc;

    @BeforeClass
    public static void setUpClass() {
        CMDBModel.getInstance();
        CMDBModel.setUserId(816);
        opoc = OpenPositionsOptionController.getInstance();
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
    public void TwoTransA() {
        /**
         * 2 long Calls on the same day, different strike and expiration
         * Second is 2 transactions
         */
        FIFOOpenTransactionModel fotm;
        fotm = FIFOOpenTransactionModel.builder()
            .dmAcctId(2)
            .joomlaId(816)
            .fiTId("210602_3893_1")
            .equityId("NVDA  210820C00645000")
            .ticker("NVDA")
            .optionType("CALL")
            .strikePrice(645.0)
            .dateExpire(Date.valueOf("2021-08-20"))
            .dateOpen(Date.valueOf("2021-06-02"))
            .shPerCtrct(100)
            .units(1.0)
            .priceOpen(59.53)
            .totalOpen(-5953.11)
            .transactionType("BUYTOOPEN")
            .mktVal(8503.00)
            .lMktVal(69828.00)
            .equityType("OPTION")
            .positionType("LONG")
            .actPct(1.2404)
            .gainPct(42.8355)
            .days(72)
            .complete(0)
            .bComplete(false)
            .build();

        opoc.getFifoOpenTransactionModels().add(fotm);

        fotm = FIFOOpenTransactionModel.builder()
            .dmAcctId(2)
            .joomlaId(816)
            .fiTId("210602_3894_0")
            .equityId("NVDA  210917C00605000")
            .ticker("NVDA")
            .optionType("CALL")
            .strikePrice(605.0)
            .dateExpire(Date.valueOf("2021-09-17"))
            .dateOpen(Date.valueOf("2021-06-02"))
            .shPerCtrct(100)
            .units(1.0)
            .priceOpen(87.62)
            .totalOpen(-8762.11)
            .transactionType("BUYTOOPEN")
            .mktVal(12045.00)
            .lMktVal(69828.00)
            .equityType("OPTION")
            .positionType("LONG")
            .actPct(1.2404)
            .gainPct(37.4686)
            .days(100)
            .complete(0)
            .bComplete(false)
            .build();

        opoc.getFifoOpenTransactionModels().add(fotm);

        fotm = FIFOOpenTransactionModel.builder()
            .dmAcctId(2)
            .joomlaId(816)
            .fiTId("210602_B_0")
            .equityId("NVDA  210917C00605000")
            .ticker("NVDA")
            .optionType("CALL")
            .strikePrice(605.0)
            .dateExpire(Date.valueOf("2021-09-17"))
            .dateOpen(Date.valueOf("2021-06-02"))
            .shPerCtrct(100)
            .units(1.0)
            .priceOpen(87.65)
            .totalOpen(-8765.11)
            .transactionType("BUYTOOPEN")
            .mktVal(12045.00)
            .lMktVal(69828.00)
            .equityType("OPTION")
            .positionType("LONG")
            .actPct(1.2404)
            .gainPct(37.4216)
            .days(100)
            .complete(0)
            .bComplete(false)
            .build();

        opoc.getFifoOpenTransactionModels().add(fotm);

        opoc.doFotm2Potm();

        opoc.doPotm2Pom();

        opoc.doPositionsTacticId();

        opoc.doPositionName();

        String[][] stringTests = {
            {"a", opoc.getPositionOpenModels().get(0).getPositionName(), "NVDA 20Aug21 645.0 Call"},
            {"b", opoc.getPositionOpenModels().get(0).getTicker(), "nvda"},
            {"c", opoc.getPositionOpenModels().get(0).getPositionType(), "LONG"},
            {"d", opoc.getPositionOpenModels().get(1).getPositionName(), "NVDA 17Sep21 605.0 Call"},
            {"e", opoc.getPositionOpenModels().get(1).getTicker(), "nvda"},
            {"f", opoc.getPositionOpenModels().get(1).getPositionType(), "LONG"},};

        Integer[][] integerTests = {
            {1, opoc.getPositionOpenTransactionModels().size(), 2},
            {2, opoc.getPositionOpenModels().size(), 2},
            {11, opoc.getPositionOpenModels().get(0).getTacticId(), PositionOpenModel.TACTICID_LONG},
            {12, opoc.getPositionOpenModels().get(0).getDays(), 72},
            {21, opoc.getPositionOpenModels().get(1).getTacticId(), PositionOpenModel.TACTICID_LONG},
            {22, opoc.getPositionOpenModels().get(1).getDays(), 100}

        };

        Double[][] doubleTests = {
            {1.0, opoc.getPositionOpenModels().get(0).getUnits(), 1.0},
            {2.0, opoc.getPositionOpenModels().get(0).getPriceOpen(), -59.5310},
            {3.0, opoc.getPositionOpenModels().get(0).getPrice(), 85.03},
            {4.0, opoc.getPositionOpenModels().get(0).getGainPct(), 42.8329},
            {5.0, opoc.getPositionOpenModels().get(0).getGain(), 2549.89},
            {6.0, opoc.getPositionOpenModels().get(0).getMktVal(), 8503.0},
            {7.0, opoc.getPositionOpenModels().get(0).getLMktVal(), 69828.0},
            {8.0, opoc.getPositionOpenModels().get(0).getActPct(), 1.2404}
        };

        for (String[] test : stringTests) {
            (new TestString(test[0], test[1], test[2])).doTest();
        }

        for (Integer[] test : integerTests) {
            (new TestInteger(test[0], test[1], test[2])).doTest();
        }

        for (Double[] test : doubleTests) {
            (new TestDouble(test[0], test[1], test[2])).doTest();
        }

        assertTrue("Position GMTDtTradeOpen '" + opoc.getPositionOpenModels().get(0).getDateOpen().toString()
                       + "' not the expected value of '2022-01-21T00:00'",
            opoc.getPositionOpenModels().get(0).getDateOpen()
                .equals(CMHPIUtils.convertStringToLocalDateTime("2021-06-02 00:00:00")));

        //do not leave uncommented
//        opoc.doSQL();
    }
}
