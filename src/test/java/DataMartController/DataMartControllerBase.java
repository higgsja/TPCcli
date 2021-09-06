package DataMartController;

import com.hpi.TPCCMcontrollers.*;
import com.hpi.TPCCMprefs.CMDBModel;
import com.hpi.appcontrollers.DataMartController;
import com.hpi.appcontrollers.DbOfxController;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class DataMartControllerBase 
{
    public static DataMartController dmController;
    public static DbOfxController dbController;
    public static StockController stockController;
    public static OptionController optionController;

    @BeforeClass
    public static void setUpClass() {
        CMDBModel.getInstance();
        CMDBModel.setUserId(0);
        CMDBController.initDBConnection();
        
        dmController = DataMartController.getInstance();
        dbController = DbOfxController.getInstance();
        stockController = StockController.getInstance();
        optionController = OptionController.getInstance();
    }

    @AfterClass
    public static void tearDownClass() {
    }
}
