package StockController;

import com.hpi.TPCCMcontrollers.*;
import com.hpi.TPCCMprefs.CMDBModel;
import com.hpi.appcontrollers.*;
import com.hpi.appcontrollers.positions.OpenPositionsStockController;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class StockControllerBase
{

    public static DataMartController dataMartController;
    public static DbOfxController dbController;
    public static StockController stockController;
    public static OpenPositionsStockController opsc;

    private static final Integer USER_ID = 5;

    @BeforeClass
    public static void setUpClass()
    {
        CMDBModel.getInstance();
        CMDBModel.setUserId(StockControllerBase.USER_ID);
        CMDBController.initDBConnection();

        dataMartController = DataMartController.getInstance();
        dbController = DbOfxController.getInstance();
        stockController = StockController.getInstance();
        opsc = OpenPositionsStockController.getInstance();

        stockController.getAccountList().clear();
        stockController.getEquityIdList().clear();
        stockController.getStockOpeningList().clear();
        stockController.getStockClosingList().clear();
        stockController.getStockClosedList().clear();
        stockController.getStockClosedTransList().clear();
        stockController.getStockOpenList().clear();
        stockController.setUserId(USER_ID);

        //clear closedStockFIFO for user id
        CMDBController.executeSQL("delete from hlhtxc5_dmOfx.ClosedStockFIFO where JoomlaId = " + USER_ID);

        //clear openStockFIFO for user id
        CMDBController.executeSQL("delete from hlhtxc5_dmOfx.OpenStockFIFO where JoomlaId = " + USER_ID);

        //clear fifoOpenTransactions
        CMDBController
            .executeSQL("delete from hlhtxc5_dmOfx.FIFOOpenTransactions where JoomlaId = " + USER_ID);

        //clear fifoClosedTransactions
        CMDBController
            .executeSQL("delete from hlhtxc5_dmOfx.FIFOClosedTransactions where JoomlaId = " + USER_ID);

        //clear positionsOpenTransactions
        CMDBController
            .executeSQL("delete from hlhtxc5_dmOfx.PositionsOpenTransactions where JoomlaId = " + USER_ID);

        //clear positionsClosedTransactions
        CMDBController
            .executeSQL("delete from hlhtxc5_dmOfx.PositionsClosedTransactions where JoomlaId = " + USER_ID);

        //clear positionsOpen
        CMDBController.executeSQL("delete from hlhtxc5_dmOfx.PositionsOpen where JoomlaId = " + USER_ID);

        //clear positionsClosed
        CMDBController.executeSQL("delete from hlhtxc5_dmOfx.PositionsClosed where JoomlaId = " + USER_ID);

        CMLanguageController.getInstance();
    }

    @AfterClass
    public static void tearDownClass()
    {
    }
}
