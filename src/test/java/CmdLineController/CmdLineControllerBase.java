package CmdLineController;

import com.hpi.appcontrollers.*;
import org.junit.*;

public class CmdLineControllerBase
{

    public static TPCcliAppController appController;
    public static CmdLineController cmdLineController;

    public static final Integer USER_ID = 5;

    @BeforeClass
    public static void beforeClass()
    {
        appController = TPCcliAppController.getInstance();
        cmdLineController = CmdLineController.getInstance();
    }

    @AfterClass
    public static void afterClass()
    {
    }
    
    @Before
    public void before(){
        //clear dmOfx for the test userId
        String[] args = new String[3];
        args[0] = "--clearDmOfxUserId";
        args[1] = "--userId";
        args[2] = USER_ID.toString();
        
        appController.initApp();
        cmdLineController.doCommandLine(args);
        
        //have to reset this to false for next step
        CmdLineController.setBClearDmOfxAcct(false);

    }
}
