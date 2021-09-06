package DataMartController;

import com.hpi.TPCCMcontrollers.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class FifoLotsTest
    extends DataMartControllerBase {

    @Before
    public void setUp() {
        dmController.setUserId(816);
        stockController.setUserId(816);
        optionController.setUserId(816);

    }

    @After
    public void tearDown() {
    }

    @Ignore
    @Test
    public void doClearDmOfx816() {
        dbController.doClearDmOfxUserId(816);
    }

    @Ignore
    @Test
    public void doStockLots() {
        CMDBController.executeSQL("truncate hlhtxc5_dmOfx.ClosedStockFIFO;");
        CMDBController.executeSQL("truncate hlhtxc5_dmOfx.ClosedStockTrans;");
        CMDBController.executeSQL("truncate hlhtxc5_dmOfx.OpenStockFIFO;");

        dmController.processFIFOStockLotsAccounts();

    }

    @Ignore
    @Test
    public void doOptionLots() {
        CMDBController.executeSQL("truncate hlhtxc5_dmOfx.ClosedOptionFIFO;");
        CMDBController.executeSQL("truncate hlhtxc5_dmOfx.ClosedOptionTrans;");
        CMDBController.executeSQL("truncate hlhtxc5_dmOfx.OpenOptionFIFO;");

        dmController.processFIFOOptionLotsAccounts();

    }
}
