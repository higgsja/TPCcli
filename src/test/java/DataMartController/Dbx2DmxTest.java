package DataMartController;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class Dbx2DmxTest
        extends DataMartControllerBase {

    @Before
    public void setUp() {
        dbController.doClearDmOfxUserId(816);
        dmController.setUserId(816);

    }

    @After
    public void tearDown() {
    }
    
    @Ignore
    @Test
    public void doClearDmOfx816(){
                dbController.doClearDmOfxUserId(816);
        
    }

    @Ignore
    @Test
    public void doStocks() {
        //move stock transactions
        dmController.doDbx2DmxStock();

    }

    @Ignore
    @Test
    public void doClientStocks() {
        //move clientStock transactions
        dmController.doClientStock();
    }
    
    @Ignore
    @Test
    public void doOptions() {
        //move clientStock transactions
        dmController.doDbx2DmxOptions();
    }


    @Ignore
    @Test
    public void doClientOptions() {
        //todo: add clientOption capability
        dbController.doClearDmOfxUserId(816);
        dmController.setUserId(816);

        //move clientStock transactions
        dmController.doClientStock();
    }
}
