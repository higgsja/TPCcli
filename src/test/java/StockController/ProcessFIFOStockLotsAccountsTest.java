package StockController;

import org.junit.*;

public class ProcessFIFOStockLotsAccountsTest
        extends StockControllerBase {

    @Before
    public void setUp() {
        stockController.setUserId(816);
    }

    @After
    public void tearDown() {
    }

    @Ignore
    @Test
    public void doStockLotsAccounts() {
        dataMartController.processDataMart();
        dbController.doClearDbOfxUserId(816);

        stockController.processFIFOStockLotsAccounts();
    }
}
