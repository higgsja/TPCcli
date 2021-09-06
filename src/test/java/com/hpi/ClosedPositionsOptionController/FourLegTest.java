package com.hpi.ClosedPositionsOptionController;

import com.hpi.TPCCMcontrollers.CMDBController;
import com.hpi.TPCCMprefs.CMDBModel;
import com.hpi.appcontrollers.positions.ClosedPositionsOptionController2;
import com.hpi.entities.FIFOClosedTransactionModel;
import com.hpi.entities.PositionOpenModel;
import com.hpi.hpiUtils.CMHPIUtils;
import java.sql.Date;
import org.junit.*;
import static org.junit.Assert.*;

public class FourLegTest {

    public static ClosedPositionsOptionController2 cpoc;

    public FourLegTest() {
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
//        cpoc.getPositionClosedModels().clear();
//        cpoc.getFifoClosedTransactionModels().clear();
    }

    @After
    public void tearDown() {
    }

    @Ignore
    @Test
    public void IronCondor2() {
        FIFOClosedTransactionModel fctm;

        //custom position
        //test doTransactionsToPositions
        //2 unit iron condor but with 8 transactions
        fctm = FIFOClosedTransactionModel.builder()
                .dmAcctId(1)
                .joomlaId(816)
                .fiTId("201020_S_0")
                .transactionGrp(33485)
                .equityId("NFLX  201023C00605000")
                .ticker("NFLX")
                .dateOpen(Date.valueOf("2020-10-20"))
                .dateClose(Date.valueOf("2020-10-21"))
                .units(1.0)
                .priceOpen(2.04)
                .priceClose(-0.06)
                .dateExpire(Date.valueOf("2020-10-23"))
                .positionType("SHORT")
                .totalOpen(203.88)
                .totalClose(-6.015)
                .equityType("OPTION")
                .gainPct(97.049)
                .transactionType("SELLTOOPEN")
                .bComplete(false)
                .build();

        cpoc.getFifoClosedTransactionModels().add(fctm);

        fctm = FIFOClosedTransactionModel.builder()
                .dmAcctId(1)
                .joomlaId(816)
                .fiTId("201020_S_1")
                .transactionGrp(1)
                .equityId("NFLX  201023C00605000")
                .ticker("NFLX")
                .dateOpen(Date.valueOf("2020-10-20"))
                .dateClose(Date.valueOf("2020-10-21"))
                .units(1.0)
                .priceOpen(2.04)
                .priceClose(-0.06)
                .dateExpire(Date.valueOf("2020-10-23"))
                .positionType("SHORT")
                .totalOpen(203.88)
                .totalClose(-6.015)
                .equityType("OPTION")
                .gainPct(97.0497)
                .transactionType("SELLTOOPEN")
                .bComplete(false)
                .build();

        cpoc.getFifoClosedTransactionModels().add(fctm);

        fctm = FIFOClosedTransactionModel.builder()
                .dmAcctId(1)
                .joomlaId(816)
                .fiTId("201020_B_0")
                .transactionGrp(1)
                .equityId("NFLX  201023C00630000")
                .ticker("NFLX")
                .dateOpen(Date.valueOf("2020-10-20"))
                .dateClose(Date.valueOf("2020-10-21"))
                .units(-1.0)
                .priceOpen(0.97)
                .priceClose(-0.05)
                .dateExpire(Date.valueOf("2020-10-23"))
                .positionType("LONG")
                .totalOpen(-97.11)
                .totalClose(4.88)
                .equityType("OPTION")
                .gainPct(-94.9748)
                .transactionType("BUYTOOPEN")
                .bComplete(false)
                .build();

        cpoc.getFifoClosedTransactionModels().add(fctm);

        fctm = FIFOClosedTransactionModel.builder()
                .dmAcctId(1)
                .joomlaId(816)
                .fiTId("201020_B_3")
                .transactionGrp(1)
                .equityId("NFLX  201023C00630000")
                .ticker("NFLX")
                .dateOpen(Date.valueOf("2020-10-20"))
                .dateClose(Date.valueOf("2020-10-21"))
                .units(-1.0)
                .priceOpen(0.97)
                .priceClose(-0.05)
                .dateExpire(Date.valueOf("2020-10-23"))
                .positionType("LONG")
                .totalOpen(-97.11)
                .totalClose(4.88)
                .equityType("OPTION")
                .gainPct(-94.9748)
                .transactionType("BUYTOOPEN")
                .bComplete(false)
                .build();

        cpoc.getFifoClosedTransactionModels().add(fctm);

        fctm = FIFOClosedTransactionModel.builder()
                .dmAcctId(1)
                .joomlaId(816)
                .fiTId("201020_B_1")
                .transactionGrp(1)
                .equityId("NFLX  201023P00450000")
                .ticker("NFLX")
                .dateOpen(Date.valueOf("2020-10-20"))
                .dateClose(Date.valueOf("2020-10-21"))
                .units(-1.0)
                .priceOpen(0.82)
                .priceClose(-0.11)
                .dateExpire(Date.valueOf("2020-10-23"))
                .positionType("LONG")
                .totalOpen(-82.11)
                .totalClose(10.88)
                .equityType("OPTION")
                .gainPct(-86.7495)
                .transactionType("BUYTOOPEN")
                .bComplete(false)
                .build();

        cpoc.getFifoClosedTransactionModels().add(fctm);

        fctm = FIFOClosedTransactionModel.builder()
                .dmAcctId(1)
                .joomlaId(816)
                .fiTId("201020_B_2")
                .transactionGrp(1)
                .equityId("NFLX  201023P00450000")
                .ticker("NFLX")
                .dateOpen(Date.valueOf("2020-10-20"))
                .dateClose(Date.valueOf("2020-10-21"))
                .units(-1.0)
                .priceOpen(0.82)
                .priceClose(-0.11)
                .dateExpire(Date.valueOf("2020-10-23"))
                .positionType("LONG")
                .totalOpen(-82.11)
                .totalClose(10.88)
                .equityType("OPTION")
                .gainPct(-86.7495)
                .transactionType("BUYTOOPEN")
                .bComplete(false)
                .build();

        cpoc.getFifoClosedTransactionModels().add(fctm);

        fctm = FIFOClosedTransactionModel.builder()
                .dmAcctId(1)
                .joomlaId(816)
                .fiTId("201020_S_2")
                .transactionGrp(1)
                .equityId("NFLX  201023P00472500")
                .ticker("NFLX")
                .dateOpen(Date.valueOf("2020-10-20"))
                .dateClose(Date.valueOf("2020-10-21"))
                .units(1.0)
                .priceOpen(2.45)
                .priceClose(-0.7)
                .dateExpire(Date.valueOf("2020-10-23"))
                .positionType("SHORT")
                .totalOpen(244.88)
                .totalClose(-70.115)
                .equityType("OPTION")
                .gainPct(71.3676)
                .transactionType("SELLTOOPEN")
                .bComplete(false)
                .build();

        cpoc.getFifoClosedTransactionModels().add(fctm);

        fctm = FIFOClosedTransactionModel.builder()
                .dmAcctId(1)
                .joomlaId(816)
                .fiTId("201020_S_3")
                .transactionGrp(1)
                .equityId("NFLX  201023P00472500")
                .ticker("NFLX")
                .dateOpen(Date.valueOf("2020-10-20"))
                .dateClose(Date.valueOf("2020-10-21"))
                .units(1.0)
                .priceOpen(2.45)
                .priceClose(-0.7)
                .dateExpire(Date.valueOf("2020-10-23"))
                .positionType("SHORT")
                .totalOpen(244.88)
                .totalClose(-70.115)
                .equityType("OPTION")
                .gainPct(71.3676)
                .transactionType("SELLTOOPEN")
                .bComplete(false)
                .build();

        cpoc.getFifoClosedTransactionModels().add(fctm);

        cpoc.doFctm2Pctm();

        cpoc.doPctm2Pcm();

        cpoc.doPositionsTacticId();

        assertEquals("Position legs not the expected value of 4.0",
                cpoc.getPositionClosedTransactionModels().size(), 4.0, 0.001);

        assertEquals("Position units not the expected value of 2.0",
                cpoc.getPositionClosedModels().get(0).getUnits(), 2.0, 0.001);

        assertTrue("Position Ticker not the expected value of NFLX",
                cpoc.getPositionClosedModels().get(0).getTicker().equalsIgnoreCase("nflx"));

        assertTrue("TacticId is not the expected value of iron condor",
                cpoc.getPositionClosedModels().get(0).getTacticId().equals(PositionOpenModel.TACTICID_IRONCONDOR));

        assertEquals("Position PriceOpen '" + cpoc.getPositionClosedModels().get(0).getPriceOpen()
                + "' not the expected value of '2.6954'",
                cpoc.getPositionClosedModels().get(0).getPriceOpen(), 2.6954, .001);

        assertEquals("Position Price '" + cpoc.getPositionClosedModels().get(0).getPrice()
                + "' not the expected value of -0.6037",
                cpoc.getPositionClosedModels().get(0).getPrice(), -0.6037, .001);

        assertEquals("Position Gain not the expected value of '418.34'",
                //                cpoc.getPositionClosedModels().get(0).getGain(), 979.52, 0.001);
                cpoc.getPositionClosedModels().get(0).getGain(), 418.34, 0.001);

        assertEquals("Position GainPct not the expected value of 77.6026",
                //                cpoc.getPositionClosedModels().get(0).getGain(), 979.52, 0.001);
                cpoc.getPositionClosedModels().get(0).getGainPct(), 77.6026, 0.001);

        assertTrue("Position GMTDtTradeOpen '" + cpoc.getPositionClosedModels().get(0).getDateOpen().toString()
                + "' not the expected value of '2020-10-20T00:00'",
                cpoc.getPositionClosedModels().get(0).getDateOpen()
                        .equals(CMHPIUtils.convertStringToLocalDateTime("2020-10-20 00:00:00")));

        assertTrue("Position GMTDtTradeClose '" + cpoc.getPositionClosedModels().get(0).getDateClose().toString()
                + "' not the expected value of '2021-10-20T00:00'",
                cpoc.getPositionClosedModels().get(0).getDateClose()
                        .equals(CMHPIUtils.convertStringToLocalDateTime("2020-10-21 00:00:00")));

        cpoc.doPositionName();

        assertTrue("Position name is not the expected value of 'NFLX 23Oct20 605.0/630.0/450.0/472.5 I Cndr'",
                cpoc.getPositionClosedModels().get(0).getPositionName()
                        .equalsIgnoreCase("NFLX 23Oct20 605.0/630.0/450.0/472.5 I Cndr"));

        //do not leave this uncommented
//        cpoc.doSQL();
    }

}
