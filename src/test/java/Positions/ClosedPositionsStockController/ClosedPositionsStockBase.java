package Positions.ClosedPositionsStockController;

import com.hpi.TPCCMcontrollers.*;
import com.hpi.TPCCMprefs.*;
import com.hpi.appcontrollers.positions.*;
import org.junit.*;

public class ClosedPositionsStockBase
{

    public static ClosedPositionsStockController cpsController;

    public static final Integer USER_ID = 5;

    @BeforeClass
    public static void beforeClass()
    {
        CMDBModel.getInstance();
        CMDBModel.setUserId(USER_ID);
        CMDBController.initDBConnection();

        CMLanguageController.getInstance();

        cpsController = ClosedPositionsStockController.getInstance();
    }

    @AfterClass
    public static void afterClass()
    {
    }

    @Before
    public void before()
    {
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
        
        cpsController.getFifoClosedTransactionModels().clear();
        cpsController.getPositionTransactionModels().clear();
        cpsController.getPositionModels().clear();
    }
}
