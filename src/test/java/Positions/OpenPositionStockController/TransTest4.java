package Positions.OpenPositionStockController;

import StockController.StockControllerBase;
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
public class TransTest4
    extends StockControllerBase
{

//    private static OpenPositionsStockController2 opsc;
//    @BeforeClass
//    public static void setUpClass()
//    {
//        CMDBModel.getInstance();
////        CMDBModel.setUserId(816);
//        opsc = OpenPositionsStockController2.getInstance();
//        CMDBController.initDBConnection();
//    }
//    @AfterClass
//    public static void tearDownClass()
//    {
//    }
    @Before
    public void setUp()
    {
    }

    @After
    public void tearDown()
    {
    }

//    @Ignore
    @Test
    public void OneTrans1()
    {
        FIFOOpenTransactionModel ftm;

        ftm = FIFOOpenTransactionModel.builder()
            .dmAcctId(1)
            .joomlaId(816)
            .fiTId("210628_6221_0")
            //.transactionGrp(null)
            .ticker("GOOGL")
            .equityId("GOOGL")
            .dateOpen(Date.valueOf("2021-06-28"))
            //.dateClose(null)
            //.dateExpire(null)
            .units(5.0)
            .priceOpen(2435.29)
            //.priceClose(null)
            .totalOpen(-12176.4500)
            //.totalClose(null)
            //.gain(null)
            //.gainPct(null)
            .equityType("STOCK")
            .positionType("LONG")
            .transactionType("BUY")
            .complete(0)
            //.optionType(null)
            //.strikePrice(null)
            .shPerCtrct(1)
            .days(0)
            .clientSectorId(47)
            .mktVal(13742.9500)
            .lMktVal(13742.9500)
            .actPct(0.6145)
            .bComplete(false)
            .build();

        opsc.getFifoTransactionModels()
            .add(ftm);

        ftm = FIFOOpenTransactionModel.builder()
            .dmAcctId(1)
            .joomlaId(816)
            .fiTId("210706_6243_0")
            //.transactionGrp(null)
            .ticker("GOOGL")
            .equityId("GOOGL")
            .dateOpen(Date.valueOf("2021-07-06"))
            //.dateClose(null)
            //.dateExpire(null)
            .units(5.0)
            .priceOpen(2517.0)
            //.priceClose(null)
            .totalOpen(-12585.0000)
            //.totalClose(null)
            //.gain(null)
            //.gainPct(null)
            .equityType("STOCK")
            .positionType("LONG")
            .transactionType("BUY")
            .complete(0)
            //.optionType(null)
            //.strikePrice(null)
            .shPerCtrct(1)
            .days(0)
            .clientSectorId(47)
            .mktVal(13742.9500)
            .lMktVal(13742.9500)
            .actPct(0.6145)
            .bComplete(false)
            .build();

        opsc.getFifoTransactionModels()
            .add(ftm);

        ftm = FIFOOpenTransactionModel.builder()
            .dmAcctId(2)
            .joomlaId(816)
            .fiTId("210712_4198_0")
            //.transactionGrp(null)
            .ticker("GOOGL")
            .equityId("GOOGL")
            .dateOpen(Date.valueOf("2021-07-12"))
            //.dateClose(null)
            //.dateExpire(null)
            .units(1.0)
            .priceOpen(2525.6300)
            //.priceClose(null)
            .totalOpen(-2525.6300)
            //.totalClose(null)
            //.gain(null)
            //.gainPct(null)
            .equityType("STOCK")
            .positionType("LONG")
            .transactionType("BUY")
            .complete(0)
            //.optionType(null)
            //.strikePrice(null)
            .shPerCtrct(1)
            .days(0)
            .clientSectorId(47)
            .mktVal(2748.5900)
            .lMktVal(2748.5900)
            .actPct(0.1229)
            .bComplete(false)
            .build();

        opsc.getFifoTransactionModels()
            .add(ftm);

        ftm = FIFOOpenTransactionModel.builder()
            .dmAcctId(2)
            .joomlaId(816)
            .fiTId("210712_4198_1")
            //.transactionGrp(null)
            .ticker("GOOGL")
            .equityId("GOOGL")
            .dateOpen(Date.valueOf("2021-07-12"))
            //.dateClose(null)
            //.dateExpire(null)
            .units(9.0)
            .priceOpen(2525.7300)
            //.priceClose(null)
            .totalOpen(-22731.5700)
            //.totalClose(null)
            //.gain(null)
            //.gainPct(null)
            .equityType("STOCK")
            .positionType("LONG")
            .transactionType("BUY")
            .complete(0)
            //.optionType(null)
            //.strikePrice(null)
            .shPerCtrct(1)
            .days(0)
            .clientSectorId(47)
            .mktVal(24737.3100)
            .lMktVal(24737.3100)
            .actPct(1.1061)
            .bComplete(false)
            .build();

        opsc.getFifoTransactionModels()
            .add(ftm);

        opsc.doFtm2Ptm();

        opsc.doPtm2Pm();

//        opsc.doPositionsTacticId(opsc.getPositionModels());
//        opsc.doPositionName(opsc.getPositionModels());
        String[][] stringTests = {
            {"a",
                opsc.getPositionModels()
                .get(0)
                .getPositionName(),
                "GOOGL"},
            {"b",
                opsc.getPositionModels()
                .get(0)
                .getTicker(),
                "googl"},
            {"c",
                opsc.getPositionModels()
                .get(0)
                .getPositionType(),
                "LONG"}
        };

        Integer[][] integerTests = {
            {1,
                opsc.getPositionTransactionModels()
                .size(),
                2},
            {2,
                opsc.getPositionModels()
                .size(),
                1},
            {3,
                opsc.getPositionModels()
                .get(0)
                .getTacticId(),
                PositionOpenModel.TACTICID_LONG}
        };

        Double[][] doubleTests = {
            {1.0,
                opsc.getPositionModels()
                .get(0)
                .getUnits(),
                20.0},
            {2.0,
                opsc.getPositionModels()
                .get(0)
                .getPriceOpen(),
                2500.9325},
            //            {3.0, opsc.getPositionModels().get(0).getPrice(), null},
            //            {4.0, opsc.getPositionModels().get(0).getGainPct(), null},
            //            {5.0, opsc.getPositionModels().get(0).getGain(), null},
            {6.0,
                opsc.getPositionModels()
                .get(0)
                .getMktVal(),
                54971.8},
            {7.0,
                opsc.getPositionModels()
                .get(0)
                .getLMktVal(),
                54971.8},
            {8.0,
                opsc.getPositionModels()
                .get(0)
                .getActPct(),
                0.0},
            {9.0,
                opsc.getPositionModels()
                .get(0)
                .getGain(),
                4953.15},
            {10.0,
                opsc.getPositionModels()
                .get(0)
                .getGainPct(),
                9.9026}
        };

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

//        assertTrue("Position GMTDtTradeOpen '" + opsc.getPositionModels().get(0).getDateOpen().toString()
//                       + "' not the expected value of '2021-03-05T00:00'",
//            opsc.getPositionModels().get(0).getDateOpen()
//                .equals(CMHPIUtils.convertStringToLocalDateTime("2021-03-05 00:00:00")));
        //do not leave uncommented
        opsc.doSQL();

        //mark transactions complete
        opsc.setFifoTransactionsComplete();

    }
}
