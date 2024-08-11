package com.hpi.ClosedPositionsOptionController;

import static com.hpi.ClosedPositionsOptionController.ClosedPositionsOptionBase.*;
import com.hpi.entities.*;
import java.sql.Date;
import lombok.*;
import org.junit.*;

/**
 * Given fifoClosedTransaction models, get to closedPosition
 *
 */
@AllArgsConstructor
public class CloseTransTest21
    extends ClosedPositionsOptionBase
{

    @Before
    @Override
    public void before()
    {
        super.before();
    }

    @After
    public void after()
    {
    }

    /**
     * special situation
     * sell option in dmAcct 2; buy option in dmAcct1: same open day, different close days
     */
//    @Ignore
    @Test
    public void CloseTrans21A()
    {
        cpoController.getFifoClosedTransactionModels().add(new FIFOClosedTransactionModel(fctm1));
        cpoController.getFifoClosedTransactionModels().add(new FIFOClosedTransactionModel(fctm2));

        cpoController.doFctm2Pctm();

        cpoController.doPctm2Pcm();

        cpoController.doPositionsTacticId();

        cpoController.doPositionName();

        cpoController.doSQL();

        cpoController.setFifoClosedTransactionsComplete();

        String[][] stringTests =
        {
            {
                "a", cpoController.getPositionClosedModels().get(0).getPositionName(), "PCG 18Sep20 13.0 Call"
            },
            {
                "b", cpoController.getPositionClosedModels().get(0).getTicker(), "pcg"
            },
            {
                "c", cpoController.getPositionClosedModels().get(0).getPositionType(), "LONG"
            }

        };

        Integer[][] integerTests =
        {
            {
                1, cpoController.getPositionClosedTransactionModels().size(), 2
            }
        };

        Double[][] doubleTests =
        {
            {
                1.0, cpoController.getPositionClosedModels().get(0).getUnits(), 10.0
            },
            {
                2.0, cpoController.getPositionClosedModels().get(0).getPriceOpen(), -2.72875
            },
            {
                3.0, cpoController.getPositionClosedModels().get(0).getPrice(), -2.63116
            },
            {
                4.0, cpoController.getPositionClosedModels().get(0).getTotalOpen(), -2728.75
            },
            {
                5.0, cpoController.getPositionClosedModels().get(0).getTotalClose(), -2631.16
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

    private static final FIFOClosedTransactionModel fctm1 = FIFOClosedTransactionModel.builder()
        .dmAcctId(2)
        .joomlaId(USER_ID)
        .fiTId("200413_1161_0")
        .ticker("PCG")
        .equityId("PCG   200918C00013000")
        .transactionName("PCG 18Sep20 13.0 Call")
        .dateOpen(Date.valueOf("2020-04-13"))
        .dateClose(Date.valueOf("2020-04-15"))
        .dateExpire(Date.valueOf("2020-09-18"))
        .units(10.0)
        .priceOpen(2.73)
        .priceClose(-2.63)
        .totalOpen(-2728.75)
        .totalClose(-2631.16)
        .gain(97.59)
        .gainPct(3.5764)
        .equityType("OPTION")
        .positionType("SHORT")
        .transactionType("SELLTOOPEN")
        .complete(0)
        //        .optionType()
        //        .strikePrice()
        //        .shPerCtrct(1)
        .days(-397)
        //        .clientSectorId(4)
        //        .mktVal(10000.0)
        //        .lMktVal(10000.0)
        //        .actPct(2.0)
        //        .optionType(null)
        .bComplete(false)
        .build();

    private static final FIFOClosedTransactionModel fctm2 = FIFOClosedTransactionModel.builder()
        .dmAcctId(1)
        .joomlaId(USER_ID)
        .fiTId("200413_2359_0")
        .ticker("PCG")
        .equityId("PCG   200918C00013000")
        .transactionName("PCG 18Sep20 13.0 Call")
        .dateOpen(Date.valueOf("2020-04-13"))
        .dateClose(Date.valueOf("2020-07-23"))
        .dateExpire(Date.valueOf("2020-09-18"))
        .units(-10.0)
        .priceOpen(2.83)
        .priceClose(-0.01)
        .totalOpen(2831.16)
        .totalClose(98.81)
        .gain(-2732.35)
        .gainPct(-96.5099)
        .equityType("OPTION")
        .positionType("LONG")
        .transactionType("BUYTOOPEN")
        .complete(0)
        //        .optionType()
        //        .strikePrice()
        //        .shPerCtrct(1)
        .days(-397)
        //        .clientSectorId(4)
        //        .mktVal(10000.0)
        //        .lMktVal(10000.0)
        //        .actPct(2.0)
        //        .optionType(null)
        .bComplete(false)
        .build();
}
