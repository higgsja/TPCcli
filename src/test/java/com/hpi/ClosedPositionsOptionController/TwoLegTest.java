package com.hpi.ClosedPositionsOptionController;

import com.hpi.TPCCMcontrollers.CMDBController;
import com.hpi.TPCCMprefs.CMDBModel;
import com.hpi.appcontrollers.positions.ClosedPositionsOptionController2;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public class TwoLegTest {

    public static ClosedPositionsOptionController2 cpoc;

    public TwoLegTest() {
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
    
//    //@Ignore
//    @Test
//    public void Diagonal() {
//        FIFOClosedTransactionModel fctm;
//
//        fctm = FIFOClosedTransactionModel.builder()
//            .dmAcctId(1)
//            .joomlaId(816)
//            .fiTId("210409_3606_0")
//            .closedGrp(7088)
//            .equityId("AMZN  210521C03365000")
//            .ticker("AMZN")
//            .gmtDtTradeOpen(Date.valueOf("2021-04-09"))
//            .gmtDtTradeClose(Date.valueOf("2021-04-27"))
//            .units(-1.0)
//            .unitPriceOpen(130.45)
//            .unitPriceClose(-157.80)
//            .dtExpire(Date.valueOf("2021-05-21"))
//            .days(-9)
//            .posType("LONG")
//            .totalOpen(-13045.110)
//            .totalClose(15779.8)
//            .equityType("OPTION")
//            .gainPct(20.9633)
//            .transType("BUYTOOPEN")
//            .bComplete(false)
//            .build();
//
//        cpoc.getFifoClosedTransactionModels().add(fctm);
//
//        fctm = FIFOClosedTransactionModel.builder()
//            .dmAcctId(1)
//            .joomlaId(816)
//            .fiTId("210409_3608_0")
//            .closedGrp(7089)
//            .equityId("AMZN  210618C03370000")
//            .ticker("AMZN")
//            .gmtDtTradeOpen(Date.valueOf("2021-04-09"))
//            .gmtDtTradeClose(Date.valueOf("2021-04-27"))
//            .units(-1.0)
//            .unitPriceOpen(161.85)
//            .unitPriceClose(-190.55)
//            .dtExpire(Date.valueOf("2021-06-18"))
//            .days(19)
//            .posType("LONG")
//            .totalOpen(-16185.11)
//            .totalClose(19054.79)
//            .equityType("OPTION")
//            .gainPct(17.7304)
//            .transType("BUYTOOPEN")
//            .bComplete(false)
//            .build();
//
//        cpoc.getFifoClosedTransactionModels().add(fctm);
//
//        cpoc.doFctm2Pctm();
//
//        assertTrue("PositionClosedTransactionModels size '"
//                       + cpoc.getPositionClosedTransactionModels().size() + "' is not expected value '2'",
//            cpoc.getPositionClosedTransactionModels().size() == 2);
//
//        cpoc.doPctm2Pcm();
//
//        cpoc.doPositionsTacticId();
//
//        assertTrue("Positions count not the expected value of '1'",
//            cpoc.getPositionClosedModels().size() == 1);
//
//        assertEquals("Position legs not the expected value of '2.0'",
//            cpoc.getPositionClosedTransactionModels().size(), 2.0, 0.001);
//
//        assertEquals("Position units not the expected value of '-1.0'",
//            cpoc.getPositionClosedModels().get(0).getUnits(), -1.0, 0.001);
//
//        assertTrue("Position Ticker not the expected value of 'AMZN'",
//            cpoc.getPositionClosedModels().get(0).getTicker().equalsIgnoreCase("amzn"));
//
//        assertTrue("TacticId is not the expected value of 'SHORT'",
//            cpoc.getPositionClosedModels().get(0).getTacticId().equals(PositionOpenModel.TACTICID_DIAGONAL));
//
//        assertEquals("Position IPrice '" + cpoc.getPositionClosedModels().get(0).getIPrice()
//                         + "' not the expected value of '292.3022'",
//            cpoc.getPositionClosedModels().get(0).getIPrice(), 292.3022, .001);
//
//        assertEquals("Position Price '" + cpoc.getPositionClosedModels().get(0).getPrice()
//                         + "' not the expected value of '-348.3459'",
//            cpoc.getPositionClosedModels().get(0).getPrice(), -348.3459, .001);
//
//        assertEquals("Position Gain not the expected value of '5604.3699'",
//            //                cpoc.getPositionClosedModels().get(0).getGain(), 979.52, 0.001);
//            cpoc.getPositionClosedModels().get(0).getGain(), 5604.3699, 0.001);
//
//        assertEquals("Position GainPct not the expected value of '19.1732'",
//            //                cpoc.getPositionClosedModels().get(0).getGain(), 979.52, 0.001);
//            cpoc.getPositionClosedModels().get(0).getGainPct(), 19.1732, 0.001);
//
//        assertTrue("Position GMTDtTradeOpen '" + cpoc.getPositionClosedModels().get(0).getGmtDtTrade().toString()
//                       + "' not the expected value of '2021-04-09T00:00'",
//            cpoc.getPositionClosedModels().get(0).getGmtDtTrade()
//                .equals(CMHPIUtils.convertStringToLocalDateTime("2021-04-09 00:00:00")));
//
//        assertTrue("Position GMTDtTradeClose '" + cpoc.getPositionClosedModels().get(0).getGmtDtTradeClose().toString()
//                       + "' not the expected value of '2021-04-27T00:00'",
//            cpoc.getPositionClosedModels().get(0).getGmtDtTradeClose()
//                .equals(CMHPIUtils.convertStringToLocalDateTime("2021-04-27 00:00:00")));
//
//        cpoc.doPositionName();
//        assertTrue("Position name is not the expected value of 'AAPL 19Feb21 115.0 Call'",
//                cpoc.getPositionClosedModels().get(0).getPositionName()
//                        .equalsIgnoreCase("AMZN 3365.0 21May21/18Jun21 C Diagnl"));
//    }
}
