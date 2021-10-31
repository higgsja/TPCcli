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
public class CloseTransTest1
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
     * simple single leg call
     */
//    @Ignore
    @Test
    public void CloseTrans11A()
    {
        cpoController.getFifoClosedTransactionModels().add(new FIFOClosedTransactionModel(fctm1));

        cpoController.doFctm2Pctm();

        cpoController.doPctm2Pcm();

        cpoController.doPositionsTacticId();

        cpoController.doPositionName();

        cpoController.doSQL();

        cpoController.setFifoClosedTransactionsComplete();

        String[][] stringTests =
        {
            {
                "a", cpoController.getPositionClosedModels().get(0).getPositionName(),
                "NVDA 17Dec21 212.5 Call"
            },
            {
                "b", cpoController.getPositionClosedModels().get(0).getTicker(), "nvda"
            },
            {
                "c", cpoController.getPositionClosedModels().get(0).getPositionType(), "long"
            }
        };

        Integer[][] integerTests =
        {
            {
                1, cpoController.getPositionClosedTransactionModels().size(), 1
            }
        };

        Double[][] doubleTests =
        {
            {
                1.0, cpoController.getPositionClosedModels().get(0).getUnits(), -2.0
            },
            {
                2.0, cpoController.getPositionClosedModels().get(0).getPriceOpen(), 18.2011
            },
            {
                3.0, cpoController.getPositionClosedModels().get(0).getPrice(), -37.6287
            },
            {
                4.0, cpoController.getPositionClosedModels().get(0).getTotalOpen(), -3640.22
            },
            {
                5.0, cpoController.getPositionClosedModels().get(0).getTotalClose(), 7525.74
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

    //sql pulls with order by EquityId, DateOpen, DateClose
    private static final FIFOClosedTransactionModel fctm1 = FIFOClosedTransactionModel.builder()
        .dmAcctId(1)
        .joomlaId(USER_ID)
        .fiTId("211020_6673_0")
        .ticker("NVDA")
        .equityId("NVDA  211217C00212500")
        .transactionName("NVDA 17Dec21 212.5 Call")
        .dateOpen(Date.valueOf("2021-10-20"))
        .dateClose(Date.valueOf("2021-10-26"))
        .dateExpire(Date.valueOf("2021-12-17"))
        .units(-2.0)
        .priceOpen(18.2)
        .priceClose(-37.63)
        .totalOpen(-3640.2200)
        .totalClose(7525.7400)
        .gain(3885.5200)
        .gainPct(106.7386)
        .equityType("OPTION")
        .positionType("LONG")
        .transactionType("BUYTOOPEN")
        .complete(0)
        //        .optionType()
        //        .strikePrice()
        //        .shPerCtrct(1)
        .days(47)
        //        .clientSectorId(4)
        //        .mktVal(10000.0)
        //        .lMktVal(10000.0)
        //        .actPct(2.0)
        //        .optionType(null)
        .bComplete(false)
        .build();
}
