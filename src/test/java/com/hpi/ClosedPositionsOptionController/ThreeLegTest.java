package com.hpi.ClosedPositionsOptionController;

import com.hpi.TPCCMcontrollers.CMDBController;
import com.hpi.TPCCMprefs.CMDBModel;
import com.hpi.appcontrollers.positions.ClosedPositionsOptionController2;
import com.hpi.entities.FIFOClosedTransactionModel;
import com.hpi.entities.PositionOpenModel;
import com.hpi.hpiUtils.*;
import java.sql.Date;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

public class ThreeLegTest {

    public static ClosedPositionsOptionController2 cpoc;

    public ThreeLegTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        CMDBModel.getInstance();
        CMDBModel.setUserId(816);
        ClosedPositionsOptionController2.getInstance();
        cpoc = ClosedPositionsOptionController2.getInstance();
        CMDBController.initDBConnection();

    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        cpoc.getPositionClosedModels().clear();
        cpoc.getFifoClosedTransactionModels().clear();
    }

    @After
    public void tearDown() {
    }

    @Ignore //fails
    @Test
    public void ThreeTransactions1() {
        FIFOClosedTransactionModel fctm;
        //sold 1 short call
        //sold 1 short call and 1 short put (strangle)
        //should result in 2 positions
        fctm = FIFOClosedTransactionModel.builder()
            .dmAcctId(1)
            .joomlaId(816)
            .fiTId("200731_3300_2")
            .transactionGrp(5662)
            .equityId("FB    200918C00280000")
            .ticker("FB")
            .dateOpen(Date.valueOf("2020-07-31"))
            .dateClose(Date.valueOf("2020-08-03"))
            .units(2.0)
            .priceOpen(2.35)
            .priceClose(-2.50)
            .dateExpire(Date.valueOf("2020-09-18"))
            .positionType("SHORT")
            .totalOpen(469.75)
            .totalClose(-500.23)
            .equityType("OPTION")
            .gainPct(-6.4886)
            .transactionType("SELLTOOPEN")
            .bComplete(false)
            .build();

        cpoc.getFifoClosedTransactionModels().add(fctm);

        fctm = FIFOClosedTransactionModel.builder()
            .dmAcctId(1)
            .joomlaId(816)
            .fiTId("200731_3300_3")
            .transactionGrp(5663)
            .equityId("FB    200918P00225000")
            .ticker("FB")
            .dateOpen(Date.valueOf("2020-07-31"))
            .dateClose(Date.valueOf("2020-08-03"))
            .units(2.0)
            .priceOpen(3.29)
            .priceClose(-3.55)
            .dateExpire(Date.valueOf("2020-09-18"))
            .positionType("SHORT")
            .totalOpen(657.75)
            .totalClose(-710.23)
            .equityType("OPTION")
            .gainPct(-7.9787)
            .transactionType("SELLTOOPEN")
            .bComplete(false)
            .build();

        cpoc.getFifoClosedTransactionModels().add(fctm);

        fctm = FIFOClosedTransactionModel.builder()
            .dmAcctId(1)
            .joomlaId(816)
            .fiTId("200731_3303_1")
            .transactionGrp(5662)
            .equityId("FB    200918C00275000")
            .ticker("FB")
            .dateOpen(Date.valueOf("2020-07-31"))
            .dateClose(Date.valueOf("2020-08-03"))
            .units(2.0)
            .priceOpen(3.94)
            .priceClose(-3.40)
            .dateExpire(Date.valueOf("2020-09-18"))
            .positionType("SHORT")
            .totalOpen(787.75)
            .totalClose(-680.23)
            .equityType("OPTION")
            .gainPct(13.6490)
            .transactionType("SELLTOOPEN")
            .bComplete(false)
            .build();

        cpoc.getFifoClosedTransactionModels().add(fctm);

        //at this point have the transactions in the fifoClosedTransactionModels array
        //in the same order as the query would put them
        cpoc.doFctm2Pctm();

        assertTrue("Position positionClosedTransactionModels size not the expect value of 3",
            cpoc.getPositionClosedTransactionModels().size() == 3);

        cpoc.doPctm2Pcm();

        cpoc.doPositionsTacticId();

        assertTrue("After aggregation, Position positionClosedModels size not the expect value of 2",
            cpoc.getPositionClosedModels().size() == 2);

        assertTrue("Position positionClosedTransactionsModels size not the expect value of 1",
            cpoc.getPositionClosedModels().get(0).getPositionClosedTransactionModels().size() == 1);

        assertEquals("Position units not the expected value of '2.0'",
            cpoc.getPositionClosedModels().get(0).getUnits(), 2.0, 0.001);

        assertEquals("Position PriceOpen not the expected value of 3.93875",
            cpoc.getPositionClosedModels().get(0).getPriceOpen(), 3.93875, 0.001);

        assertEquals("Position Price not the expected value of -3.40115",
            cpoc.getPositionClosedModels().get(0).getPrice(), -3.40115, 0.001);
        
        assertEquals("Position Gain not the expected value of '107.5199'",
            //                cpoc.getPositionClosedModels().get(0).getGain(), 979.52, 0.001);
            cpoc.getPositionClosedModels().get(0).getGain(), 107.5199, 0.001);

        assertEquals("Position gainPct not the expected value of '13.6490",
            cpoc.getPositionClosedModels().get(0).getGainPct(), 13.6490, 0.001);
        
        assertTrue("Position GMTDtTradeOpen '" + cpoc.getPositionClosedModels().get(0).getDateOpen().toString()
                       + "' not the expected value of '202-07-31T00:00'",
            cpoc.getPositionClosedModels().get(0).getDateOpen()
                .equals(CMHPIUtils.convertStringToLocalDateTime("2020-07-31 00:00:00")));

        assertTrue("Position GMTDtTradeClose '" + cpoc.getPositionClosedModels().get(0).getDateClose().toString()
                       + "' not the expected value of '2020-08-03T00:00'",
            cpoc.getPositionClosedModels().get(0).getDateClose()
                .equals(CMHPIUtils.convertStringToLocalDateTime("2020-08-03 00:00:00")));

        assertTrue("Position Ticker not the expected value of 'FB'",
            cpoc.getPositionClosedModels().get(0).getTicker().equalsIgnoreCase("fb"));

        assertTrue("TacticId is not the expected value of 'SHORT'",
            cpoc.getPositionClosedModels().get(0).getTacticId().equals(PositionOpenModel.TACTICID_SHORT));
        
/**
 * 
 */

        assertTrue("Position positionClosedTransactionsModels size not the expect value of '2'",
            cpoc.getPositionClosedModels().get(1).getPositionClosedTransactionModels().size() == 2);

        assertEquals("Position units not the expected value of '2.0'",
            cpoc.getPositionClosedModels().get(1).getUnits(), 2.0, 0.001);

        assertEquals("Position PriceOpen not the expected value of 5.6375",
            cpoc.getPositionClosedModels().get(1).getPriceOpen(), 5.6375, 0.001);

        assertEquals("Position Price not the expected value of -6.0523",
            cpoc.getPositionClosedModels().get(1).getPrice(), -6.0523, 0.001);
        
        assertEquals("Position Gain not the expected value of '-82.96'",
            //                cpoc.getPositionClosedModels().get(0).getGain(), 979.52, 0.001);
            cpoc.getPositionClosedModels().get(1).getGain(), -82.96, 0.001);

        assertEquals("Position gainPct not the expected value of '-7.3578",
            cpoc.getPositionClosedModels().get(1).getGainPct(), -7.3578, 0.001);
        
        assertTrue("Position GMTDtTradeOpen '" + cpoc.getPositionClosedModels().get(1).getDateOpen().toString()
                       + "' not the expected value of '202-07-31T00:00'",
            cpoc.getPositionClosedModels().get(1).getDateOpen()
                .equals(CMHPIUtils.convertStringToLocalDateTime("2020-07-31 00:00:00")));

        assertTrue("Position GMTDtTradeClose '" + cpoc.getPositionClosedModels().get(1).getDateClose().toString()
                       + "' not the expected value of '2020-08-03T00:00'",
            cpoc.getPositionClosedModels().get(1).getDateClose()
                .equals(CMHPIUtils.convertStringToLocalDateTime("2020-08-03 00:00:00")));

        assertTrue("Position Ticker not the expected value of 'FB'",
            cpoc.getPositionClosedModels().get(1).getTicker().equalsIgnoreCase("fb"));

        assertTrue("TacticId is not the expected value of 'SHORT'",
            cpoc.getPositionClosedModels().get(1).getTacticId().equals(PositionOpenModel.TACTICID_STRANGLE));

        cpoc.doPositionName();

        assertTrue("Position name is not the expected value of 'FB 18Sep20 275.0 Call'",
            cpoc.getPositionClosedModels().get(0).getPositionName()
                .equalsIgnoreCase("FB 18Sep20 275.0 Call"));

        //do not leave this uncommented
//        cpoc.doSQL();
    }

    @Ignore
    @Test
    public void ThreePositionClosedTransactionModels2() {
        FIFOClosedTransactionModel fctm;
        //1 short call; 2 lots of a short call; 4 lots of a short put
        //should yield a short strangle position; short call position; and
        // short put position
        //actually this is a 2x1 strangle and a short call
        fctm = FIFOClosedTransactionModel.builder()
            .dmAcctId(1)
            .joomlaId(816)
            .fiTId("210204_5345_0")
            .transactionGrp(1160)
            .equityId("PTON  210205C00185000")
            .ticker("PTON")
            .dateOpen(Date.valueOf("2021-02-04"))
            .dateClose(Date.valueOf("2021-02-05"))
            .units(10.0)
            .priceOpen(0.75)
            .priceClose(-0.03)
            .dateExpire(Date.valueOf("2021-02-05"))
            .positionType("SHORT")
            .totalOpen(748.82)
            .totalClose(-30.14)
            .equityType("OPTION")
            .gainPct(95.975)
            .transactionType("SELLTOOPEN")
            .bComplete(false)
            .build();

        cpoc.getFifoClosedTransactionModels().add(fctm);

        fctm = FIFOClosedTransactionModel.builder()
            .dmAcctId(1)
            .joomlaId(816)
            .fiTId("210204_5350_0")
            .transactionGrp(1161)
            .equityId("PTON  210205C00190000")
            .ticker("PTON")
            .dateOpen(Date.valueOf("2021-02-04"))
            .dateClose(Date.valueOf("2021-02-05"))
            .units(8.0)
            .priceOpen(0.59)
            .priceClose(-0.05)
            .dateExpire(Date.valueOf("2021-02-05"))
            .positionType("SHORT")
            .totalOpen(471.05)
            .totalClose(-40.1120)
            .equityType("OPTION")
            .gainPct(91.4846)
            .transactionType("SELLTOOPEN")
            .bComplete(false)
            .build();

        cpoc.getFifoClosedTransactionModels().add(fctm);

        fctm = FIFOClosedTransactionModel.builder()
            .dmAcctId(1)
            .joomlaId(816)
            .fiTId("210204_5350_3")
            .transactionGrp(1162)
            .equityId("PTON  210205C00190000")
            .ticker("PTON")
            .dateOpen(Date.valueOf("2021-02-04"))
            .dateClose(Date.valueOf("2021-02-05"))
            .units(2.0)
            .priceOpen(0.57)
            .priceClose(-0.05)
            .dateExpire(Date.valueOf("2021-02-05"))
            .positionType("SHORT")
            .totalOpen(113.76)
            .totalClose(-10.028)
            .equityType("OPTION")
            .gainPct(91.185)
            .transactionType("SELLTOOPEN")
            .bComplete(false)
            .build();

        cpoc.getFifoClosedTransactionModels().add(fctm);

        fctm = FIFOClosedTransactionModel.builder()
            .dmAcctId(1)
            .joomlaId(816)
            .fiTId("210204_5350_1")
            .transactionGrp(1163)
            .equityId("PTON  210205P00129000")
            .ticker("PTON")
            .dateOpen(Date.valueOf("2021-02-04"))
            .dateClose(Date.valueOf("2021-02-05"))
            .units(2.0)
            .priceOpen(0.41)
            .priceClose(-0.02)
            .dateExpire(Date.valueOf("2021-02-05"))
            .positionType("SHORT")
            .totalOpen(81.76)
            .totalClose(-4.028)
            .equityType("OPTION")
            .gainPct(95.074)
            .transactionType("SELLTOOPEN")
            .bComplete(false)
            .build();

        cpoc.getFifoClosedTransactionModels().add(fctm);

        fctm = FIFOClosedTransactionModel.builder()
            .dmAcctId(1)
            .joomlaId(816)
            .fiTId("210204_5350_2")
            .transactionGrp(1164)
            .equityId("PTON  210205P00129000")
            .ticker("PTON")
            .dateOpen(Date.valueOf("2021-02-04"))
            .dateClose(Date.valueOf("2021-02-05"))
            .units(8.0)
            .priceOpen(0.39)
            .priceClose(-0.02)
            .dateExpire(Date.valueOf("2021-02-05"))
            .positionType("SHORT")
            .totalOpen(311.06)
            .totalClose(-16.1120)
            .equityType("OPTION")
            .gainPct(94.8203)
            .transactionType("SELLTOOPEN")
            .bComplete(false)
            .build();

        cpoc.getFifoClosedTransactionModels().add(fctm);

        fctm = FIFOClosedTransactionModel.builder()
            .dmAcctId(1)
            .joomlaId(816)
            .fiTId("210204_S_0")
            .transactionGrp(1165)
            .equityId("PTON  210205P00129000")
            .ticker("PTON")
            .dateOpen(Date.valueOf("2021-02-04"))
            .dateClose(Date.valueOf("2021-02-05"))
            .units(2.0)
            .priceOpen(0.39)
            .priceClose(-0.05)
            .dateExpire(Date.valueOf("2021-02-05"))
            .positionType("SHORT")
            .totalOpen(77.76)
            .totalClose(-10.028)
            .equityType("OPTION")
            .gainPct(87.1039)
            .transactionType("SELLTOOPEN")
            .bComplete(false)
            .build();

        cpoc.getFifoClosedTransactionModels().add(fctm);

        fctm = FIFOClosedTransactionModel.builder()
            .dmAcctId(1)
            .joomlaId(816)
            .fiTId("210204_S_1")
            .transactionGrp(1166)
            .equityId("PTON  210205P00129000")
            .ticker("PTON")
            .dateOpen(Date.valueOf("2021-02-04"))
            .dateClose(Date.valueOf("2021-02-05"))
            .units(8.0)
            .priceOpen(0.39)
            .priceClose(-0.05)
            .dateExpire(Date.valueOf("2021-02-05"))
            .positionType("SHORT")
            .totalOpen(311.06)
            .totalClose(-40.1120)
            .equityType("OPTION")
            .gainPct(87.1047)
            .transactionType("SELLTOOPEN")
            .bComplete(false)
            .build();

        cpoc.getFifoClosedTransactionModels().add(fctm);
        //at this point have the transactions in the fifoClosedTransactionModels array
        //in the same order as the query would put them
        
        cpoc.getFifoClosedTransactionModels().add(fctm);

        cpoc.doFctm2Pctm();

        cpoc.doPctm2Pcm();

        cpoc.doPositionsTacticId();
        
        assertTrue("Position positionClosedTransactionModels size not the expect value of 3",
            cpoc.getPositionClosedModels().get(0).getPositionClosedTransactionModels().size() == 3);

        assertTrue("After aggregation, Position positionClosedTransactionsModels size not the expect value of 3",
            cpoc.getPositionClosedModels().get(0).getPositionClosedTransactionModels().size() == 3);

        cpoc.doPositionsTacticId();

        //have the right tactic but have not set the position attributes other than the tactic
        assertTrue("TacticId is not the expected value of 'strangle'",
            cpoc.getPositionClosedModels().get(1).getTacticId().equals(PositionOpenModel.TACTICID_STRANGLE_CUSTOM));

        assertTrue("Position units is not the expected value of '10'",
            cpoc.getPositionClosedModels().get(1).getUnits().equals(10.0));

        assertEquals("Position gain is not the expected value of '1246.03'",
            cpoc.getPositionClosedModels().get(1).getGain(), 1246.03, 0.0001);

        assertEquals("Position gain% is not the expected value of '91.1873'",
            cpoc.getPositionClosedModels().get(1).getGainPct(), 91.1873, 0.0001);

        assertEquals("Position PriceOpen is not the expected value of '1.36645'",
            cpoc.getPositionClosedModels().get(1).getPriceOpen(), 1.36645, 0.0001);

        assertEquals("Position Price is not the expected value of '-0.12042'",
            cpoc.getPositionClosedModels().get(1).getPrice(), -0.12042, 0.0001);

        cpoc.doPositionName();

        assertTrue("Position name is not the expected value of 'PTON 05Feb21 185.0 Call'",
            cpoc.getPositionClosedModels().get(0).getPositionName().equalsIgnoreCase("PTON 05Feb21 185.0 Call"));

        assertTrue("Position name is not the expected value of 'PTON 05Feb21 190.0C/129.0P Strngl Cstm'",
            cpoc.getPositionClosedModels().get(1).getPositionName()
                .equalsIgnoreCase("PTON 05Feb21 190.0C/129.0P Strngl Cstm"));

        //do not leave this uncommented
//        cpoc.doSQL();
    }
}
