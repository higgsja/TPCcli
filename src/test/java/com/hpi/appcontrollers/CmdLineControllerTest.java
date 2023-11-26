package com.hpi.appcontrollers;

import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;
import utils.BaseTest;

public class CmdLineControllerTest extends BaseTest {

    public CmdLineControllerTest() {
        super();
    }

    @BeforeClass
       public static void setUpClass() {
        System.out.println("@BeforeClass: ");

        // if (TPCCMGlobalsModel.getInstance() == null
        // || TTbCMLanguagesModel.getInstance() == null
        // || TPCCMLanguageController. == null)
        // {
        // System.exit(1);
        // }
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    @Override
    public void setUp() {
        this.setLogger(Logger.getLogger(this.getClass().toString()));
    }

    @After
    public void tearDown() {
    }

    @Ignore
    @Test
    @java.lang.SuppressWarnings("empty-statement")
    public void testValidDoParseCmdLine() {
        class Local {
        }
        ;
        println(Local.class.getEnclosingMethod().getName(), "doParseCmdLine() with all valid args");

        String[] args;
        String[] good;

        args = new String[1];
        // test good parameters
        good = new String[] { "--daily", "--chains" };

        for (int i = 0; i + 1 < good.length; i++) {
            args[0] = good[i];
            Assert.assertTrue("doParseCmdLine() failed with args=" + args[0],
                    CmdLineController.getInstance().doParseCmdLine(args));
        }
    }

    @Ignore
    @Test
    @java.lang.SuppressWarnings("empty-statement")
    public void testInvalidDoParseCmdLine() {
        class Local {
        }
        ;
        println(Local.class.getEnclosingMethod().getName(), "doParseCmdLine() with all invalid args");

        String[] args;
        String[] bad;

        args = new String[1];
        bad = new String[] { "test" };

        for (int i = 0; i + 1 < bad.length; i++) {
            args[0] = bad[i];
            Assert.assertFalse("doParseCmdLine() succeeded with invalid args=" + args[0],
                    CmdLineController.getInstance().doParseCmdLine(args));
        }
    }

    @Ignore
    @Test
    @java.lang.SuppressWarnings("empty-statement")
    public void testEquityInfo() {
        class Local {
        }
        ;
        println(Local.class.getEnclosingMethod().getName(), "startApp() with args (--equityInfo -> command line)");

        String[] args = new String[3];
        args[0] = "--equityInfo";
        args[1] = "--progressBar";
        args[2] = "true";
        TPCcliAppController.getInstance().startApp(args);
    }

    @Ignore
    @Test
    @java.lang.SuppressWarnings("empty-statement")
    public void testIEXEquityHistory() {
        class Local {
        }
        ;
        println(Local.class.getEnclosingMethod().getName(),
                "startApp() with args " + "(--equityHistoryIEX -> command line)");

        String[] args = new String[3];
        args[0] = "--equityHistoryIEX";
        args[1] = "--progressBar";
        args[2] = "true";
        TPCcliAppController.getInstance().startApp(args);
    }

    @Ignore
    @Test
    @java.lang.SuppressWarnings("empty-statement")
    public void testIEXEquityHistoryMin() {
        class Local {
        }
        ;
        println(Local.class.getEnclosingMethod().getName(),
                "startApp() with args " + "(--equityHistoryIEXMin -> command line)");

        String[] args = new String[3];
        args[0] = "--equityHistoryIEXMin";
        args[1] = "--progressBar";
        args[2] = "true";
        TPCcliAppController.getInstance().startApp(args);
    }

    @Ignore
    @Test
    @java.lang.SuppressWarnings("empty-statement")
    public void testIEXEquityHistoryDate() {
        class Local {
        }
        ;
        println(Local.class.getEnclosingMethod().getName(),
                "startApp() with args " + "(--equityHistoryIEX --date 2019-06-27 -> command line)");

        String[] args = new String[5];
        args[0] = "--equityHistoryIEX";
        args[1] = "--progressBar";
        args[2] = "true";
        args[3] = "--date";
        args[4] = "2019-12-31";
        TPCcliAppController.getInstance().startApp(args);
    }

    @Ignore
    @Test
    @java.lang.SuppressWarnings("empty-statement")
    public void testOptionHistory() {
        class Local {
        }
        ;
        println(Local.class.getEnclosingMethod().getName(),
                "startApp() with args " + "(--optionHistory -> command line)");

        String[] args = new String[3];
        args[0] = "--optionHistory";
        args[1] = "--progressBar";
        args[2] = "true";
        TPCcliAppController.getInstance().startApp(args);
    }

    @Ignore
    @Test
    @java.lang.SuppressWarnings("empty-statement")
    public void testTrdStnDl() {
        class Local {
        }
        ;
        println(Local.class.getEnclosingMethod().getName(),
                "startApp() with args (--trdstnDl --directory xxx) -> command line");

        String[] args = new String[3];
        args[0] = "--trdstnDl";
        args[1] = "--directory";
        args[2] = "/home/white/Documents/Quicken";
        TPCcliAppController.getInstance().startApp(args);
    }

    @Ignore
    @Test
    @java.lang.SuppressWarnings("empty-statement")
    public void testTrdStnDlHeadless() {
        class Local {
        }
        ;
        println(Local.class.getEnclosingMethod().getName(),
                "startApp() with args (--trdstnDl " + "--directory xxx) " + "--headless true -> command line");

        String[] args = new String[5];
        args[0] = "--trdstnDl";
        args[1] = "--directory";
        args[2] = "/home/white/Documents/Quicken";
        args[3] = "--headless";
        args[4] = "true";
        TPCcliAppController.getInstance().startApp(args);
    }

    @Ignore
    @Test
    @java.lang.SuppressWarnings("empty-statement")
    public void testDataMartDate() {
        class Local {
        }
        ;
        println(Local.class.getEnclosingMethod().getName(),
                "startApp() with args (--dataMart --progressbar true " + "--date 19700101 -> command line)");

        String[] args = new String[5];
        args[0] = "--dataMart";
        args[1] = "--progressBar";
        args[2] = "true";
        args[3] = "--date";
        args[4] = "2020-03-01";
        TPCcliAppController.getInstance().startApp(args);
    }

    @Ignore
    @Test
    @java.lang.SuppressWarnings("empty-statement")
    public void testAll() {
        class Local {
        }
        ;
        println(Local.class.getEnclosingMethod().getName(), "startApp() with args (--dataMart -> command line)");

        String[] args = new String[3];
        args[0] = "--dataMart";
        args[1] = "--progressBar";
        args[2] = "true";
        TPCcliAppController.getInstance().startApp(args);
    }

    @Ignore
    @Test
    @java.lang.SuppressWarnings("empty-statement")
    public void testMPT() {
        class Local {
        }
        ;
        println(Local.class.getEnclosingMethod().getName(), "startApp() with args " + "(--mpt -> command line)");

        String[] args = new String[1];
        args[0] = "--mpt";
        TPCcliAppController.getInstance().startApp(args);
    }

    @Ignore
    @Test
    @java.lang.SuppressWarnings("empty-statement")
    public void testInstitutions() {
        class Local {
        }
        ;
        println(Local.class.getEnclosingMethod().getName(),
                "startApp() with args " + "(--ofxInstitutions -> command line)");

        String[] args = new String[1];
        args[0] = "--ofxInstitutions";
        TPCcliAppController.getInstance().startApp(args);
    }

    @Ignore
    @Test
    @java.lang.SuppressWarnings("empty-statement")
    public void testTdAmeritradeOptions() {
        class Local {
        }
        ;
        println(Local.class.getEnclosingMethod().getName(), "startApp() with args (--tdAmeritradeOptions)");

        String[] args = new String[1];
        args[0] = "--tdAmeritradeOptions";
        // args[1] = "--progressBar";
        // args[2] = "true";
        TPCcliAppController.getInstance().startApp(args);
    }

    @Ignore
    @Test
    @java.lang.SuppressWarnings("empty-statement")
    public void testTdAmeritradeStocks() {
        class Local {
        }
        ;
        println(Local.class.getEnclosingMethod().getName(), "startApp() with args (--tdAmeritradeStocks)");

        String[] args = new String[1];
        args[0] = "--tdAmeritradeStocks";
        // args[1] = "--progressBar";
        // args[2] = "true";
        TPCcliAppController.getInstance().startApp(args);
    }

    @Ignore
    @Test
    @java.lang.SuppressWarnings("empty-statement")
    public void clearUserId() {
        class Local {
        }
        ;
        println(Local.class.getEnclosingMethod().getName(), "startApp() with args (--clearUserId --userId 1)");

        String[] args = new String[3];
        args[0] = "--clearUserId";
        args[1] = "--userId";
        args[2] = "818";
        TPCcliAppController.getInstance().startApp(args);
    }

    @Ignore
    @Test
    @java.lang.SuppressWarnings("empty-statement")
    public void buildDemo() {
        class Local {
        }
        ;
        println(Local.class.getEnclosingMethod().getName(), "startApp() with args (--buildDemo --userId 1)");

        String[] args = new String[3];
        args[0] = "--buildDemo";
        args[1] = "--userId";
        args[2] = "1";
        TPCcliAppController.getInstance().startApp(args);
    }

    @Ignore
    @Test
    @java.lang.SuppressWarnings("empty-statement")
    public void markFiTIdIgnore() {
        class Local {
        }
        ;
        println(Local.class.getEnclosingMethod().getName(),
                "startApp() with args (--ignoreFiTId --fiTId String --acctId Integer)");

        String[] args = new String[5];
        args[0] = "--ignoreFiTId";
        args[1] = "--fiTId";
        args[2] = "200409_1124_1";
        args[3] = "--acctId";
        args[4] = "2";
        TPCcliAppController.getInstance().startApp(args);
    }

    @Ignore
    @Test
    @java.lang.SuppressWarnings("empty-statement")
    public void resetSkip() {
        class Local {
        }
        ;
        println(Local.class.getEnclosingMethod().getName(),
                "startApp() with args (--resetSkip --fiTId String --acctId Integer)");

        String[] args = new String[5];
        args[0] = "--resetSkip";
        args[1] = "--fiTId";
        args[2] = "200409_1142_0";
        args[3] = "--acctId";
        args[4] = "2";
        TPCcliAppController.getInstance().startApp(args);
    }

    @Ignore
    @Test
    @java.lang.SuppressWarnings("empty-statement")
    public void processOfxFiles() {
        class Local {
        }
        ;
        println(Local.class.getEnclosingMethod().getName(), "startApp() with args (--processOfxFiles)");

        String[] args = new String[1];
        args[0] = "--processOfxFiles";
        TPCcliAppController.getInstance().startApp(args);
    }
}
