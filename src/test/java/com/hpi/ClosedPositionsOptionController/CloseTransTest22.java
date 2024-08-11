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
public class CloseTransTest22
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
     * short same put in 2 transactions; long put in 1 transactions
     * short put in 1 transactions; long put in 1 transactions
     * same underlying; same open date; same close date
     * outcome is 2 separate vertical positions
     */
//    @Ignore
    @Test
    public void CloseTrans22A()
    {
        cpoController.getFifoClosedTransactionModels().add(new FIFOClosedTransactionModel(fctm1));
        cpoController.getFifoClosedTransactionModels().add(new FIFOClosedTransactionModel(fctm2));
        cpoController.getFifoClosedTransactionModels().add(new FIFOClosedTransactionModel(fctm3));
        cpoController.getFifoClosedTransactionModels().add(new FIFOClosedTransactionModel(fctm4));
        cpoController.getFifoClosedTransactionModels().add(new FIFOClosedTransactionModel(fctm5));

        cpoController.doFctm2Pctm();

        cpoController.doPctm2Pcm();

        cpoController.doPositionsTacticId();

        cpoController.doPositionName();

        cpoController.doSQL();

        cpoController.setFifoClosedTransactionsComplete();

        String[][] stringTests =
        {
            {
                "a", cpoController.getPositionClosedModels().get(0).getPositionName(), "SPY 17Apr20 220.0/230.0 Put Vrtcl"
            },
            {
                "b", cpoController.getPositionClosedModels().get(0).getTicker(), "spy"
            },
            {
                "c", cpoController.getPositionClosedModels().get(0).getPositionType(), "SHORT"
            }

        };

        Integer[][] integerTests =
        {
            {
                1, cpoController.getPositionClosedTransactionModels().size(), 4
            }
        };

        Double[][] doubleTests =
        {
            {
                1.0, cpoController.getPositionClosedModels().get(0).getUnits(), -5.0
            },
            {
                2.0, cpoController.getPositionClosedModels().get(0).getPriceOpen(), -2.595
            },
            {
                3.0, cpoController.getPositionClosedModels().get(0).getPrice(), 2.003
            },
            {
                4.0, cpoController.getPositionClosedModels().get(0).getTotalOpen(), 1297.69
            },
            {
                5.0, cpoController.getPositionClosedModels().get(0).getTotalClose(), -1001.2740
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
        .fiTId("200312_2148_2")
        .ticker("SPY")
        .equityId("SPY   200417P00220000")
        .transactionName("SPY 17Apr20 220.0 Put")
        .dateOpen(Date.valueOf("2020-03-12"))
        .dateClose(Date.valueOf("2020-03-25"))
        .dateExpire(Date.valueOf("2020-04-17"))
        .units(-5.0)
        .priceOpen(8.23)
        .priceClose(-5.26)
        .totalOpen(-4115.5800)
        .totalClose(2629.3500)
        .gain(-1486.2300)
        .gainPct(-36.1123)
        .equityType("OPTION")
        .positionType("LONG")
        .transactionType("BUYTOOPEN")
        .complete(0)
        //        .optionType()
        //        .strikePrice()
        //        .shPerCtrct(1)
        .days(-552)
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
        .fiTId("200312_2148_0")
        .ticker("SPY")
        .equityId("SPY   200417P00230000")
        .transactionName("SPY 17Apr20 230.0 Put")
        .dateOpen(Date.valueOf("2020-03-12"))
        .dateClose(Date.valueOf("2020-03-25"))
        .dateExpire(Date.valueOf("2020-04-17"))
        .units(1.0)
        .priceOpen(10.82)
        .priceClose(-7.26)
        .totalOpen(1081.85)
        .totalClose(-726.16)
        .gain(355.734)
        .gainPct(32.882)
        .equityType("OPTION")
        .positionType("SHORT")
        .transactionType("SELLTOOPEN")
        .complete(0)
        //        .optionType()
        //        .strikePrice()
        //        .shPerCtrct(1)
        .days(-552)
        //        .clientSectorId(4)
        //        .mktVal(10000.0)
        //        .lMktVal(10000.0)
        //        .actPct(2.0)
        //        .optionType(null)
        .bComplete(false)
        .build();

    private static final FIFOClosedTransactionModel fctm3 = FIFOClosedTransactionModel.builder()
        .dmAcctId(1)
        .joomlaId(USER_ID)
        .fiTId("200312_2148_1")
        .ticker("SPY")
        .equityId("SPY   200417P00230000")
        .transactionName("SPY 17Apr20 230.0 Put")
        .dateOpen(Date.valueOf("2020-03-12"))
        .dateClose(Date.valueOf("2020-03-25"))
        .dateExpire(Date.valueOf("2020-04-17"))
        .units(4.0)
        .priceOpen(10.83)
        .priceClose(-7.26)
        .totalOpen(4331.42)
        .totalClose(-2904.464)
        .gain(1426.956)
        .gainPct(32.9443)
        .equityType("OPTION")
        .positionType("SHORT")
        .transactionType("SELLTOOPEN")
        .complete(0)
        //        .optionType()
        //        .strikePrice()
        //        .shPerCtrct(1)
        .days(-552)
        //        .clientSectorId(4)
        //        .mktVal(10000.0)
        //        .lMktVal(10000.0)
        //        .actPct(2.0)
        //        .optionType(null)
        .bComplete(false)
        .build();

    private static final FIFOClosedTransactionModel fctm4 = FIFOClosedTransactionModel.builder()
        .dmAcctId(1)
        .joomlaId(USER_ID)
        .fiTId("200312_2152_0")
        .ticker("SPY")
        .equityId("SPY   200417P00241000")
        .transactionName("SPY 17Apr20 241.0 Put")
        .dateOpen(Date.valueOf("2020-03-12"))
        .dateClose(Date.valueOf("2020-03-25"))
        .dateExpire(Date.valueOf("2020-04-17"))
        .units(-4.0)
        .priceOpen(16.0400)
        .priceClose(-10.9200)
        .totalOpen(-6416.4700)
        .totalClose(4367.4200)
        .gain(-2049.0500)
        .gainPct(-31.9342)
        .equityType("OPTION")
        .positionType("LONG")
        .transactionType("BUYTOOPEN")
        .complete(0)
        //        .optionType()
        //        .strikePrice()
        //        .shPerCtrct(1)
        .days(-552)
        //        .clientSectorId(4)
        //        .mktVal(10000.0)
        //        .lMktVal(10000.0)
        //        .actPct(2.0)
        //        .optionType(null)
        .bComplete(false)
        .build();

    private static final FIFOClosedTransactionModel fctm5 = FIFOClosedTransactionModel.builder()
        .dmAcctId(1)
        .joomlaId(USER_ID)
        .fiTId("200312_2152_1")
        .ticker("SPY")
        .equityId("SPY   200417P00251000")
        .transactionName("SPY 17Apr20 251.0 Put")
        .dateOpen(Date.valueOf("2020-03-12"))
        .dateClose(Date.valueOf("2020-03-25"))
        .dateExpire(Date.valueOf("2020-04-17"))
        .units(4.0)
        .priceOpen(20.2900)
        .priceClose(-14.7200)
        .totalOpen(8115.3400)
        .totalClose(-5888.4700)
        .gain(2226.8700)
        .gainPct(27.4403)
        .equityType("OPTION")
        .positionType("SHORT")
        .transactionType("SELLTOOPEN")
        .complete(0)
        //        .optionType()
        //        .strikePrice()
        //        .shPerCtrct(1)
        .days(-552)
        //        .clientSectorId(4)
        //        .mktVal(10000.0)
        //        .lMktVal(10000.0)
        //        .actPct(2.0)
        //        .optionType(null)
        .bComplete(false)
        .build();
}
