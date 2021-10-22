package Positions.ClosedPositionsStockController;

import com.hpi.ClosedPositionsOptionController.ClosedPositionsOptionBase;
import static com.hpi.ClosedPositionsOptionController.ClosedPositionsOptionBase.*;
import com.hpi.entities.*;
import java.sql.Date;
import lombok.*;
import org.junit.*;

@AllArgsConstructor
public class CloseTransTest10
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
     * simple single long stock position
     */
//    @Ignore
    @Test
    public void OneTrans1()
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
                "a", cpoController.getPositionClosedModels().get(0).getPositionName(), "AAPL LONG"
            },
            {
                "b", cpoController.getPositionClosedModels().get(0).getTicker(), "aapl"
            },
            {
                "c", cpoController.getPositionClosedModels().get(0).getPositionType(), "LONG"
            }

        };

        Integer[][] integerTests =
        {
            {
                1, cpoController.getPositionClosedTransactionModels().size(), 1
            },
            {
                2, cpoController.getPositionClosedTransactionModels().size(), 1
            }
        };

        Double[][] doubleTests =
        {
            {
                1.0, cpoController.getPositionClosedModels().get(0).getUnits(), 100.0
            },
            {
                2.0, cpoController.getPositionClosedModels().get(0).getPriceOpen(), 100.0
            },
            {
                3.0, cpoController.getPositionClosedModels().get(0).getPrice(), 100.0
            },
            {   
                4.0, cpoController.getPositionClosedModels().get(0).getTotalOpen(), -10000.0
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
//        .shPerCtrct(1)
        .days(0)
//        .clientSectorId(4)
//        .mktVal(10000.0)
//        .lMktVal(10000.0)
//        .actPct(2.0)
//        .optionType(null)
        .bComplete(false)
        .build();
}
