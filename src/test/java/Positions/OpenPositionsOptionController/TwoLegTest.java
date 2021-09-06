package Positions.OpenPositionsOptionController;

import com.hpi.TPCCMcontrollers.*;
import com.hpi.TPCCMprefs.*;
import com.hpi.appcontrollers.positions.OpenPositionsOptionController2;
import lombok.*;
import org.junit.*;

@AllArgsConstructor
public class TwoLegTest {

    private static OpenPositionsOptionController2 opoc;

    @BeforeClass
    public static void setUpClass() {
        CMDBModel.getInstance();
        CMDBModel.setUserId(816);
//        ClosedPositionsOptionController2.getInstance();
//        cpoc = ClosedPositionsOptionController2.getInstance();
        opoc = OpenPositionsOptionController2.getInstance();
        CMDBController.initDBConnection();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

//    @Ignore
//    @Test
//    public void DiagonalA() {
//        FIFOOpenTransactionModel fotm;
//        fotm = FIFOOpenTransactionModel.builder()
//            .dmAcctId(2)
//            .joomlaId(816)
//            .fiTId("210409_3606_0")
////            .closedGrp(5254)
//            .equityId("AMZN  210521C03365000")
//            .ticker("AMZN")
//            .optType("CALL")
//            .strikePrice(3365.0)
//            .dtExpire(LocalDate.parse("2021-05-21"))
//            .gmtDtTrade(CMHPIUtils.convertStringToLocalDateTime("2021-04-09 00:00:00"))
//            .gmtDtSettle(null)
//            .shPerCtrct(100)
//            .units(-1.0)
//            .unitPrice(130.45)
//            .total(-13045.110)
//            .optTransType("BUYTOOPEN")            
//            .mktVal(14045.110)
//            .lMktVal(140451.10)
//            .equityType("OPTION")
//            .posType("LONG")
//            .actPct(0.0915)
//            .gainPct(7.6657)
//            .days(123)
//            .complete(0)
//            .bComplete(false)
//            .build();
//
//        opoc.getFifoOpenTransactionModels().add(fotm);
//        
//fotm = FIFOOpenTransactionModel.builder()
//            .dmAcctId(2)
//            .joomlaId(816)
//            .fiTId("210409_3608_0")
////            .closedGrp(5254)
//            .equityId("AMZN  210618C03370000")
//            .ticker("AMZN")
//            .optType("CALL")
//            .strikePrice(3370.0)
//            .dtExpire(LocalDate.parse("2021-06-18"))
//            .gmtDtTrade(CMHPIUtils.convertStringToLocalDateTime("2021-04-09 00:00:00"))
//            .gmtDtSettle(null)
//            .shPerCtrct(100)
//            .units(-1.0)
//            .unitPrice(161.85)
//            .total(-16185.11)
//            .optTransType("BUYTOOPEN")            
//            .mktVal(17185.11)
//            .lMktVal(171851.1)
//            .equityType("OPTION")
//            .posType("LONG")
//            .actPct(0.0915)
//            .gainPct(6.1785)
//            .days(153)
//            .complete(0)
//            .bComplete(false)
//            .build();
//
//        opoc.getFifoOpenTransactionModels().add(fotm);
//
//        opoc.doFotm2Potm();
//
//        opoc.doPotm2Pom();
//
//        opoc.doPositionsTacticId();
//
//        opoc.doPositionName();
//
//        String[][] stringTests = {
//            {"a", opoc.getPositionOpenModels().get(0).getPositionName(), "AMZN 3365.0 21May21/18Jun21 C Diagnl"},
//            {"b", opoc.getPositionOpenModels().get(0).getTicker(), "amzn"},
//            {"c", opoc.getPositionOpenModels().get(0).getType(), "LONG"}
//
//        };
//
//        Integer[][] integerTests = {
//            {1, opoc.getPositionOpenTransactionModels().size(), 2},
//            {2, opoc.getPositionOpenModels().size(), 1},
//            {11, opoc.getPositionOpenModels().get(0).getTacticId(), PositionOpenModel.TACTICID_DIAGONAL},
//            {12, opoc.getPositionOpenModels().get(0).getDays(), 123}
//
//        };
//
//        Double[][] doubleTests = {
//            {1.0, opoc.getPositionOpenModels().get(0).getUnits(), -1.0},
//            {2.0, opoc.getPositionOpenModels().get(0).getIPrice(), 292.3022},
//            {3.0, opoc.getPositionOpenModels().get(0).getPrice(), -312.3022},
//            {4.0, opoc.getPositionOpenModels().get(0).getGainPct(), 6.8422},
//            {5.0, opoc.getPositionOpenModels().get(0).getGain(), 2000.0},
//            {6.0, opoc.getPositionOpenModels().get(0).getMktVal(), 31230.22},
//            {7.0, opoc.getPositionOpenModels().get(0).getLMktVal(), 312302.2},
//            {8.0, opoc.getPositionOpenModels().get(0).getActPct(), 0.183},
//
//        };
//
//        for (String[] test : stringTests) {
//            (new TestString(test[0], test[1], test[2])).doTest();
//        }
//
//        for (Integer[] test : integerTests) {
//            (new TestInteger(test[0], test[1], test[2])).doTest();
//        }
//
//        for (Double[] test : doubleTests) {
//            (new TestDouble(test[0], test[1], test[2])).doTest();
//        }
//
//        assertTrue("Position GMTDtTradeOpen '" + opoc.getPositionOpenModels().get(0).getGmtDtTrade().toString()
//                       + "' not the expected value of '2021-04-09T00:00'",
//            opoc.getPositionOpenModels().get(0).getGmtDtTrade()
//                .equals(CMHPIUtils.convertStringToLocalDateTime("2021-04-09 00:00:00")));
//
//        //do not leave uncommented
////        cpoc.doSQL();
//    }
}
