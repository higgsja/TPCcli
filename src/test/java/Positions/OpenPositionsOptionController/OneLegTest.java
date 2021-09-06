package Positions.OpenPositionsOptionController;

import com.hpi.TPCCMcontrollers.*;
import com.hpi.TPCCMprefs.*;
import com.hpi.appcontrollers.positions.OpenPositionsOptionController2;
import com.hpi.entities.*;
import java.sql.Date;
import lombok.*;
import org.junit.*;

@AllArgsConstructor
public class OneLegTest
{

    private static OpenPositionsOptionController2 opoc;

    @BeforeClass
    public static void setUpClass()
    {
        CMDBModel.getInstance();
        CMDBModel.setUserId(816);
//        ClosedPositionsOptionController2.getInstance();
//        cpoc = ClosedPositionsOptionController2.getInstance();
        opoc = OpenPositionsOptionController2.getInstance();
        CMDBController.initDBConnection();
    }

    @AfterClass
    public static void tearDownClass()
    {
    }

    @Before
    public void setUp()
    {
    }

    @After
    public void tearDown()
    {
    }

    @Ignore
    @Test
    public void OneTrans()
    {
        FIFOOpenTransactionModel fotm;
        fotm = FIFOOpenTransactionModel.builder()
            .dmAcctId(2)
            .joomlaId(816)
            .fiTId("210604_3896_0")
            //            .transactionGrp(5254)
            .transactionGrp(null)
            .equityId("BP    220121C00026000")
            .ticker("BP")
            .optionType("CALL")
            .strikePrice(26.0)
            .dateExpire(Date.valueOf("2022-01-21"))
            .dateOpen(Date.valueOf("2022-01-21"))
            .shPerCtrct(100)
            .units(3.0)
            .priceOpen(3.05)
            .totalOpen(-915.34)
            .transactionType("BUYTOOPEN")
            .mktVal(990.00)
            .lMktVal(8232.00)
            .equityType("OPTION")
            .positionType("LONG")
            .actPct(0.0915)
            //            .gainPct(8.1967)
            .gainPct(null)
            .gain(null)
            .days(230)
            .complete(0)
            .bComplete(false)
            .build();

        opoc.getFifoOpenTransactionModels()
            .add(fotm);

        opoc.doFotm2Potm();

        opoc.doPotm2Pom();

        opoc.doPositionsTacticId();

        opoc.doPositionName();

        String[][] stringTests = {
            {"a",
                opoc.getPositionOpenModels()
                .get(0)
                .getPositionName(),
                "BP 21Jan22 26.0 Call"},
            {"b",
                opoc.getPositionOpenModels()
                .get(0)
                .getTicker(),
                "bp"},
            {"c",
                opoc.getPositionOpenModels()
                .get(0)
                .getPositionType(),
                "LONG"}

        };

        Integer[][] integerTests = {
            {1,
                opoc.getPositionOpenTransactionModels()
                .size(),
                1},
            {2,
                opoc.getPositionOpenModels()
                .size(),
                1},
            {11,
                opoc.getPositionOpenModels()
                .get(0)
                .getTacticId(),
                PositionOpenModel.TACTICID_LONG},
            {12,
                opoc.getPositionOpenModels()
                .get(0)
                .getDays(),
                230}

        };

        Double[][] doubleTests = {
            {1.0,
                opoc.getPositionOpenModels()
                .get(0)
                .getUnits(),
                3.0},
            {2.0,
                opoc.getPositionOpenModels()
                .get(0)
                .getPriceOpen(),
                -3.0511},
            {3.0,
                opoc.getPositionOpenModels()
                .get(0)
                .getPrice(),
                3.3},
            {4.0,
                opoc.getPositionOpenModels()
                .get(0)
                .getGainPct(),
                8.1565},
            {5.0,
                opoc.getPositionOpenModels()
                .get(0)
                .getGain(),
                74.659},
            {6.0,
                opoc.getPositionOpenModels()
                .get(0)
                .getMktVal(),
                990.0},
            {7.0,
                opoc.getPositionOpenModels()
                .get(0)
                .getLMktVal(),
                8232.0},
            {8.0,
                opoc.getPositionOpenModels()
                .get(0)
                .getActPct(),
                0.0915},};

        for (String[] test : stringTests) {
            (new TestString(test[0],
                test[1],
                test[2])).doTest();
        }

        for (Integer[] test : integerTests) {
            (new TestInteger(test[0],
                test[1],
                test[2])).doTest();
        }

        for (Double[] test : doubleTests) {
            (new TestDouble(test[0],
                test[1],
                test[2])).doTest();
        }

//        assertTrue("Position GMTDtTradeOpen '" + opoc.getPositionOpenModels().get(0).getDateOpen().toString()
//                + "' not the expected value of '2022-01-21T00:00'",
//                opoc.getPositionOpenModels().get(0).getDateOpen()
//                        .equals("2022-01-21"));
        //do not leave uncommented
        opoc.doSQL();
    }
}
