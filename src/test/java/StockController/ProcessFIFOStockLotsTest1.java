package StockController;

import com.hpi.TPCCMprefs.CMDBModel;
import com.hpi.appcontrollers.positions.ClosedPositionsStockController;
import com.hpi.appcontrollers.positions.OpenPositionsStockController;
import com.hpi.entities.OpeningStockModel;
import java.sql.Date;
import org.junit.*;

/**
 * Selected Stock transactions from OpeningStock so skip
 * processFIFOStockLostsAccounts
 *
 *
 */
public class ProcessFIFOStockLotsTest1
        extends StockControllerBase {

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void doStockSeries() {
        this.doStockLots1();
        this.doStockLots2();

        //have 2 in
        // process lots
        stockController.processFIFOStockLots();

        //process to DataMart
        dataMartController.processDataMart();

        //validate
        stockLots1Validation();
        stockLots2Validation();

        //positions
        OpenPositionsStockController.getInstance().doPositions();
        ClosedPositionsStockController.getInstance().doPositions();
    }

    /**
     * Single Lot
     */
    @Ignore
    @Test
    public void doStockLots1() {
//        FIFOOpenTransactionModel ftm;
        OpeningStockModel openingStockModel;
//        Integer index;

        //get opening list
        //we will instead specify some particular opening transactions
        openingStockModel = OpeningStockModel.builder()
                .dmAcctId(1)
                .joomlaId(CMDBModel.getUserId())
                .fiTId("210412_5961_0")
                .ticker("GM")
                .equityId("GM")
                .dateOpen(Date.valueOf("2021-04-12"))
                //            .dateClose(null)
                .shPerCtrct(1)
                .units(250.0)
                .priceOpen(60.18)
                //            .priceClose(null)
                //            .markUpDn(null)
                //            .commission(null)
                //            .taxes(null)
                //            .fees(null)
                //            .transLoad(null)
                .totalOpen(-15045.0000)
                //            .totalClose(null)
                //            .curSym(null)
                .subAcctSec("MARGIN")
                .subAcctFund("MARGIN")
                .equityType("STOCK")
                //            .optionType(null)
                .transactionType("BUY")
                //            .reversalFiTId(null)
                .comment("")
                .build();

        stockController.getStockOpeningList().add(openingStockModel);

    }

    private void stockLots1Validation() {
        Integer index;
        /**
         * get closing list there are none here as these are tests of open
         * positions
         */
        // process lots
//        stockController.processFIFOStockLots();
//
//        dataMartController.processDataMart();

//        stockController.processFIFOStockLotsAccounts();
        index = 0;
        String[][] stringTests
                = {
                    {"a", stockController.getStockOpeningList().get(index).getEquityId(), "GM"},
                    {"b", stockController.getStockOpeningList().get(index).getTicker(), "gm"}
                };

        Integer[][] integerTests
                = {
                    {1, stockController.getStockOpeningList().size(), index + 1}
                };

        Double[][] doubleTests
                = {
                    {1.0, stockController.getStockOpeningList().get(index).getUnits(), 250.0},
                    {2.0, stockController.getStockOpeningList().get(index).getPriceOpen(), 60.18},
                    {3.0, stockController.getStockOpeningList().get(index).getTotalOpen(), -15045.0000}
                };

        for (String[] test : stringTests) {
            (new TestString(test[0], test[1], test[2])).doTest();
        }

//        for (Integer[] test : integerTests) {
//            (new TestInteger(test[0], test[1], test[2])).doTest();
//        }
        for (Double[] test : doubleTests) {
            (new TestDouble(test[0], test[1], test[2])).doTest();
        }

//        assertTrue("Position GMTDtTradeOpen '" + opsc.getPositionModels().get(0).getDateOpen().toString()
//                       + "' not the expected value of '2021-03-05T00:00'",
//            opsc.getPositionModels().get(0).getDateOpen()
//                .equals(CMHPIUtils.convertStringToLocalDateTime("2021-03-05 00:00:00")));
        //do not leave uncommented
//        stockController..doSQL();
    }

    /**
     * Single Lot
     */
    @Ignore
    @Test
    public void doStockLots2() {
//        FIFOOpenTransactionModel ftm;
        OpeningStockModel openingStockModel;
//        Integer index;

        //get opening list
        //we will instead specify some particular opening transactions
        openingStockModel = OpeningStockModel.builder()
                .dmAcctId(1)
                .joomlaId(CMDBModel.getUserId())
                .fiTId("210505_6081_0")
                .ticker("GM")
                .equityId("GM")
                .dateOpen(Date.valueOf("2021-05-05"))
                //            .dateClose(null)
                .shPerCtrct(1)
                .units(50.0)
                .priceOpen(57.41)
                //            .priceClose(null)
                //            .markUpDn(null)
                //            .commission(null)
                //            .taxes(null)
                //            .fees(null)
                //            .transLoad(null)
                .totalOpen(-2870.500)
                //            .totalClose(null)
                //            .curSym(null)
                .subAcctSec("MARGIN")
                .subAcctFund("MARGIN")
                .equityType("STOCK")
                //            .optionType(null)
                .transactionType("BUY")
                //            .reversalFiTId(null)
                .comment("")
                .build();

        stockController.getStockOpeningList().add(openingStockModel);
    }

    private void stockLots2Validation() {
        Integer index;

        /**
         * get closing list there are none here as these are tests of open
         * positions
         */
        // process lots
//        stockController.processFIFOStockLots();
//
//        dataMartController.processDataMart();
//        stockController.processFIFOStockLotsAccounts();
        index = 1;
        String[][] stringTests
                = {
                    {"a", stockController.getStockOpeningList().get(index).getEquityId(), "GM"},
                    {"b", stockController.getStockOpeningList().get(index).getTicker(), "gm"}
                };

        Integer[][] integerTests
                = {
                    {1, stockController.getStockOpeningList().size(), index + 1}
                };

        Double[][] doubleTests
                = {
                    {1.0, stockController.getStockOpeningList().get(index).getUnits(), 50.0},
                    {2.0, stockController.getStockOpeningList().get(index).getPriceOpen(), 57.41},
                    {3.0, stockController.getStockOpeningList().get(index).getTotalOpen(), -2870.500}
                };

        for (String[] test : stringTests) {
            (new TestString(test[0], test[1], test[2])).doTest();
        }

//        for (Integer[] test : integerTests) {
//            (new TestInteger(test[0], test[1], test[2])).doTest();
//        }
        for (Double[] test : doubleTests) {
            (new TestDouble(test[0], test[1], test[2])).doTest();
        }

//        assertTrue("Position GMTDtTradeOpen '" + opsc.getPositionModels().get(0).getDateOpen().toString()
//                       + "' not the expected value of '2021-03-05T00:00'",
//            opsc.getPositionModels().get(0).getDateOpen()
//                .equals(CMHPIUtils.convertStringToLocalDateTime("2021-03-05 00:00:00")));
        //do not leave uncommented
//        stockController..doSQL();
    }
}
