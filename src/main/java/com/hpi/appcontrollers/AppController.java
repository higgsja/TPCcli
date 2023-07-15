package com.hpi.appcontrollers;

import com.hpi.TPCCMcontrollers.*;
import com.hpi.TPCCMprefs.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * Application core controller.
 * <p>
 */
public class AppController
{
    private static final ExecutorService TPCcli_EXECUTOR_SERVICE;
    
    static 
    {
        TPCcli_EXECUTOR_SERVICE = Executors.newFixedThreadPool(4);
    }

    //*** Singleton
    private static AppController instance;

    protected AppController()
    {
        // protected prevents instantiation outside of package
    }

    public synchronized static AppController getInstance()
    {
        if (AppController.instance == null)
        {
            AppController.instance = new AppController();
        }
        return AppController.instance;
    }
    //***

    public void initApp(){
        URL mySource = CmdLineController.class.getProtectionDomain().
              getCodeSource().getLocation();
        System.setProperty("app.root", mySource.getPath());

        CMLanguageController.getInstance();
        CMPrefsController.getInstance().initConfigFiles();
//        CMDBController.initDBConnection();
        CmdLineController.getInstance();
        CMPrefsController.getInstance();
        CMGlobalsModel.getInstance();
        CMGlobalsModel.setGui(false);
    }
    public void startApp(String[] args)
    {
//        URL mySource = CmdLineController.class.getProtectionDomain().
//              getCodeSource().getLocation();
//        System.setProperty("app.root", mySource.getPath());
//
//        CMLanguageController.getInstance();
//        CMPrefsController.getInstance().initConfigFiles();
//        CMDBController.initDBConnection();
//        CmdLineController.getInstance();
//        CMPrefsController.getInstance();
//        CMGlobalsModel.getInstance();
//        CMGlobalsModel.setGui(false);
        CmdLineController.getInstance().doCommandLine(args);
    }

    public static ExecutorService getTPCcli_EXECUTOR_SERVICE()
    {
        return TPCcli_EXECUTOR_SERVICE;
    }
}
