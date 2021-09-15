package Positions.OpenPositionStockController;

import com.hpi.TPCCMcontrollers.*;
import com.hpi.TPCCMprefs.*;
import com.hpi.appcontrollers.positions.*;
import org.junit.*;

public class OpenPositionsStockBase
{

    public static OpenPositionsStockController opsController;

    public static final Integer USER_ID = 5;

    @BeforeClass
    public static void setupClass()
    {
        CMDBModel.getInstance();
        CMDBModel.setUserId(USER_ID);
        CMDBController.initDBConnection();
        
        CMLanguageController.getInstance();

        opsController = OpenPositionsStockController.getInstance();
    }

    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void beforeClass()
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
        
        opsController.getFifoTransactionModels().clear();
        opsController.getPositionTransactionModels().clear();
        opsController.getPositionModels().clear();
    }
}
