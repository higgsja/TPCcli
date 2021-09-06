package Positions.OpenPositionStockController;

import com.hpi.TPCCMcontrollers.*;
import com.hpi.TPCCMprefs.*;
import com.hpi.appcontrollers.positions.OpenPositionsStockController2;
import com.hpi.entities.*;
import java.sql.Date;
import lombok.*;
import org.junit.*;

/**
 * Two stock transactions of the same stock, different dates
 *
 */
@AllArgsConstructor
public class TransTest2 {

    private static OpenPositionsStockController2 opsc;

    @BeforeClass
    public static void setUpClass() {
        CMDBModel.getInstance();
        CMDBModel.setUserId(816);
        opsc = OpenPositionsStockController2.getInstance();
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

//    @Ignore
    @Test
    public void OneTrans1() {
        FIFOOpenTransactionModel ftm;

        ftm = FIFOOpenTransactionModel.builder()
                .dmAcctId(1)
                .joomlaId(816)
                .fiTId("210412_5961_0")
                //.transactionGrp(null)
                .ticker("GM")
                .equityId("GM")
                .dateOpen(Date.valueOf("2021-04-12"))
                //.dateClose(null)
                //.dateExpire(null)
                .units(250.0)
                .priceOpen(60.1800)
                //.priceClose(null)
                .totalOpen(-15045.0000)
                //.totalClose(null)
                //.gain(null)
                //.gainPct(null)
                .equityType("STOCK")
                .positionType("LONG")
                .transactionType("BUY")
                .complete(1)
                //.optionType(null)
                //.strikePrice(null)
                .shPerCtrct(1)
                .days(0)
                .clientSectorId(4)
                .mktVal(14210.0000)
                .lMktVal(14210.0000)
                .actPct(0.9509)
                .bComplete(false)
                .build();

        opsc.getFifoTransactionModels().add(ftm);

        ftm = FIFOOpenTransactionModel.builder()
                .dmAcctId(1)
                .joomlaId(816)
                .fiTId("210505_6081_0")
                //.transactionGrp(null)
                .ticker("GM")
                .equityId("GM")
                .dateOpen(Date.valueOf("2021-05-05"))
                //.dateClose(null)
                //.dateExpire(null)
                .units(50.0)
                .priceOpen(57.4100)
                //.priceClose(null)
                .totalOpen(-2870.5000)
                //.totalClose(null)
                //.gain(null)
                //.gainPct(null)
                .equityType("STOCK")
                .positionType("LONG")
                .transactionType("BUY")
                .complete(1)
                //.optionType(null)
                //.strikePrice(null)
                .shPerCtrct(1)
                .days(0)
                .clientSectorId(4)
                .mktVal(14210.0000)
                .lMktVal(14210.0000)
                .actPct(0.9509)
                .bComplete(false)
                .build();

        opsc.doFtm2Ptm();

        opsc.doPtm2Pm();

        opsc.doPositionsTacticId(opsc.getPositionModels());

        opsc.doPositionName(opsc.getPositionModels());

        String[][] stringTests = {
            {"a", opsc.getPositionModels().get(0).getPositionName(), "GM LONG"},
            {"b", opsc.getPositionModels().get(0).getTicker(), "gm"},
            {"c", opsc.getPositionModels().get(0).getPositionType(), "LONG"}
        };

        Integer[][] integerTests = {
            {1, opsc.getPositionTransactionModels().size(), 1},
            {2, opsc.getPositionModels().size(), 1},
            {3, opsc.getPositionModels().get(0).getTacticId(), PositionOpenModel.TACTICID_LONG}
        };

        Double[][] doubleTests = {
            {1.0, opsc.getPositionModels().get(0).getUnits(), 250.0},
            {2.0, opsc.getPositionModels().get(0).getPriceOpen(), 60.1800},
            //            {3.0, opsc.getPositionModels().get(0).getPrice(), null},
            //            {4.0, opsc.getPositionModels().get(0).getGainPct(), null},
            //            {5.0, opsc.getPositionModels().get(0).getGain(), null},
            {6.0, opsc.getPositionModels().get(0).getMktVal(), 14210.0},
            {7.0, opsc.getPositionModels().get(0).getLMktVal(), 14210.0},
            {8.0, opsc.getPositionModels().get(0).getActPct(), 0.9509}
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

//        assertTrue("Position GMTDtTradeOpen '" + opsc.getPositionModels().get(0).getDateOpen().toString()
//                       + "' not the expected value of '2021-03-05T00:00'",
//            opsc.getPositionModels().get(0).getDateOpen()
//                .equals(CMHPIUtils.convertStringToLocalDateTime("2021-03-05 00:00:00")));
        //do not leave uncommented
        opsc.doSQL();
    }
}
