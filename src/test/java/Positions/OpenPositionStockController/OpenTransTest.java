package Positions.OpenPositionStockController;

import static Positions.OpenPositionStockController.OpenPositionsStockBase.*;
import com.hpi.entities.*;
import java.sql.Date;
import lombok.*;
import org.junit.*;

@AllArgsConstructor
public class OpenTransTest
    extends OpenPositionsStockBase
{
    @Before
    @Override
    public void beforeClass()
    {
        super.beforeClass();
    }

    @After
    public void tearDown()
    {
    }

//    @Ignore
    @Test
    public void OneTrans1()
    {
        opsController.getFifoTransactionModels().add(new FIFOOpenTransactionModel(ftm1));

        opsController.doFtm2Ptm();

        opsController.doPtm2Pm();

        opsController.doSQL();

        opsController.setFifoTransactionsComplete();

        String[][] stringTests =
        {
            {
                "a", opsController.getPositionModels().get(0).getPositionName(), "AAPL LONG"
            },
            {
                "b", opsController.getPositionModels().get(0).getTicker(), "aapl"
            },
            {
                "c", opsController.getPositionModels().get(0).getPositionType(), "LONG"
            }

        };

        Integer[][] integerTests =
        {
            {
                1, opsController.getPositionTransactionModels().size(), 1
            },
            {
                2, opsController.getPositionModels().size(), 1
            },
            {
                11, opsController.getPositionModels().get(0).getTacticId(), PositionOpenModel.TACTICID_LONG
            },
        };

        Double[][] doubleTests =
        {
            {
                1.0, opsController.getPositionModels().get(0).getUnits(), 100.0
            },
            {
                2.0, opsController.getPositionModels().get(0).getPriceOpen(), 100.0
            },
            {
                3.0, opsController.getPositionModels().get(0).getPrice(), 100.0
            },
            {   
                4.0, opsController.getPositionModels().get(0).getTotalOpen(), -10000.0
            }
        };

        for (String[] test : stringTests)
        {
            (new TestString(test[0], test[1], test[2])).doTest();
        }

        for (Integer[] test : integerTests)
        {
            (new TestInteger(test[0], test[1], test[2])).doTest();
        }

        for (Double[] test : doubleTests)
        {
            (new TestDouble(test[0], test[1], test[2])).doTest();
        }
    }

//    @Ignore
    @Test
    public void TwoTrans1()
    {
        opsController.getFifoTransactionModels().add(new FIFOOpenTransactionModel(ftm1));
        opsController.getFifoTransactionModels().add(new FIFOOpenTransactionModel(ftm2));

        opsController.doFtm2Ptm();

        opsController.doPtm2Pm();

        opsController.doSQL();

        opsController.setFifoTransactionsComplete();

        String[][] stringTests =
        {
            {
                "a", opsController.getPositionModels().get(0).getPositionName(), "AAPL LONG"
            },
            {
                "b", opsController.getPositionModels().get(0).getTicker(), "aapl"
            },
            {
                "c", opsController.getPositionModels().get(0).getPositionType(), "LONG"
            }

        };

        Integer[][] integerTests =
        {
            {
                1, opsController.getPositionTransactionModels().size(), 2
            },
            {
                2, opsController.getPositionModels().size(), 1
            },
            {
                3, opsController.getPositionModels().get(0).getTacticId(), PositionOpenModel.TACTICID_LONG
            },
        };

        Double[][] doubleTests =
        {
            {
                1.0, opsController.getPositionModels().get(0).getUnits(), 200.0
            },
            {
                2.0, opsController.getPositionModels().get(0).getPriceOpen(), 100.0
            },
            {
                3.0, opsController.getPositionModels().get(0).getPrice(), 100.0
            },
            {
                4.0, opsController.getPositionModels().get(0).getTotalOpen(), -20000.0
            }
        };

        for (String[] test : stringTests)
        {
            (new TestString(test[0], test[1], test[2])).doTest();
        }

        for (Integer[] test : integerTests)
        {
            (new TestInteger(test[0], test[1], test[2])).doTest();
        }

        for (Double[] test : doubleTests)
        {
            (new TestDouble(test[0], test[1], test[2])).doTest();
        }

//        assertTrue("Position GMTDtTradeOpen '" + opsc.getPositionModels().get(0).getDateOpen().toString()
//                       + "' not the expected value of '2021-03-05T00:00'",
//            opsc.getPositionModels().get(0).getDateOpen()
//                .equals(CMHPIUtils.convertStringToLocalDateTime("2021-03-05 00:00:00")));
    }

    private static final FIFOOpenTransactionModel ftm1 = FIFOOpenTransactionModel.builder()
        .dmAcctId(1)
        .joomlaId(USER_ID)
        .fiTId("210910_6582_0")
        .ticker("AAPL")
        .equityId("AAPL")
        .transactionName("AAPL")
        .dateOpen(Date.valueOf("2021-09-10"))
        //        .dateClose()
        //        .dateExpire()
        .units(100.0)
        .priceOpen(100.0)
        //        .priceClose()
        .totalOpen(-10000.0)
        //        .totalClose()
        //        .gain()
        //        .gainPct()
        .equityType("STOCK")
        .positionType("LONG")
        .transactionType("BUY")
        .complete(0)
        //        .optionType()
        //        .strikePrice()
        .shPerCtrct(1)
        .days(0)
        .clientSectorId(4)
        .mktVal(10000.0)
        .lMktVal(10000.0)
        .actPct(2.0)
        .optionType(null)
        .bComplete(false)
        .build();

    private static final FIFOOpenTransactionModel ftm2 = FIFOOpenTransactionModel.builder()
        .dmAcctId(2)
        .joomlaId(USER_ID)
        .fiTId("210911_6582_0")
        .ticker("AAPL")
        .equityId("AAPL")
        .transactionName("AAPL")
        .dateOpen(Date.valueOf("2021-09-11"))
        //        .dateClose()
        //        .dateExpire()
        .units(100.0)
        .priceOpen(100.0)
        //        .priceClose()
        .totalOpen(-10000.0)
        //        .totalClose()
        //        .gain()
        //        .gainPct()
        .equityType("STOCK")
        .positionType("LONG")
        .transactionType("BUY")
        .complete(0)
        //        .optionType()
        //        .strikePrice()
        .shPerCtrct(1)
        .days(0)
        .clientSectorId(4)
        .mktVal(10000.0)
        .lMktVal(10000.0)
        .actPct(2.0)
        .bComplete(false)
        .build();
}
