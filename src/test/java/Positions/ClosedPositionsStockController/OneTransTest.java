package Positions.ClosedPositionsStockController;

import com.hpi.TPCCMcontrollers.*;
import com.hpi.TPCCMprefs.*;
import com.hpi.appcontrollers.positions.OpenPositionsStockController2;
import com.hpi.entities.*;
import com.hpi.hpiUtils.CMHPIUtils;
import java.sql.Date;
import lombok.*;
import org.junit.*;
import static org.junit.Assert.assertTrue;

@AllArgsConstructor
public class OneTransTest {

    private static OpenPositionsStockController2 opsc;

    @BeforeClass
    public static void setUpClass() {
        CMDBModel.getInstance();
        CMDBModel.setUserId(816);
        opsc = OpenPositionsStockController2.getInstance();
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

    @Ignore
    @Test
    public void OneLeg2TransactionsB() {
        FIFOOpenTransactionModel ftm;
        
        ftm = FIFOOpenTransactionModel.builder()
            .dmAcctId(1)
            .joomlaId(816)
            .fiTId("210305_5596_0")
//            .closedGrp(5254)
            .equityId("GOOGL")
            .ticker("GOOGL")
            .dateOpen(Date.valueOf("2021-03-05"))
            .shPerCtrct(1)
            .units(10.0)
            .priceOpen(2064.927)
            .totalOpen(-20649.27)
            .transactionType("BUY")
            .mktVal(23935.70)
            .lMktVal(239357.0)
            .equityType("STOCK")
            .positionType("LONG")
            .actPct(0.2648)
            .gainPct(15.9155)
            .days(null)
            .complete(0)
            .bComplete(false)
            .build();

        opsc.getFifoTransactionModels().add(ftm);

        opsc.doFtm2Ptm();

        opsc.doPtm2Pm();

        opsc.doPositionsTacticId(opsc.getPositionModels());

        opsc.doPositionName(opsc.getPositionModels());

        String[][] stringTests = {
            {"a", opsc.getPositionModels().get(0).getPositionName(), "GOOGL LONG"},
            {"b", opsc.getPositionModels().get(0).getTicker(), "googl"},
            {"c", opsc.getPositionModels().get(0).getPositionType(), "LONG"}

        };

        Integer[][] integerTests = {
            {1, opsc.getPositionTransactionModels().size(), 1},
            {2, opsc.getPositionModels().size(), 1},
            {11, opsc.getPositionModels().get(0).getTacticId(), PositionOpenModel.TACTICID_LONG},

        };

        Double[][] doubleTests = {
            {1.0, opsc.getPositionModels().get(0).getUnits(), 10.0},
            {2.0, opsc.getPositionModels().get(0).getPriceOpen(), -2064.927},
            {3.0, opsc.getPositionModels().get(0).getPrice(), 2393.57},
            {4.0, opsc.getPositionModels().get(0).getGainPct(), 15.91547},
            {5.0, opsc.getPositionModels().get(0).getGain(), 3286.43},
            {6.0, opsc.getPositionModels().get(0).getMktVal(), 23935.7},
            {7.0, opsc.getPositionModels().get(0).getLMktVal(), 239357.0},
            {8.0, opsc.getPositionModels().get(0).getActPct(), 0.2648},

        };

        for (String[] test : stringTests) {
            (new TestString(test[0], test[1], test[2])).doTest();
        }

        for (Integer[] test : integerTests) {
            (new TestInteger(test[0], test[1], test[2])).doTest();
        }

        for (Double[] test : doubleTests) {
            (new TestDouble(test[0], test[1], test[2])).doTest();
        }

        assertTrue("Position GMTDtTradeOpen '" + opsc.getPositionModels().get(0).getDateOpen().toString()
                       + "' not the expected value of '2021-03-05T00:00'",
            opsc.getPositionModels().get(0).getDateOpen()
                .equals(CMHPIUtils.convertStringToLocalDateTime("2021-03-05 00:00:00")));

        //do not leave uncommented
//        cpoc.doSQL();
    }
}
