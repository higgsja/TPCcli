package Positions.ClosedPositionsStockController;

import com.hpi.entities.*;
import java.sql.Date;
import lombok.*;
import org.junit.*;

@AllArgsConstructor
public class ClosedTransTest
    extends ClosedPositionsStockBase
{

    @Override
    @Before
    public void before()
    {
        super.before();
    }

    @After
    public void tearDown()
    {
    }

    @Ignore
    @Test
    public void ClosedTrans1()
    {
        FIFOOpenTransactionModel ftm;

        cpsController.getFifoClosedTransactionModels().add(ftm1);

        cpsController.doFtm2Ptm();

        cpsController.doPtm2Pm();

        cpsController.doSQL();

        cpsController.setFifoTransactionsComplete();
        String[][] stringTests =
        {
            {
                "a", cpsController.getPositionModels().get(0).getPositionName(), "AAPL LONG"
            },
            {
                "b", cpsController.getPositionModels().get(0).getTicker(), "AAPL"
            },
            {
                "c", cpsController.getPositionModels().get(0).getPositionType(), "LONG"
            }

        };

        Integer[][] integerTests =
        {
            {
                1, cpsController.getPositionTransactionModels().size(), 1
            },
            {
                2, cpsController.getPositionModels().size(), 1
            },
            {
                11, cpsController.getPositionModels().get(0).getTacticId(), PositionOpenModel.TACTICID_LONG
            }

        };

        Double[][] doubleTests =
        {
            {
                1.0, cpsController.getPositionModels().get(0).getUnits(), 100.0
            },
            {
                2.0, cpsController.getPositionModels().get(0).getPriceOpen(), -100.0
            },
            {
                3.0, cpsController.getPositionModels().get(0).getPrice(), 101.0
            },
            {
                4.0, cpsController.getPositionModels().get(0).getGainPct(), 1.0
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

//        assertTrue("Position GMTDtTradeOpen '" + cpsController.getPositionModels().get(0).getDateOpen().toString()
//                       + "' not the expected value of '2021-03-05T00:00'",
//            cpsController.getPositionModels().get(0).getDateOpen()
//                .equals(CMHPIUtils.convertStringToLocalDateTime("2021-03-05 00:00:00")));
    }
    
    /**
     * 2 transactions from 2 accounts
     */
//    @Ignore
    @Test
    public void ClosedTrans2()
    {
        FIFOOpenTransactionModel ftm;

        cpsController.getFifoClosedTransactionModels().add(new FIFOClosedTransactionModel(ftm1));
        cpsController.getFifoClosedTransactionModels().add(new FIFOClosedTransactionModel(ftm2));

        cpsController.doFtm2Ptm();

        cpsController.doPtm2Pm();

        cpsController.doSQL();

        cpsController.setFifoTransactionsComplete();
        String[][] stringTests =
        {
            {
                "a", cpsController.getPositionModels().get(0).getPositionName(), "AAPL LONG"
            },
            {
                "b", cpsController.getPositionModels().get(0).getTicker(), "AAPL"
            },
            {
                "c", cpsController.getPositionModels().get(0).getPositionType(), "LONG"
            }

        };

        Integer[][] integerTests =
        {
            {
                1, cpsController.getPositionTransactionModels().size(), 1
            },
            {
                2, cpsController.getPositionModels().size(), 1
            },
            {
                11, cpsController.getPositionModels().get(0).getTacticId(), PositionOpenModel.TACTICID_LONG
            }

        };

        Double[][] doubleTests =
        {
            {
                1.0, cpsController.getPositionModels().get(0).getUnits(), 200.0
            },
            {
                2.0, cpsController.getPositionModels().get(0).getPriceOpen(), -100.0
            },
            {
                3.0, cpsController.getPositionModels().get(0).getPrice(), 101.0
            },
            {
                4.0, cpsController.getPositionModels().get(0).getGainPct(), 1.0
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

//        assertTrue("Position GMTDtTradeOpen '" + cpsController.getPositionModels().get(0).getDateOpen().toString()
//                       + "' not the expected value of '2021-03-05T00:00'",
//            cpsController.getPositionModels().get(0).getDateOpen()
//                .equals(CMHPIUtils.convertStringToLocalDateTime("2021-03-05 00:00:00")));
    }

    private static final FIFOClosedTransactionModel ftm1 = FIFOClosedTransactionModel.builder()
        .dmAcctId(1)
        .joomlaId(816)
        .fiTId("210429_6034_0")
        .transactionGrp(3338)
        .equityId("AAPL")
        .ticker("AAPL")
        .dateOpen(Date.valueOf("2021-04-29"))
        .dateClose(Date.valueOf("2021-07-28"))
        .units(100.0)
        .priceOpen(100.0)
        .priceClose(101.0)
        .totalOpen(-10000.0)
        .totalClose(10100.0)
        .transactionType("BUY")
        .equityType("STOCK")
        .positionType("LONG")
        .gainPct(1.0)
        .days(null)
        .complete(0)
        .bComplete(false)
        .build();

    private static final FIFOClosedTransactionModel ftm2 = FIFOClosedTransactionModel.builder()
        .dmAcctId(2)
        .joomlaId(816)
        .fiTId("210429_6034_1")
        .transactionGrp(3338)
        .equityId("AAPL")
        .ticker("AAPL")
        .dateOpen(Date.valueOf("2021-05-29"))
        .dateClose(Date.valueOf("2021-07-28"))
        .units(100.0)
        .priceOpen(100.0)
        .priceClose(101.0)
        .totalOpen(-10000.0)
        .totalClose(10100.0)
        .transactionType("BUY")
        .equityType("STOCK")
        .positionType("LONG")
        .gainPct(1.0)
        .days(null)
        .complete(0)
        .bComplete(false)
        .build();

}
